package com.company.office_admin.dto;

public class AddRecordRequest {
    private String itemType; // POST, ACCOUNT, REPORT, NOTE, CAMPAIGN, MEDIA, IMAGE, VIDEO
    private Long itemId;
    private String title;
    private String subtitle;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String badge;
    private String badgeColor;

    public AddRecordRequest() {}

    public AddRecordRequest(String itemType, Long itemId, String title, String subtitle, String content,
                            String mediaUrl, String mediaType, String badge, String badgeColor) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.badge = badge;
        this.badgeColor = badgeColor;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }
}
