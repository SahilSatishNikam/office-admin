package com.company.office_admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts")
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform; // Instagram, LinkedIn, Twitter, YouTube

    @Column(nullable = false)
    private String handle;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "followers_count")
    private Long followersCount;

    @Column(name = "growth_rate")
    private Double growthRate;

    @Column(name = "engagement_rate")
    private Double engagementRate;

    @Column(name = "posts_count")
    private Integer postsCount;

    @Column(name = "badge_class")
    private String badgeClass; // instagram, linkedin, twitter, youtube

    @Column(name = "brand_color")
    private String brandColor;

    private boolean connected;

    @Column(name = "oauth_connected")
    private Boolean oauthConnected = false;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SocialAccount() {}

    public SocialAccount(String platform, String handle, Long followersCount, Double growthRate,
                         Double engagementRate, Integer postsCount, String badgeClass,
                         String brandColor, boolean connected) {
        this.platform = platform;
        this.handle = handle;
        this.followersCount = followersCount;
        this.growthRate = growthRate;
        this.engagementRate = engagementRate;
        this.postsCount = postsCount;
        this.badgeClass = badgeClass;
        this.brandColor = brandColor;
        this.connected = connected;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getProfileUrl() {
        if (profileUrl != null && !profileUrl.isBlank()) {
            return profileUrl;
        }
        if (handle == null || handle.isBlank()) return "#";
        String clean = handle.trim();
        if (clean.startsWith("http://") || clean.startsWith("https://")) {
            return clean;
        }
        String cleanHandle = clean.replaceFirst("^@", "");
        String norm = platform != null ? platform.toLowerCase() : "";
        if (norm.contains("insta")) {
            return "https://www.instagram.com/" + cleanHandle;
        } else if (norm.contains("link")) {
            return "https://www.linkedin.com/in/" + cleanHandle;
        } else if (norm.contains("twit") || norm.contains("x")) {
            return "https://x.com/" + cleanHandle;
        } else if (norm.contains("you")) {
            return "https://www.youtube.com/@" + cleanHandle;
        } else if (norm.contains("face")) {
            return "https://www.facebook.com/" + cleanHandle;
        } else if (norm.contains("tik")) {
            return "https://www.tiktok.com/@" + cleanHandle;
        }
        return "#";
    }

    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public Double getGrowthRate() { return growthRate; }
    public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }

    public Double getEngagementRate() { return engagementRate; }
    public void setEngagementRate(Double engagementRate) { this.engagementRate = engagementRate; }

    public Integer getPostsCount() { return postsCount; }
    public void setPostsCount(Integer postsCount) { this.postsCount = postsCount; }

    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }

    public String getBrandColor() { return brandColor; }
    public void setBrandColor(String brandColor) { this.brandColor = brandColor; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public boolean isOauthConnected() { return Boolean.TRUE.equals(oauthConnected); }
    public void setOauthConnected(Boolean oauthConnected) { this.oauthConnected = oauthConnected != null ? oauthConnected : false; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFormattedFollowers() {
        if (followersCount == null) return "0";
        if (followersCount >= 1_000_000) {
            return String.format("%.1fM", followersCount / 1_000_000.0);
        } else if (followersCount >= 1_000) {
            return String.format("%.1fK", followersCount / 1_000.0);
        }
        return String.valueOf(followersCount);
    }

    public String getFormattedGrowth() {
        if (growthRate == null) return "0%";
        return (growthRate >= 0 ? "+" : "") + String.format("%.1f%%", growthRate);
    }

    public String getFormattedEngagement() {
        if (engagementRate == null) return "0%";
        return String.format("%.1f%%", engagementRate);
    }
}
