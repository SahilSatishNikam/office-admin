package com.company.office_admin.controller;

import com.company.office_admin.dto.ChartSeriesDto;
import com.company.office_admin.dto.CreatePostRequest;
import com.company.office_admin.dto.DashboardStatsDto;
import com.company.office_admin.dto.SearchResultDto;
import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;
    private final com.company.office_admin.service.NotificationDeliveryService deliveryService;

    public DashboardApiController(DashboardService dashboardService,
                                  com.company.office_admin.service.NotificationDeliveryService deliveryService) {
        this.dashboardService = dashboardService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<SocialAccount>> getAccounts() {
        return ResponseEntity.ok(dashboardService.getAllConnectedAccounts());
    }

    @GetMapping("/charts/follower-growth")
    public ResponseEntity<ChartSeriesDto> getFollowerGrowth(@RequestParam(defaultValue = "30d") String period) {
        return ResponseEntity.ok(dashboardService.getFollowerGrowthData(period));
    }

    @GetMapping("/charts/engagement")
    public ResponseEntity<Map<String, Object>> getEngagementOverview() {
        return ResponseEntity.ok(dashboardService.getEngagementOverviewData());
    }

    @GetMapping("/posts/recent")
    public ResponseEntity<List<SocialPost>> getRecentPosts(@RequestParam(defaultValue = "all") String platform) {
        return ResponseEntity.ok(dashboardService.getRecentPosts(platform));
    }

    @GetMapping("/posts/scheduled")
    public ResponseEntity<List<SocialPost>> getScheduledPosts() {
        return ResponseEntity.ok(dashboardService.getUpcomingScheduledPosts());
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody CreatePostRequest request) {
        List<SocialPost> saved = dashboardService.createPost(request);
        DashboardStatsDto updatedStats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post created and saved successfully!",
                "createdCount", saved.size(),
                "posts", saved,
                "stats", updatedStats
        ));
    }

    @PostMapping("/posts/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishNow(@PathVariable Long id) {
        boolean success = dashboardService.publishPostNow(id);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Post published!"));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Post not found"));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long id) {
        boolean success = dashboardService.deletePost(id);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications() {
        return ResponseEntity.ok(Map.of(
                "notifications", dashboardService.getAllNotifications(),
                "unreadCount", dashboardService.getUnreadNotificationCount()
        ));
    }

    @PutMapping("/notifications/mark-read")
    public ResponseEntity<Map<String, Object>> markNotificationsRead() {
        dashboardService.markAllNotificationsRead();
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        boolean deleted = dashboardService.deleteNotification(id);
        long newUnreadCount = dashboardService.getUnreadNotificationCount();
        return ResponseEntity.ok(Map.of(
                "success", deleted,
                "message", deleted ? "Notification permanently deleted" : "Notification not found",
                "unreadCount", newUnreadCount
        ));
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<Map<String, Object>> clearAllNotifications() {
        dashboardService.clearAllNotifications();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All notifications permanently cleared from database",
                "unreadCount", 0
        ));
    }

    @PostMapping("/notifications/test")
    public ResponseEntity<Map<String, Object>> testNotification(@RequestBody(required = false) Map<String, String> payload) {
        String email = payload != null ? payload.get("email") : null;
        String phone = payload != null ? payload.get("phone") : null;
        return ResponseEntity.ok(deliveryService.sendTestNotification(email, phone));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResultDto> search(@RequestParam(value = "q", defaultValue = "") String query) {
        return ResponseEntity.ok(dashboardService.search(query));
    }
}

