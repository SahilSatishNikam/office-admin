package com.company.office_admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "folder_records")
public class FolderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    @JsonIgnore
    private DashboardFolder folder;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType; // POST, ACCOUNT, REPORT, NOTE, CAMPAIGN, MEDIA, IMAGE, VIDEO

    @Column(name = "item_id")
    private Long itemId; // referenced entity id if applicable

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url", columnDefinition = "LONGTEXT")
    private String mediaUrl;

    @Column(name = "media_type", length = 50)
    private String mediaType; // Image, Video, Banner, Carousel

    @Column(length = 50)
    private String badge;

    @Column(name = "badge_color", length = 30)
    private String badgeColor = "primary"; // primary, success, warning, info, purple

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public FolderRecord() {}

    public FolderRecord(DashboardFolder folder, String itemType, Long itemId, String title,
                        String subtitle, String content, String badge, String badgeColor) {
        this(folder, itemType, itemId, title, subtitle, content, null, null, badge, badgeColor);
    }

    public FolderRecord(DashboardFolder folder, String itemType, Long itemId, String title,
                        String subtitle, String content, String mediaUrl, String mediaType,
                        String badge, String badgeColor) {
        this.folder = folder;
        this.itemType = itemType != null ? itemType : "NOTE";
        this.itemId = itemId;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.badge = badge;
        this.badgeColor = badgeColor != null ? badgeColor : "primary";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DashboardFolder getFolder() {
        return folder;
    }

    public void setFolder(DashboardFolder folder) {
        this.folder = folder;
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

    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.isBlank();
    }

    public boolean isVideo() {
        if (!hasMedia()) return false;
        String t = mediaType != null ? mediaType.toLowerCase() : "";
        String u = mediaUrl.toLowerCase();
        return t.contains("video") || u.startsWith("data:video") || u.contains(".mp4") || u.contains(".webm") || u.contains(".mov");
    }

    public boolean isImage() {
        return hasMedia() && !isVideo();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtFormatted() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
}
