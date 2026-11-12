package com.company.office_admin.dto;

public class FolderRecordDto {
    private Long id;
    private Long folderId;
    private String itemType;
    private Long itemId;
    private String title;
    private String subtitle;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private String badge;
    private String badgeColor;
    private String createdAtFormatted;

    public FolderRecordDto() {}

    public FolderRecordDto(Long id, Long folderId, String itemType, Long itemId, String title,
                           String subtitle, String content, String mediaUrl, String mediaType,
                           String badge, String badgeColor, String createdAtFormatted) {
        this.id = id;
        this.folderId = folderId;
        this.itemType = itemType;
        this.itemId = itemId;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.badge = badge;
        this.badgeColor = badgeColor;
        this.createdAtFormatted = createdAtFormatted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
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

    public boolean isHasMedia() {
        return mediaUrl != null && !mediaUrl.isBlank();
    }

    public boolean isVideo() {
        if (!isHasMedia()) return false;
        String t = mediaType != null ? mediaType.toLowerCase() : "";
        String u = mediaUrl.toLowerCase();
        return t.contains("video") || u.startsWith("data:video") || u.contains(".mp4") || u.contains(".webm") || u.contains(".mov");
    }

    public boolean isImage() {
        return isHasMedia() && !isVideo();
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

    public String getCreatedAtFormatted() {
        return createdAtFormatted;
    }

    public void setCreatedAtFormatted(String createdAtFormatted) {
        this.createdAtFormatted = createdAtFormatted;
    }
}
