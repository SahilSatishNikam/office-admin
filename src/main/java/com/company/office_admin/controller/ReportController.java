package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final DashboardService dashboardService;

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showReportsPage(Model model) {
        List<SocialPost> allPosts = dashboardService.getFilteredPosts("all", "all");

        model.addAttribute("pageTitle", "Reports & Performance Summary");
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("allAccounts", dashboardService.getAllAccounts());
        model.addAttribute("allPosts", allPosts);

        return "reports";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"codemetric_report_" + System.currentTimeMillis() + ".csv\"");

        List<SocialPost> posts = dashboardService.getFilteredPosts("all", "all");
        PrintWriter writer = response.getWriter();

        // CSV Header
        writer.println("ID,Platform,Account Handle,Title,Status,Format,Date,Likes,Comments,Shares,Reach,Engagement Rate (%)");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (SocialPost p : posts) {
            String dateStr = p.getPublishedAt() != null ? p.getPublishedAt().format(fmt) :
                    (p.getScheduledAt() != null ? p.getScheduledAt().format(fmt) : "");
            String titleClean = p.getTitle() != null ? p.getTitle().replace("\"", "\"\"") : "";
            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%.1f",
                    p.getId(),
                    p.getPlatform() != null ? p.getPlatform() : "",
                    p.getAccountHandle() != null ? p.getAccountHandle() : "",
                    titleClean,
                    p.getStatus() != null ? p.getStatus() : "",
                    p.getMediaType() != null ? p.getMediaType() : "",
                    dateStr,
                    p.getLikesCount() != null ? p.getLikesCount() : 0,
                    p.getCommentsCount() != null ? p.getCommentsCount() : 0,
                    p.getSharesCount() != null ? p.getSharesCount() : 0,
                    p.getReachCount() != null ? p.getReachCount() : 0,
                    p.getEngagementRate() != null ? p.getEngagementRate() : 0.0
            ));
        }

        writer.flush();
    }
}

