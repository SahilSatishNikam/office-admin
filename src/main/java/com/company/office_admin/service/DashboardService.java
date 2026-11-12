package com.company.office_admin.service;

import com.company.office_admin.dto.ChartSeriesDto;
import com.company.office_admin.dto.CreatePostRequest;
import com.company.office_admin.dto.DashboardStatsDto;
import com.company.office_admin.dto.SearchResultDto;
import com.company.office_admin.entity.FollowerHistory;
import com.company.office_admin.entity.Notification;
import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.repository.FollowerHistoryRepository;
import com.company.office_admin.repository.NotificationRepository;
import com.company.office_admin.repository.SocialAccountRepository;
import com.company.office_admin.repository.SocialPostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class DashboardService {

    private final SocialAccountRepository accountRepository;
    private final SocialPostRepository postRepository;
    private final FollowerHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService deliveryService;

    @Value("${workspace.max-accounts:20}")
    private int maxAccounts = 20;

    public DashboardService(SocialAccountRepository accountRepository,
                            SocialPostRepository postRepository,
                            FollowerHistoryRepository historyRepository,
                            NotificationRepository notificationRepository,
                            NotificationDeliveryService deliveryService) {
        this.accountRepository = accountRepository;
        this.postRepository = postRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
        this.deliveryService = deliveryService;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. Total Followers
        Long totalFollowers = accountRepository.sumTotalFollowers();
        stats.setTotalFollowers(formatNumber(totalFollowers));

        // 2. Dynamic Follower Growth Calculation from 30d history
        List<FollowerHistory> monthHistory = historyRepository.findByPeriodTagOrderByRecordedDateAsc("30d");
        if (monthHistory != null && monthHistory.size() >= 2) {
            long earliest = monthHistory.get(0).getCurrentFollowers();
            long latest = monthHistory.get(monthHistory.size() - 1).getCurrentFollowers();
            long netDiff = latest - earliest;
            double growthPct = earliest > 0 ? ((double) netDiff / earliest) * 100 : 0.0;
            stats.setFollowersGrowth(String.format("%s%.1f%%", growthPct >= 0 ? "+" : "", growthPct));
            stats.setNetGrowth(String.format("%s%s net new", netDiff >= 0 ? "+" : "", formatCount(netDiff)));

            stats.setOrganicGrowth(formatNumber(netDiff));
            stats.setPaidGrowth("0");

            // Dynamically calculate peak velocity day from history
            FollowerHistory peakEntry = null;
            long peakDiff = 0;
            for (int i = 1; i < monthHistory.size(); i++) {
                long diff = monthHistory.get(i).getCurrentFollowers() - monthHistory.get(i - 1).getCurrentFollowers();
                if (diff > peakDiff) {
                    peakDiff = diff;
                    peakEntry = monthHistory.get(i);
                }
            }
            if (peakEntry != null) {
                stats.setPeakDay(String.format("%s (+%s)", peakEntry.getLabelText(), formatCount(peakDiff)));
            } else {
                stats.setPeakDay(monthHistory.get(monthHistory.size() - 1).getLabelText());
            }
        } else {
            stats.setFollowersGrowth("+0.0%");
            stats.setNetGrowth("0 net new");
            stats.setOrganicGrowth("0 (0%)");
            stats.setPaidGrowth("0 (0%)");
            stats.setPeakDay("None");
        }

        // 3. Total Engagement & Growth
        Long totalEngagement = postRepository.sumTotalEngagement();
        stats.setTotalEngagement(formatNumber(totalEngagement));
        stats.setEngagementGrowth("+0.0%");

        // Real interaction sums
        Long likes = postRepository.sumTotalLikes();
        Long comments = postRepository.sumTotalComments();
        Long shares = postRepository.sumTotalShares();
        stats.setTotalLikes(formatCount(likes));
        stats.setTotalComments(formatCount(comments));
        stats.setTotalShares(formatCount(shares));

        // 4. Published Posts Count & Growth
        long publishedCount = postRepository.countByStatus("PUBLISHED");
        stats.setPublishedPosts(publishedCount);
        LocalDateTime now = LocalDateTime.now();
        long publishedThisMonth = postRepository.countByStatusAndPublishedAtBetween("PUBLISHED", now.minusDays(30), now);
        long publishedLastMonth = postRepository.countByStatusAndPublishedAtBetween("PUBLISHED", now.minusDays(60), now.minusDays(30));
        double postGrowth = publishedLastMonth > 0
                ? ((double) (publishedThisMonth - publishedLastMonth) / publishedLastMonth) * 100
                : (publishedThisMonth > 0 ? 100.0 : 0.0);
        stats.setPublishedGrowth(String.format("%s%.1f%%", postGrowth >= 0 ? "+" : "", postGrowth));

        // 5. Scheduled Posts Count & Growth
        long scheduledCount = postRepository.countByStatus("SCHEDULED");
        stats.setScheduledPosts(scheduledCount);
        stats.setScheduledGrowth("+0.0%");

        // 6. Total Reach & Average Engagement Rate
        Long totalReach = postRepository.sumTotalReach();
        stats.setTotalReach(formatCount(totalReach));
        Double avgEng = accountRepository.averageEngagementRate();
        stats.setAvgEngagementRate(String.format("%.1f%%", avgEng != null ? avgEng : 0.0));

        // 7. Workspace capacity
        int connectedAccounts = (int) accountRepository.count();
        stats.setConnectedAccountsCount(connectedAccounts);
        stats.setMaxAccounts(maxAccounts);
        stats.setPlanUsagePercentage(maxAccounts > 0 ? (int) Math.min(100, Math.round(((double) connectedAccounts / maxAccounts) * 100)) : 0);

        return stats;
    }

    public List<SocialAccount> getAllConnectedAccounts() {
        return accountRepository.findByConnectedTrue();
    }

    public List<SocialAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public SocialAccount createSocialAccount(String platform, String handle, Long followersCount, String profileUrl) {
        String p = platform != null ? platform.trim() : "Instagram";
        String rawInput = handle != null ? handle.trim() : "@handle";
        String computedUrl = profileUrl != null ? profileUrl.trim() : "";
        String h = rawInput;

        // If user pasted a full URL into the handle field:
        if (rawInput.startsWith("http://") || rawInput.startsWith("https://")) {
            computedUrl = rawInput;
            try {
                String cleanPath = rawInput.replaceAll("/+$", "");
                int lastSlash = cleanPath.lastIndexOf('/');
                if (lastSlash != -1 && lastSlash < cleanPath.length() - 1) {
                    h = cleanPath.substring(lastSlash + 1);
                }
            } catch (Exception ignored) {}
        }

        if (!h.startsWith("@") && !p.equalsIgnoreCase("LinkedIn") && !p.equalsIgnoreCase("YouTube")) {
            h = "@" + h;
        }

        String badgeClass = "instagram";
        String brandColor = "#E1306C";
        String lower = p.toLowerCase();
        if (lower.contains("link")) {
            badgeClass = "linkedin";
            brandColor = "#0A66C2";
            p = "LinkedIn";
        } else if (lower.contains("twit") || lower.contains("x")) {
            badgeClass = "twitter";
            brandColor = "#1DA1F2";
            p = "X (Twitter)";
        } else if (lower.contains("you")) {
            badgeClass = "youtube";
            brandColor = "#FF0000";
            p = "YouTube";
        } else if (lower.contains("face")) {
            badgeClass = "facebook";
            brandColor = "#1877F2";
            p = "Facebook";
        } else if (lower.contains("tik")) {
            badgeClass = "tiktok";
            brandColor = "#000000";
            p = "TikTok";
        }

        SocialAccount sa = new SocialAccount(
                p,
                h,
                followersCount != null && followersCount >= 0 ? followersCount : 0L,
                0.0,
                0.0,
                0,
                badgeClass,
                brandColor,
                true
        );
        if (!computedUrl.isBlank()) {
            sa.setProfileUrl(computedUrl);
        }

        SocialAccount saved = accountRepository.save(sa);
        notificationRepository.save(new Notification(
                "Channel Connected",
                "Successfully connected real account: " + p + " (" + h + ")",
                "milestone"
        ));
        return saved;
    }

    public SocialAccount createSocialAccount(String platform, String handle, Long followersCount) {
        return createSocialAccount(platform, handle, followersCount, null);
    }

    public boolean toggleAccountConnection(Long id) {
        Optional<SocialAccount> opt = accountRepository.findById(id);
        if (opt.isPresent()) {
            SocialAccount sa = opt.get();
            sa.setConnected(!sa.isConnected());
            sa.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(sa);
            notificationRepository.save(new Notification(
                    "Account Status Updated",
                    sa.getPlatform() + " (" + sa.getHandle() + ") is now " + (sa.isConnected() ? "Active" : "Paused"),
                    "review"
            ));
            return true;
        }
        return false;
    }

    public boolean deleteSocialAccount(Long id) {
        Optional<SocialAccount> opt = accountRepository.findById(id);
        if (opt.isPresent()) {
            SocialAccount sa = opt.get();
            accountRepository.deleteById(id);
            notificationRepository.save(new Notification(
                    "Account Disconnected",
                    "Removed " + sa.getPlatform() + " (" + sa.getHandle() + ") from workspace.",
                    "review"
            ));
            return true;
        }
        return false;
    }

    public List<SocialPost> getRecentPosts(String platformFilter) {
        if (platformFilter != null && !platformFilter.equalsIgnoreCase("all")) {
            return postRepository.findByStatusAndPlatformClassOrderByPublishedAtDesc("PUBLISHED", platformFilter.toLowerCase());
        }
        return postRepository.findByStatusOrderByPublishedAtDesc("PUBLISHED");
    }

    public List<SocialPost> getFilteredPosts(String status, String platform) {
        List<SocialPost> all = postRepository.findAll();
        all.sort((a, b) -> {
            LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
            return bTime.compareTo(aTime);
        });
        return all.stream()
                .filter(p -> status == null || status.equalsIgnoreCase("all") || p.getStatus().equalsIgnoreCase(status))
                .filter(p -> platform == null || platform.equalsIgnoreCase("all") || p.getPlatformClass().equalsIgnoreCase(platform))
                .toList();
    }

    public List<SocialPost> getUpcomingScheduledPosts() {
        return postRepository.findByStatusOrderByScheduledAtAsc("SCHEDULED");
    }

    public ChartSeriesDto getFollowerGrowthData(String period) {
        String tag = (period == null || period.trim().isEmpty()) ? "30d" : period.toLowerCase();
        List<FollowerHistory> history = historyRepository.findByPeriodTagOrderByRecordedDateAsc(tag);

        List<String> labels = new ArrayList<>();
        List<Long> currentData = new ArrayList<>();
        List<Long> previousData = new ArrayList<>();

        for (FollowerHistory h : history) {
            labels.add(h.getLabelText());
            currentData.add(h.getCurrentFollowers());
            previousData.add(h.getPreviousFollowers());
        }

        return new ChartSeriesDto(labels, currentData, previousData);
    }

    public Map<String, Object> getEngagementOverviewData() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<String> labels = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        long[] likesArr = new long[7];
        long[] commentsArr = new long[7];
        long[] sharesArr = new long[7];

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<SocialPost> recentPublished = postRepository.findByStatusOrderByPublishedAtDesc("PUBLISHED");
        for (SocialPost post : recentPublished) {
            if (post.getPublishedAt() != null && post.getPublishedAt().isAfter(sevenDaysAgo)) {
                int dayIndex = post.getPublishedAt().getDayOfWeek().getValue() - 1;
                if (dayIndex >= 0 && dayIndex < 7) {
                    likesArr[dayIndex] += post.getLikesCount() != null ? post.getLikesCount() : 0L;
                    commentsArr[dayIndex] += post.getCommentsCount() != null ? post.getCommentsCount() : 0L;
                    sharesArr[dayIndex] += post.getSharesCount() != null ? post.getSharesCount() : 0L;
                }
            }
        }

        List<Long> likesList = new ArrayList<>();
        List<Long> commentsList = new ArrayList<>();
        List<Long> sharesList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            likesList.add(likesArr[i]);
            commentsList.add(commentsArr[i]);
            sharesList.add(sharesArr[i]);
        }

        data.put("labels", labels);
        data.put("likes", likesList);
        data.put("comments", commentsList);
        data.put("shares", sharesList);
        return data;
    }

    public List<SocialPost> createPost(CreatePostRequest req) {
        List<SocialPost> createdPosts = new ArrayList<>();
        List<String> channels = req.getChannels();
        if (channels == null || channels.isEmpty()) {
            channels = List.of("Instagram");
        }

        String option = req.getPublishOption() != null ? req.getPublishOption() : "schedule";
        LocalDateTime scheduledTime = null;
        LocalDateTime publishedTime = null;
        String status = "SCHEDULED";

        if ("now".equalsIgnoreCase(option)) {
            status = "PUBLISHED";
            publishedTime = LocalDateTime.now();
        } else if ("draft".equalsIgnoreCase(option)) {
            status = "DRAFT";
        } else {
            status = "SCHEDULED";
            if (req.getScheduledDateTime() != null && !req.getScheduledDateTime().trim().isEmpty()) {
                try {
                    scheduledTime = LocalDateTime.parse(req.getScheduledDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    scheduledTime = LocalDateTime.now().plusHours(4);
                }
            } else {
                scheduledTime = LocalDateTime.now().plusHours(4);
            }
        }

        String mediaType = (req.getMediaType() != null && !req.getMediaType().isEmpty()) ? req.getMediaType() : "Single Image + Caption";
        String content = req.getContent() != null ? req.getContent().trim() : "New scheduled social update";
        String title = content.length() > 50 ? content.substring(0, 47) + "..." : content;

        for (String ch : channels) {
            String platform = mapChannelName(ch);
            String platformClass = ch.toLowerCase().replaceAll("[^a-z]", "");
            if (platformClass.contains("twitter") || platformClass.contains("x")) platformClass = "twitter";
            if (platformClass.contains("insta")) platformClass = "instagram";
            if (platformClass.contains("link")) platformClass = "linkedin";
            if (platformClass.contains("you")) platformClass = "youtube";

            String handle = findHandleForPlatform(platformClass);

            SocialPost post = new SocialPost(
                    platform,
                    platformClass,
                    handle,
                    title,
                    content,
                    mediaType,
                    status,
                    scheduledTime,
                    publishedTime,
                    0L, 0L, 0L, 0L, 0.0
            );

            // Populate reminder notification preferences
            post.setRemindEmail(req.getRemindEmail() != null ? req.getRemindEmail() : true);
            post.setRemindSms(req.getRemindSms() != null ? req.getRemindSms() : true);
            post.setRecipientEmail(req.getRecipientEmail());
            post.setRecipientPhone(req.getRecipientPhone());
            post.setReminderMinutesBefore(req.getReminderMinutesBefore() != null ? req.getReminderMinutesBefore() : 15);

            if (req.getMediaUrl() != null && !req.getMediaUrl().isBlank()) {
                post.setMediaUrl(req.getMediaUrl().trim());
            }

            SocialPost saved = postRepository.save(post);
            createdPosts.add(saved);

            // Record a live dynamic notification in database
            if ("PUBLISHED".equals(status)) {
                notificationRepository.save(new Notification("New Post Published", "Your post on " + platform + " is now live.", "trend"));
            } else if ("SCHEDULED".equals(status)) {
                notificationRepository.save(new Notification("Post Queued", "Post queued for " + platform + " on " + saved.getFormattedScheduledDate(), "schedule"));
                // Dispatch instant confirmation alert via Email & SMS/MSG
                deliveryService.sendPostScheduledConfirmation(saved);
                saved.setConfirmationSent(true);
                postRepository.save(saved);
            }
        }

        return createdPosts;
    }

    public boolean publishPostNow(Long id) {
        Optional<SocialPost> optional = postRepository.findById(id);
        if (optional.isPresent()) {
            SocialPost post = optional.get();
            post.setStatus("PUBLISHED");
            post.setPublishedAt(LocalDateTime.now());
            post.setLikesCount(0L);
            post.setCommentsCount(0L);
            post.setSharesCount(0L);
            post.setReachCount(0L);
            post.setEngagementRate(0.0);
            postRepository.save(post);

            notificationRepository.save(new Notification("Post Published", "Scheduled post on " + post.getPlatform() + " published successfully.", "trend"));
            return true;
        }
        return false;
    }

    public boolean deletePost(Long id) {
        Optional<SocialPost> optional = postRepository.findById(id);
        if (optional.isPresent()) {
            SocialPost p = optional.get();
            postRepository.deleteById(id);
            notificationRepository.save(new Notification("Post Removed", "Scheduled update for " + p.getPlatform() + " was deleted.", "review"));
            return true;
        }
        return false;
    }

    // Dynamic Notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getUnreadNotificationCount() {
        return notificationRepository.countByUnreadTrue();
    }

    public void markAllNotificationsRead() {
        List<Notification> list = notificationRepository.findAll();
        for (Notification n : list) {
            n.setUnread(false);
        }
        notificationRepository.saveAll(list);
    }

    public boolean deleteNotification(Long id) {
        if (id != null && notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void clearAllNotifications() {
        notificationRepository.deleteAll();
    }

    private String formatNumber(Long number) {
        if (number == null || number == 0) return "0";
        return String.format("%,d", number);
    }

    private String formatCount(Long count) {
        if (count == null || count == 0) return "0";
        if (count >= 1_000_000) return String.format("%.1fM", count / 1_000_000.0);
        if (count >= 1_000) return String.format("%.1fK", count / 1_000.0);
        return String.valueOf(count);
    }

    private String mapChannelName(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("link")) return "LinkedIn";
        if (lower.contains("insta")) return "Instagram";
        if (lower.contains("twit") || lower.contains("x")) return "X (Twitter)";
        if (lower.contains("you")) return "YouTube";
        return raw;
    }

    private String findHandleForPlatform(String platformClass) {
        List<SocialAccount> accounts = accountRepository.findByConnectedTrue();
        for (SocialAccount sa : accounts) {
            if (sa.getBadgeClass() != null && sa.getBadgeClass().equalsIgnoreCase(platformClass)) {
                return sa.getHandle();
            }
        }
        return "@" + (platformClass != null ? platformClass.toLowerCase() : "channel");
    }

    // Global Search across Pages, Connected Accounts, and Social Posts
    public SearchResultDto search(String query) {
        if (query == null || query.trim().length() < 2) {
            return new SearchResultDto(query, 0, List.of(), List.of(), List.of());
        }

        String q = query.trim().toLowerCase();

        // 1. App Pages Navigation Search
        List<Map<String, Object>> matchedPages = new ArrayList<>();
        List<Map<String, String>> allPages = List.of(
            Map.of("title", "Dashboard Overview", "url", "/dashboard", "keywords", "dashboard overview metrics home followers engagement analytics posts", "icon", "dashboard"),
            Map.of("title", "Connected Social Accounts", "url", "/accounts", "keywords", "accounts channels social profiles handles connect link oauth facebook instagram twitter linkedin youtube tiktok", "icon", "accounts"),
            Map.of("title", "Content Management", "url", "/content", "keywords", "content posts feed draft published publishing updates caption", "icon", "content"),
            Map.of("title", "Content Calendar", "url", "/calendar", "keywords", "calendar schedule scheduled upcoming timetable dates plan planning", "icon", "calendar"),
            Map.of("title", "Analytics & Insights", "url", "/analytics", "keywords", "analytics performance growth charts audience reach impressions stats", "icon", "analytics"),
            Map.of("title", "Reports & Exports", "url", "/reports", "keywords", "reports summary export download csv pdf audit metrics", "icon", "reports"),
            Map.of("title", "Settings & Preferences", "url", "/settings", "keywords", "settings preferences profile admin password security account theme", "icon", "settings")
        );

        for (Map<String, String> p : allPages) {
            if (p.get("title").toLowerCase().contains(q) || p.get("keywords").toLowerCase().contains(q)) {
                matchedPages.add(Map.of(
                    "title", p.get("title"),
                    "url", p.get("url"),
                    "icon", p.get("icon")
                ));
            }
        }

        // 2. Connected Social Accounts Search
        List<Map<String, Object>> matchedAccounts = new ArrayList<>();
        List<SocialAccount> accounts = accountRepository.searchAccounts(q);
        for (SocialAccount a : accounts) {
            matchedAccounts.add(Map.of(
                "id", a.getId(),
                "platform", a.getPlatform(),
                "handle", a.getHandle() != null ? a.getHandle() : "",
                "profileUrl", a.getProfileUrl() != null ? a.getProfileUrl() : "/accounts",
                "badgeClass", a.getBadgeClass() != null ? a.getBadgeClass() : "social",
                "followers", a.getFollowersCount() != null ? formatCount(a.getFollowersCount()) : "0",
                "connected", a.isConnected()
            ));
        }

        // 3. Social Posts Search
        List<Map<String, Object>> matchedPosts = new ArrayList<>();
        List<SocialPost> posts = postRepository.searchPosts(q);
        for (SocialPost post : posts) {
            matchedPosts.add(Map.of(
                "id", post.getId(),
                "title", post.getTitle() != null ? post.getTitle() : "Post",
                "caption", post.getCaption() != null ? post.getCaption() : "",
                "platform", post.getPlatform() != null ? post.getPlatform() : "",
                "platformClass", post.getPlatformClass() != null ? post.getPlatformClass() : "",
                "status", post.getStatus() != null ? post.getStatus() : "PUBLISHED",
                "mediaType", post.getMediaType() != null ? post.getMediaType() : "Update",
                "mediaUrl", post.getMediaUrl() != null ? post.getMediaUrl() : ""
            ));
        }

        int total = matchedPages.size() + matchedAccounts.size() + matchedPosts.size();
        return new SearchResultDto(query, total, matchedPages, matchedAccounts, matchedPosts);
    }
}
