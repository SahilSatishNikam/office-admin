package com.company.office_admin.controller;

import com.company.office_admin.dto.DashboardStatsDto;
import com.company.office_admin.entity.Notification;
import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.entity.User;
import com.company.office_admin.repository.UserRepository;
import com.company.office_admin.service.DashboardService;
import com.company.office_admin.service.FolderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final FolderService folderService;

    public GlobalControllerAdvice(UserRepository userRepository,
                                  DashboardService dashboardService,
                                  FolderService folderService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.folderService = folderService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User u = userOpt.get();
                model.addAttribute("userName", u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
                model.addAttribute("userEmail", u.getEmail());
                model.addAttribute("userInitials", u.getAvatarInitials() != null && !u.getAvatarInitials().isBlank() ? u.getAvatarInitials() : "AD");
                model.addAttribute("userRole", u.getRole());
            } else {
                model.addAttribute("userName", username);
                model.addAttribute("userEmail", username + "@company.com");
                model.addAttribute("userInitials", username.substring(0, Math.min(2, username.length())).toUpperCase());
                model.addAttribute("userRole", "Administrator");
            }

            String roleAuth = "ROLE_ADMIN";
            if (!auth.getAuthorities().isEmpty()) {
                roleAuth = auth.getAuthorities().iterator().next().getAuthority();
            }
            model.addAttribute("userRoleAuthority", roleAuth);
            model.addAttribute("currentRole", roleAuth);
        }

        // Live dynamic notifications, stats, accounts, and folders across all pages
        List<Notification> notifications = dashboardService.getAllNotifications();
        long unreadCount = dashboardService.getUnreadNotificationCount();
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationCount", unreadCount);

        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);

        List<SocialAccount> accounts = dashboardService.getAllConnectedAccounts();
        model.addAttribute("socialAccounts", accounts);
        model.addAttribute("folders", folderService.getAllFolders());
    }
}
