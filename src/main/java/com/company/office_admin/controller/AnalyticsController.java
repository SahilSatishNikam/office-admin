package com.company.office_admin.controller;

import com.company.office_admin.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final DashboardService dashboardService;

    public AnalyticsController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showAnalyticsPage(Model model) {
        model.addAttribute("pageTitle", "Analytics");
        model.addAttribute("activeMenu", "analytics");
        model.addAttribute("allAccounts", dashboardService.getAllAccounts());
        model.addAttribute("recentPosts", dashboardService.getRecentPosts("all"));
        return "analytics";
    }
}

