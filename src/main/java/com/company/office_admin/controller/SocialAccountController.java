package com.company.office_admin.controller;

import com.company.office_admin.service.DashboardService;
import com.company.office_admin.service.SocialOAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounts")
public class SocialAccountController {

    private final DashboardService dashboardService;
    private final SocialOAuthService oauthService;

    public SocialAccountController(DashboardService dashboardService, SocialOAuthService oauthService) {
        this.dashboardService = dashboardService;
        this.oauthService = oauthService;
    }

    @GetMapping
    public String showAccountsPage(Model model) {
        model.addAttribute("pageTitle", "Social Accounts");
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("allAccounts", dashboardService.getAllAccounts());
        return "accounts";
    }

    @PostMapping("/create")
    public String createAccount(@RequestParam String platform,
                                @RequestParam String handle,
                                @RequestParam(required = false, defaultValue = "0") Long followersCount,
                                @RequestParam(required = false) String profileUrl,
                                RedirectAttributes redirectAttributes) {
        dashboardService.createSocialAccount(platform, handle, followersCount, profileUrl);
        redirectAttributes.addFlashAttribute("successMessage", "Connected real account: " + platform + " (" + handle + ")");
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/toggle")
    public String toggleAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean ok = dashboardService.toggleAccountConnection(id);
        if (ok) {
            redirectAttributes.addFlashAttribute("successMessage", "Account connection status updated.");
        }
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean ok = dashboardService.deleteSocialAccount(id);
        if (ok) {
            redirectAttributes.addFlashAttribute("successMessage", "Account successfully disconnected and removed.");
        }
        return "redirect:/accounts";
    }
}

