package com.company.office_admin.service;

import com.company.office_admin.entity.Notification;
import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.repository.NotificationRepository;
import com.company.office_admin.repository.SocialPostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostReminderScheduler.class);

    private final SocialPostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService deliveryService;

    public PostReminderScheduler(SocialPostRepository postRepository,
                                 NotificationRepository notificationRepository,
                                 NotificationDeliveryService deliveryService) {
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryService = deliveryService;
    }

    /**
     * Checks every 30 seconds for upcoming post reminders and publishes due posts.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void scanAndProcessScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<SocialPost> scheduledPosts = postRepository.findByStatusOrderByScheduledAtAsc("SCHEDULED");

        if (scheduledPosts.isEmpty()) {
            return;
        }

        for (SocialPost post : scheduledPosts) {
            LocalDateTime scheduledAt = post.getScheduledAt();
            if (scheduledAt == null) continue;

            int reminderWindow = post.getReminderMinutesBefore() != null ? post.getReminderMinutesBefore() : 15;
            LocalDateTime reminderTarget = scheduledAt.minusMinutes(reminderWindow);

            // 1. Check if it's time to send the upcoming reminder (Email & SMS/MSG)
            if (!Boolean.TRUE.equals(post.getReminderSent())) {
                if (!now.isBefore(reminderTarget) && now.isBefore(scheduledAt.plusMinutes(5))) {
                    long minutesUntil = Duration.between(now, scheduledAt).toMinutes();
                    int mins = Math.max(1, (int) minutesUntil);
                    log.info("Sending upcoming reminder for post ID {} on {} ({}m remaining)", post.getId(), post.getPlatform(), mins);
                    deliveryService.sendPostUpcomingReminder(post, mins);
                    post.setReminderSent(true);
                    postRepository.save(post);
                }
            }

            // 2. Automatically publish due posts when scheduled time is reached
            if (now.isAfter(scheduledAt) || now.isEqual(scheduledAt)) {
                log.info("Scheduled post ID {} on {} has reached publish time. Publishing now.", post.getId(), post.getPlatform());
                post.setStatus("PUBLISHED");
                post.setPublishedAt(now);
                postRepository.save(post);

                notificationRepository.save(new Notification(
                        "Post Auto-Published",
                        "Your scheduled " + post.getPlatform() + " post \"" + truncate(post.getTitle(), 35) + "\" was published automatically.",
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
