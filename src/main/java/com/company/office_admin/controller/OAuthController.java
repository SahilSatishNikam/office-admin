package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.service.SocialOAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/oauth2")
public class OAuthController {

    private final SocialOAuthService oauthService;

    public OAuthController(SocialOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/authorize/{platform}")
    public String initiateAuthorization(@PathVariable String platform, RedirectAttributes redirectAttributes) {
        String norm = platform.toLowerCase();
        String displayName = getDisplayName(norm);

        if (!oauthService.isPlatformConfigured(norm)) {
            String redirectUri = oauthService.getRedirectUri(norm);
            String portalUrl = oauthService.getDeveloperPortalUrl(norm);

            redirectAttributes.addFlashAttribute("errorMessage",
                    displayName + " API credentials not configured yet. Please add your Client ID and Secret in application.properties or environment variables.");
            redirectAttributes.addFlashAttribute("setupPlatform", displayName);
            redirectAttributes.addFlashAttribute("setupRedirectUri", redirectUri);
            redirectAttributes.addFlashAttribute("setupPortalUrl", portalUrl);
            return "redirect:/accounts";
        }

        String authUrl = oauthService.buildAuthorizationUrl(norm);
        if (authUrl == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to build OAuth URL for " + displayName);
            return "redirect:/accounts";
        }

        return "redirect:" + authUrl;
    }

    @GetMapping("/callback/{platform}")
    public String handleCallback(@PathVariable String platform,
                                 @RequestParam(required = false) String code,
                                 @RequestParam(required = false) String error,
                                 @RequestParam(required = false) String error_description,
                                 RedirectAttributes redirectAttributes) {
        String displayName = getDisplayName(platform);

        if (error != null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    displayName + " authorization cancelled or failed: " + (error_description != null ? error_description : error));
            return "redirect:/dashboard";
        }
        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Missing authorization code from " + displayName + " callback.");
            return "redirect:/dashboard";
        }

        try {
            SocialAccount account = oauthService.handleCallback(platform, code);
            redirectAttributes.addFlashAttribute("successMessage",
                    displayName + " account (" + account.getHandle() + ") successfully connected via live OAuth 2.0!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    private String getDisplayName(String platform) {
        if (platform == null) return "Social Platform";
        return switch (platform.toLowerCase()) {
            case "linkedin" -> "LinkedIn";
            case "instagram" -> "Instagram";
            case "twitter", "x" -> "X (Twitter)";
            case "facebook" -> "Facebook";
            case "youtube" -> "YouTube";
            case "tiktok" -> "TikTok";
            default -> platform;
        };
    }
}
