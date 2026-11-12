package com.company.office_admin.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.company.office_admin.dto.DashboardStatsDto;
import com.company.office_admin.entity.Notification;
import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.entity.User;
import com.company.office_admin.repository.UserRepository;
import com.company.office_admin.service.DashboardService;
import com.company.office_admin.service.FolderService;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final FolderService folderService;

    public GlobalModelAttributes(UserRepository userRepository,
                                 DashboardService dashboardService,
                                 FolderService folderService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.folderService = folderService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        String userName = "Admin";
        String userLogin = "admin";
        String userRole = "Master Admin";
        String userRoleAuthority = "ROLE_ADMIN";
        String userEmail = "admin@company.com";
        String userInitials = "AD";
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (currentUser != null) {
                userLogin = currentUser.getUsername();
                userName = currentUser.getFullName();
                userEmail = currentUser.getEmail();
                userInitials = (currentUser.getAvatarInitials() != null && !currentUser.getAvatarInitials().isBlank())
                        ? currentUser.getAvatarInitials() : "AD";
            }

            if (!authentication.getAuthorities().isEmpty()) {
                GrantedAuthority ga = authentication.getAuthorities().iterator().next();
                userRoleAuthority = ga.getAuthority();
            }

            userRole = switch (userRoleAuthority) {
                case "ROLE_ADMIN" -> "Master Admin";
                case "ROLE_DESIGNER" -> "Creative Designer";
                case "ROLE_CREATOR" -> "Content Creator";
                default -> (currentUser != null && currentUser.getRole() != null) ? currentUser.getRole() : "Team Member";
            };
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userLogin", userLogin);
        model.addAttribute("userName", userName);
        model.addAttribute("userRole", userRole);
        model.addAttribute("userRoleAuthority", userRoleAuthority);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userInitials", userInitials);

        DashboardStatsDto stats = dashboardService.getDashboardStats();
        List<SocialAccount> socialAccounts = dashboardService.getAllConnectedAccounts();
        List<Notification> notifications = dashboardService.getAllNotifications();
        long unreadCount = dashboardService.getUnreadNotificationCount();

        model.addAttribute("stats", stats);
        model.addAttribute("socialAccounts", socialAccounts);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationCount", unreadCount);
        model.addAttribute("folders", folderService.getAllFolders());
    }
}
