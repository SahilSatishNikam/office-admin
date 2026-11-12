package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/accounts")
public class AccountsController {

    private final DashboardService dashboardService;

    public AccountsController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showAccountsPage(Model model, Authentication auth) {
        String role = "ROLE_ADMIN";
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            role = auth.getAuthorities().iterator().next().getAuthority();
        }
        model.addAttribute("currentRole", role);
        model.addAttribute("userRoleAuthority", role);
        model.addAttribute("pageTitle", "Connected Social Accounts");
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("allAccounts", dashboardService.getAllAccounts());
        model.addAttribute("socialAccounts", dashboardService.getAllConnectedAccounts());
        model.addAttribute("stats", dashboardService.getDashboardStats());
        return "accounts";
    }

    @PostMapping("/create")
    public String createAccount(@RequestParam String platform,
                                @RequestParam String handle,
                                @RequestParam(defaultValue = "0") Long followersCount,
                                RedirectAttributes redirectAttributes) {
        try {
            SocialAccount created = dashboardService.createSocialAccount(platform, handle, followersCount);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account " + created.getHandle() + " on " + created.getPlatform() + " connected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to connect account: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/toggle")
    public String toggleAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean toggled = dashboardService.toggleAccountConnection(id);
        if (toggled) {
            redirectAttributes.addFlashAttribute("successMessage", "Account connection status updated.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found.");
        }
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = dashboardService.deleteSocialAccount(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Account disconnected from workspace.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found.");
        }
        return "redirect:/accounts";
    }
}

