package com.company.office_admin.dto;

import java.util.List;

public class CreatePostRequest {
    private List<String> channels;
    private String content;
    private String publishOption; // "now", "schedule", "draft"
    private String scheduledDateTime; // e.g. "2026-11-03T14:30"
    private String mediaType; // "Single Image", "Video", "Carousel"
    private String mediaUrl; // Data URL or web link for image/video
    private Boolean remindEmail = true;
    private Boolean remindSms = true;
    private String recipientEmail;
    private String recipientPhone;
    private Integer reminderMinutesBefore = 15;

    public CreatePostRequest() {}

    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPublishOption() { return publishOption; }
    public void setPublishOption(String publishOption) { this.publishOption = publishOption; }

    public String getScheduledDateTime() { return scheduledDateTime; }
    public void setScheduledDateTime(String scheduledDateTime) { this.scheduledDateTime = scheduledDateTime; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

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
}

