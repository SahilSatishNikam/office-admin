package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.service.DashboardService;
import com.company.office_admin.service.FolderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final FolderService folderService;

    public DashboardController(DashboardService dashboardService, FolderService folderService) {
        this.dashboardService = dashboardService;
        this.folderService = folderService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model, org.springframework.security.core.Authentication auth) {
        String role = "ROLE_ADMIN";
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority();
        }
        model.addAttribute("currentRole", role);
        model.addAttribute("userRoleAuthority", role);

        List<SocialPost> recentPosts = dashboardService.getRecentPosts("all");
        List<SocialPost> scheduledPosts = dashboardService.getUpcomingScheduledPosts();

        String pageTitle = "ROLE_DESIGNER".equals(role) ? "Designer Studio Vault" :
                ("ROLE_CREATOR".equals(role) ? "Content Creator Studio" : "Social Media Manager Dashboard");

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeMenu", "overview");
        model.addAttribute("stats", dashboardService.getDashboardStats());
        model.addAttribute("socialAccounts", dashboardService.getAllConnectedAccounts());
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("scheduledPosts", scheduledPosts);
        model.addAttribute("folders", folderService.getAllFolders());
        model.addAttribute("designerFolder", folderService.getDesignerFolder());
        model.addAttribute("designerRecords", folderService.getDesignerRecords());
        model.addAttribute("creatorFolder", folderService.getCreatorFolder());
        model.addAttribute("creatorRecords", folderService.getCreatorRecords());

        return "dashboard";
    }
}
