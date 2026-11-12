package com.company.office_admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "social_posts")
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform; // LinkedIn, Instagram, Twitter, YouTube

    @Column(name = "platform_class", nullable = false)
    private String platformClass; // linkedin, instagram, twitter, youtube

    @Column(name = "account_handle")
    private String accountHandle;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String caption;

    @Column(name = "media_type")
    private String mediaType; // Carousel, Single Image, Video, Thread

    @Column(name = "media_url", columnDefinition = "LONGTEXT")
    private String mediaUrl;

    @Column(nullable = false)
    private String status; // PUBLISHED, SCHEDULED, DRAFT

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "likes_count")
    private Long likesCount = 0L;

    @Column(name = "comments_count")
    private Long commentsCount = 0L;

    @Column(name = "shares_count")
    private Long sharesCount = 0L;

    @Column(name = "reach_count")
    private Long reachCount = 0L;

    @Column(name = "engagement_rate")
    private Double engagementRate = 0.0;

    @Column(name = "remind_email")
    private Boolean remindEmail = true;

    @Column(name = "remind_sms")
    private Boolean remindSms = true;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore = 15;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "confirmation_sent")
    private Boolean confirmationSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public SocialPost() {}

    public SocialPost(String platform, String platformClass, String accountHandle, String title,
                      String caption, String mediaType, String status,
                      LocalDateTime scheduledAt, LocalDateTime publishedAt,
                      Long likesCount, Long commentsCount, Long sharesCount,
                      Long reachCount, Double engagementRate) {
        this.platform = platform;
        this.platformClass = platformClass;
        this.accountHandle = accountHandle;
        this.title = title;
        this.caption = caption;
        this.mediaType = mediaType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.publishedAt = publishedAt;
        this.likesCount = likesCount != null ? likesCount : 0L;
        this.commentsCount = commentsCount != null ? commentsCount : 0L;
        this.sharesCount = sharesCount != null ? sharesCount : 0L;
        this.reachCount = reachCount != null ? reachCount : 0L;
        this.engagementRate = engagementRate != null ? engagementRate : 0.0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getPlatformClass() { return platformClass; }
    public void setPlatformClass(String platformClass) { this.platformClass = platformClass; }

    public String getAccountHandle() { return accountHandle; }
    public void setAccountHandle(String accountHandle) { this.accountHandle = accountHandle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public boolean isVideo() {
        return (mediaType != null && mediaType.toLowerCase().contains("video")) ||
               (mediaUrl != null && (mediaUrl.startsWith("data:video") || mediaUrl.contains(".mp4") || mediaUrl.contains(".webm") || mediaUrl.contains(".mov")));
    }

    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.isBlank();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Long getLikesCount() { return likesCount; }
    public void setLikesCount(Long likesCount) { this.likesCount = likesCount; }

    public Long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Long commentsCount) { this.commentsCount = commentsCount; }

    public Long getSharesCount() { return sharesCount; }
    public void setSharesCount(Long sharesCount) { this.sharesCount = sharesCount; }

    public Long getReachCount() { return reachCount; }
    public void setReachCount(Long reachCount) { this.reachCount = reachCount; }

    public Double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(Double engagementRate) { this.engagementRate = engagementRate; }

    public Boolean getRemindEmail() { return remindEmail != null ? remindEmail : false; }
    public void setRemindEmail(Boolean remindEmail) { this.remindEmail = remindEmail; }

    public Boolean getRemindSms() { return remindSms != null ? remindSms : false; }
    public void setRemindSms(Boolean remindSms) { this.remindSms = remindSms; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public Integer getReminderMinutesBefore() { return reminderMinutesBefore != null ? reminderMinutesBefore : 15; }
    public void setReminderMinutesBefore(Integer reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    public Boolean getReminderSent() { return reminderSent != null ? reminderSent : false; }
    public void setReminderSent(Boolean reminderSent) { this.reminderSent = reminderSent; }

    public Boolean getConfirmationSent() { return confirmationSent != null ? confirmationSent : false; }
    public void setConfirmationSent(Boolean confirmationSent) { this.confirmationSent = confirmationSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedPublishedTime() {
        if (publishedAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        return publishedAt.format(formatter);
    }

    public String getFormattedScheduledDate() {
        if (scheduledAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        return scheduledAt.format(formatter);
    }

    public String getCountdown() {
        if (scheduledAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        if (scheduledAt.isBefore(now)) return "Overdue";
        long hours = java.time.Duration.between(now, scheduledAt).toHours();
        if (hours < 1) {
            long minutes = java.time.Duration.between(now, scheduledAt).toMinutes();
            return "In " + Math.max(1, minutes) + "m";
        }
        if (hours < 24) {
            return "In " + hours + "h";
        }
        long days = java.time.Duration.between(now, scheduledAt).toDays();
        if (days == 1) return "Tomorrow";
        return "In " + days + " days";
    }

    public String getFormattedLikes() {
        return formatCount(likesCount);
    }

    public String getFormattedComments() {
        return formatCount(commentsCount);
    }

    public String getFormattedShares() {
        return formatCount(sharesCount);
    }

    public String getFormattedReach() {
        return formatCount(reachCount);
    }

    private String formatCount(Long count) {
        if (count == null || count == 0) return "0";
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000.0);
        if (count >= 1_000) return String.format("%.1fK", count / 1_000.0);
        return String.valueOf(count);
    }
}

