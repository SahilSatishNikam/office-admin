package com.company.office_admin.controller;

import com.company.office_admin.entity.User;
import com.company.office_admin.repository.UserRepository;
import com.company.office_admin.security.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public SettingsController(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public String showSettingsPage(Model model, Authentication authentication) {
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        List<User> allUsers = userRepository.findAll();

        model.addAttribute("pageTitle", "Account & User Settings");
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("user", currentUser);
        model.addAttribute("allUsers", allUsers);

        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String role,
                                @RequestParam String avatarInitials,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("profileError", "User not found.");
            return "redirect:/settings";
        }

        String cleanUsername = username != null ? username.trim() : "";
        String cleanFullName = fullName != null ? fullName.trim() : "";
        String cleanEmail = email != null ? email.trim() : "";
        String cleanRole = role != null ? role.trim() : "Administrator";
        String cleanInitials = avatarInitials != null ? avatarInitials.trim().toUpperCase() : "AD";

        if (cleanUsername.length() < 3) {
            redirectAttributes.addFlashAttribute("profileError", "Username must be at least 3 characters.");
            return "redirect:/settings";
        }

        // Check if new username is already taken by another user
        Optional<User> existingUsername = userRepository.findByUsername(cleanUsername);
        if (existingUsername.isPresent() && !existingUsername.get().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("profileError", "Username '" + cleanUsername + "' is already in use by another user.");
            return "redirect:/settings";
        }

        // Check if new email is already taken by another user
        Optional<User> existingEmail = userRepository.findByEmail(cleanEmail);
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("profileError", "Email address '" + cleanEmail + "' is already in use by another user.");
            return "redirect:/settings";
        }

        boolean usernameChanged = !user.getUsername().equalsIgnoreCase(cleanUsername);

        user.setUsername(cleanUsername);
        user.setFullName(cleanFullName);
        user.setEmail(cleanEmail);
        user.setRole(cleanRole);
        user.setAvatarInitials(cleanInitials.length() > 3 ? cleanInitials.substring(0, 3) : cleanInitials);
        userRepository.save(user);

        // If login username changed, seamlessly update the SecurityContext so the active session stays logged in
        if (usernameChanged) {
            UserDetails updatedDetails = userDetailsService.loadUserByUsername(user.getUsername());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedDetails,
                    authentication.getCredentials(),
                    updatedDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully! Your login username has been changed to '" + user.getUsername() + "'.");
        } else {
            redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        }

        return "redirect:/settings";
    }

    @PostMapping("/notifications")
    public String updateNotificationSettings(@RequestParam(required = false) String email,
                                             @RequestParam(required = false) String phoneNumber,
                                             @RequestParam(required = false, defaultValue = "false") boolean emailEnabled,
                                             @RequestParam(required = false, defaultValue = "false") boolean smsEnabled,
                                             @RequestParam(required = false, defaultValue = "15") int reminderMinutesBefore,
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            if (email != null && !email.isBlank()) user.setEmail(email.trim());
            user.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : "");
            user.setEmailNotificationsEnabled(emailEnabled);
            user.setSmsNotificationsEnabled(smsEnabled);
            user.setReminderMinutesBefore(reminderMinutesBefore);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("notificationSuccess", "Email & SMS reminder settings updated successfully!");
        }
        return "redirect:/settings";
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("passwordError", "User not found.");
            return "redirect:/settings";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Current password is incorrect.");
            return "redirect:/settings";
        }

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be at least 6 characters.");
            return "redirect:/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New password and confirmation do not match.");
            return "redirect:/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("passwordSuccess", "Password updated successfully in database.");
        return "redirect:/settings";
    }

    @PostMapping("/users/create")
    public String createNewUser(@RequestParam String newUsername,
                                @RequestParam String newPassword,
                                @RequestParam String newFullName,
                                @RequestParam String newEmail,
                                @RequestParam(defaultValue = "Admin") String newRole,
                                @RequestParam(required = false) String newAvatarInitials,
                                RedirectAttributes redirectAttributes) {
        String cleanUsername = newUsername != null ? newUsername.trim() : "";
        String cleanFullName = newFullName != null ? newFullName.trim() : "";
        String cleanEmail = newEmail != null ? newEmail.trim() : "";
        String cleanRole = (newRole != null && !newRole.trim().isEmpty()) ? newRole.trim() : "Administrator";

        if (cleanUsername.length() < 3) {
            redirectAttributes.addFlashAttribute("userCreateError", "Username must be at least 3 characters.");
            return "redirect:/settings";
        }

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("userCreateError", "Password must be at least 6 characters.");
            return "redirect:/settings";
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            redirectAttributes.addFlashAttribute("userCreateError", "Username '" + cleanUsername + "' is already taken.");
            return "redirect:/settings";
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            redirectAttributes.addFlashAttribute("userCreateError", "Email '" + cleanEmail + "' is already registered.");
            return "redirect:/settings";
        }

        String initials = newAvatarInitials != null ? newAvatarInitials.trim().toUpperCase() : "";
        if (initials.isEmpty() && !cleanFullName.isEmpty()) {
            String[] parts = cleanFullName.split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                initials = cleanFullName.substring(0, Math.min(2, cleanFullName.length())).toUpperCase();
            }
        }
        if (initials.isEmpty()) initials = "US";

        User newUser = new User(
                cleanUsername,
                passwordEncoder.encode(newPassword),
                cleanFullName,
                cleanEmail,
                cleanRole,
                initials.length() > 3 ? initials.substring(0, 3) : initials
        );
        newUser.setEnabled(true);
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("userCreateSuccess", "New user '" + cleanUsername + "' (" + cleanFullName + ") created successfully! They can now sign in.");
        return "redirect:/settings";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("userDeleteError", "You cannot delete your own active user account.");
            return "redirect:/settings";
        }

        Optional<User> toDelete = userRepository.findById(id);
        if (toDelete.isPresent()) {
            String deletedUsername = toDelete.get().getUsername();
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("userDeleteSuccess", "User account '" + deletedUsername + "' deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("userDeleteError", "User not found.");
        }

        return "redirect:/settings";
    }
}
