package com.company.office_admin.scheduler;

import com.company.office_admin.entity.Notification;
import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.repository.NotificationRepository;
import com.company.office_admin.repository.SocialPostRepository;
import com.company.office_admin.service.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(PostSchedulerService.class);

    private final SocialPostRepository postRepository;
    private final NotificationDeliveryService deliveryService;
    private final NotificationRepository notificationRepository;

    public PostSchedulerService(SocialPostRepository postRepository,
                                NotificationDeliveryService deliveryService,
                                NotificationRepository notificationRepository) {
        this.postRepository = postRepository;
        this.deliveryService = deliveryService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Executes every 30 seconds to:
     * 1. Send upcoming reminders (e.g. 15 minutes before scheduled time) via Email and SMS/WhatsApp
     * 2. Automatically publish posts whose scheduled time has arrived
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processScheduledPostsAndReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<SocialPost> scheduledPosts = postRepository.findByStatusOrderByScheduledAtAsc("SCHEDULED");

        if (scheduledPosts.isEmpty()) {
            return;
        }

        for (SocialPost post : scheduledPosts) {
            LocalDateTime sched = post.getScheduledAt();
            if (sched == null) continue;

            // 1. Check for upcoming pre-publication reminders
            int reminderMinutes = post.getReminderMinutesBefore() != null ? post.getReminderMinutesBefore() : 15;
            LocalDateTime reminderThreshold = sched.minusMinutes(reminderMinutes);

            if (!Boolean.TRUE.equals(post.getReminderSent()) && (now.isAfter(reminderThreshold) || now.isEqual(reminderThreshold)) && now.isBefore(sched)) {
                long minutesRemaining = Math.max(1, Duration.between(now, sched).toMinutes());
                log.info("Sending upcoming reminder for post ID {} on {} ({} minutes remaining)", post.getId(), post.getPlatform(), minutesRemaining);

                deliveryService.sendPostUpcomingReminder(post, (int) minutesRemaining);
                post.setReminderSent(true);
                postRepository.save(post);
            }

            // 2. Check if scheduled time has arrived -> Auto Publish!
            if (now.isAfter(sched) || now.isEqual(sched)) {
                log.info("Auto-publishing scheduled post ID {} on {}", post.getId(), post.getPlatform());
                post.setStatus("PUBLISHED");
                post.setPublishedAt(now);
                if (post.getLikesCount() == null) post.setLikesCount(0L);
                if (post.getCommentsCount() == null) post.setCommentsCount(0L);
                if (post.getSharesCount() == null) post.setSharesCount(0L);
                if (post.getReachCount() == null) post.setReachCount(0L);
                if (post.getEngagementRate() == null) post.setEngagementRate(0.0);
                postRepository.save(post);

                notificationRepository.save(new Notification(
                        "Post Auto-Published",
                        "Scheduled post on " + post.getPlatform() + " (\"" + truncate(post.getTitle(), 35) + "\") is now live.",
                        "trend"
                ));
            }
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

