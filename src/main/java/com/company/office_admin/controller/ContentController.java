package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/content")
public class ContentController {

    private final DashboardService dashboardService;

    public ContentController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showContentPage(@RequestParam(required = false, defaultValue = "all") String status,
                                  @RequestParam(required = false, defaultValue = "all") String platform,
                                  Model model) {
        List<SocialPost> posts = dashboardService.getFilteredPosts(status, platform);

        model.addAttribute("pageTitle", "Content Management");
        model.addAttribute("activeMenu", "content");
        model.addAttribute("posts", posts);
        model.addAttribute("selectedStatus", status.toLowerCase());
        model.addAttribute("selectedPlatform", platform.toLowerCase());

        return "content";
    }

    @PostMapping("/{id}/publish")
    public String publishPostNow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean ok = dashboardService.publishPostNow(id);
        if (ok) {
            redirectAttributes.addFlashAttribute("successMessage", "Post published live to social feed.");
        }
        return "redirect:/content";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean ok = dashboardService.deletePost(id);
        if (ok) {
            redirectAttributes.addFlashAttribute("successMessage", "Post removed successfully.");
        }
        return "redirect:/content";
    }
}

