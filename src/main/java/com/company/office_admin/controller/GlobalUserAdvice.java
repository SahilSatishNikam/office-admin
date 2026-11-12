package com.company.office_admin.controller;

import com.company.office_admin.entity.User;
import com.company.office_admin.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserAdvice {

    private final UserRepository userRepository;

    public GlobalUserAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void populateUserData(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("userName", user.getFullName());
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userInitials", user.getAvatarInitials() != null ? user.getAvatarInitials() : "U");
                
                String roleAuthority = "ROLE_USER";
                if (!authentication.getAuthorities().isEmpty()) {
                    GrantedAuthority ga = authentication.getAuthorities().iterator().next();
                    roleAuthority = ga.getAuthority();
                }
                
                String displayRole = switch (roleAuthority) {
                    case "ROLE_ADMIN" -> "Master Admin";
                    case "ROLE_DESIGNER" -> "Creative Designer";
                    case "ROLE_CREATOR" -> "Content Creator";
                    default -> "Team Member";
                };

                model.addAttribute("userRole", displayRole);
                model.addAttribute("userRoleAuthority", roleAuthority);
            }
        }
    }
}

