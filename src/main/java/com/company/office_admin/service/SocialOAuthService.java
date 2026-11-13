package com.company.office_admin.service;

import com.company.office_admin.entity.SocialAccount;
import com.company.office_admin.repository.SocialAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SocialOAuthService {

    private static final Logger log = LoggerFactory.getLogger(SocialOAuthService.class);

    private final SocialAccountRepository accountRepository;
    private final RestTemplate restTemplate;

    // LinkedIn
    @Value("${social.linkedin.client-id:}")
    private String linkedInClientId;
    @Value("${social.linkedin.client-secret:}")
    private String linkedInClientSecret;
    @Value("${social.linkedin.redirect-uri:http://localhost:8080/oauth2/callback/linkedin}")
    private String linkedInRedirectUri;

    // Instagram
    @Value("${social.instagram.client-id:}")
    private String instagramClientId;
    @Value("${social.instagram.client-secret:}")
    private String instagramClientSecret;
    @Value("${social.instagram.redirect-uri:http://localhost:8080/oauth2/callback/instagram}")
    private String instagramRedirectUri;

    // X (Twitter)
    @Value("${social.twitter.client-id:}")
    private String twitterClientId;
    @Value("${social.twitter.client-secret:}")
    private String twitterClientSecret;
    @Value("${social.twitter.redirect-uri:http://localhost:8080/oauth2/callback/twitter}")
    private String twitterRedirectUri;

    // Facebook
    @Value("${social.facebook.client-id:}")
    private String facebookClientId;
    @Value("${social.facebook.client-secret:}")
    private String facebookClientSecret;
    @Value("${social.facebook.redirect-uri:http://localhost:8080/oauth2/callback/facebook}")
    private String facebookRedirectUri;

    // YouTube (Google)
    @Value("${social.youtube.client-id:}")
    private String youtubeClientId;
    @Value("${social.youtube.client-secret:}")
    private String youtubeClientSecret;
    @Value("${social.youtube.redirect-uri:http://localhost:8080/oauth2/callback/youtube}")
    private String youtubeRedirectUri;

    // TikTok
    @Value("${social.tiktok.client-id:}")
    private String tiktokClientId;
    @Value("${social.tiktok.client-secret:}")
    private String tiktokClientSecret;
    @Value("${social.tiktok.redirect-uri:http://localhost:8080/oauth2/callback/tiktok}")
    private String tiktokRedirectUri;

    public SocialOAuthService(SocialAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.restTemplate = new RestTemplate();
    }

    public boolean isPlatformConfigured(String platform) {
        if (platform == null) return false;
        return switch (platform.toLowerCase()) {
            case "linkedin" -> isNotBlank(linkedInClientId) && isNotBlank(linkedInClientSecret);
            case "instagram" -> isNotBlank(instagramClientId) && isNotBlank(instagramClientSecret);
            case "twitter", "x" -> isNotBlank(twitterClientId) && isNotBlank(twitterClientSecret);
            case "facebook" -> isNotBlank(facebookClientId) && isNotBlank(facebookClientSecret);
            case "youtube" -> isNotBlank(youtubeClientId) && isNotBlank(youtubeClientSecret);
            case "tiktok" -> isNotBlank(tiktokClientId) && isNotBlank(tiktokClientSecret);
            default -> false;
        };
    }

    public String getRedirectUri(String platform) {
        if (platform == null) return "";
        return switch (platform.toLowerCase()) {
            case "linkedin" -> linkedInRedirectUri;
            case "instagram" -> instagramRedirectUri;
            case "twitter", "x" -> twitterRedirectUri;
            case "facebook" -> facebookRedirectUri;
            case "youtube" -> youtubeRedirectUri;
            case "tiktok" -> tiktokRedirectUri;
            default -> "http://localhost:8080/oauth2/callback/" + platform.toLowerCase();
        };
    }

    public String getDeveloperPortalUrl(String platform) {
        if (platform == null) return "https://developer.example.com";
        return switch (platform.toLowerCase()) {
            case "linkedin" -> "https://www.linkedin.com/developers/apps";
            case "instagram", "facebook" -> "https://developers.facebook.com/apps/";
            case "twitter", "x" -> "https://developer.x.com/en/portal/dashboard";
            case "youtube" -> "https://console.cloud.google.com/apis/credentials";
            case "tiktok" -> "https://developers.tiktok.com/";
            default -> "https://developer.example.com";
        };
    }

    public String buildAuthorizationUrl(String platform) {
        if (!isPlatformConfigured(platform)) {
            return null;
        }

        String norm = platform.toLowerCase();
        long state = System.currentTimeMillis();

        return switch (norm) {
            case "linkedin" -> "https://www.linkedin.com/oauth/v2/authorization?" +
                    "response_type=code" +
                    "&client_id=" + enc(linkedInClientId) +
                    "&redirect_uri=" + enc(linkedInRedirectUri) +
                    "&scope=" + enc("openid profile email w_member_social") +
                    "&state=linkedin_" + state;

            case "instagram" -> "https://www.facebook.com/v19.0/dialog/oauth?" +
                    "client_id=" + enc(instagramClientId) +
                    "&redirect_uri=" + enc(instagramRedirectUri) +
                    "&scope=" + enc("instagram_basic,pages_show_list,instagram_content_publish") +
                    "&response_type=code" +
                    "&state=instagram_" + state;

            case "twitter", "x" -> "https://twitter.com/i/oauth2/authorize?" +
                    "response_type=code" +
                    "&client_id=" + enc(twitterClientId) +
                    "&redirect_uri=" + enc(twitterRedirectUri) +
                    "&scope=" + enc("tweet.read tweet.write users.read offline.access") +
                    "&state=twitter_" + state +
                    "&code_challenge=challenge&code_challenge_method=plain";

            case "facebook" -> "https://www.facebook.com/v19.0/dialog/oauth?" +
                    "client_id=" + enc(facebookClientId) +
                    "&redirect_uri=" + enc(facebookRedirectUri) +
                    "&scope=" + enc("email,public_profile,pages_show_list,pages_read_engagement") +
                    "&response_type=code" +
                    "&state=facebook_" + state;

            case "youtube" -> "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "response_type=code" +
                    "&client_id=" + enc(youtubeClientId) +
                    "&redirect_uri=" + enc(youtubeRedirectUri) +
                    "&scope=" + enc("https://www.googleapis.com/auth/youtube.readonly https://www.googleapis.com/auth/userinfo.profile") +
                    "&access_type=offline" +
                    "&state=youtube_" + state;

            case "tiktok" -> "https://www.tiktok.com/v2/auth/authorize/?" +
                    "client_key=" + enc(tiktokClientId) +
                    "&scope=" + enc("user.info.basic,video.list,video.upload") +
                    "&response_type=code" +
                    "&redirect_uri=" + enc(tiktokRedirectUri) +
                    "&state=tiktok_" + state;

            default -> null;
        };
    }

    public SocialAccount handleCallback(String platform, String code) {
        String norm = platform.toLowerCase();
        return switch (norm) {
            case "linkedin" -> handleLinkedInCallback(code);
            case "instagram" -> handleInstagramCallback(code);
            case "twitter", "x" -> handleTwitterCallback(code);
            case "facebook" -> handleFacebookCallback(code);
            case "youtube" -> handleYouTubeCallback(code);
            case "tiktok" -> handleTikTokCallback(code);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleLinkedInCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", linkedInRedirectUri);
            body.add("client_id", linkedInClientId);
            body.add("client_secret", linkedInClientSecret);

            Map<String, Object> tokenRes = restTemplate.postForObject("https://www.linkedin.com/oauth/v2/accessToken", new HttpEntity<>(body, headers), Map.class);
            String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;

            HttpHeaders pHeaders = new HttpHeaders();
            pHeaders.setBearerAuth(accessToken);
            Map<String, Object> userInfo = restTemplate.exchange("https://api.linkedin.com/v2/userinfo", HttpMethod.GET, new HttpEntity<>(pHeaders), Map.class).getBody();

            String name = userInfo != null && userInfo.containsKey("name") ? (String) userInfo.get("name") : "LinkedIn User";
            String sub = userInfo != null ? String.valueOf(userInfo.get("sub")) : "li_" + System.currentTimeMillis();

            return saveOAuthAccount("LinkedIn", name, "linkedin", "#0a66c2", 0L, 0.0, 0.0, 0, accessToken, sub);
        } catch (Exception e) {
            log.error("LinkedIn OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("LinkedIn authentication failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleInstagramCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", instagramClientId);
            body.add("client_secret", instagramClientSecret);
            body.add("redirect_uri", instagramRedirectUri);
            body.add("code", code);

            Map<String, Object> tokenRes = restTemplate.postForObject("https://graph.facebook.com/v19.0/oauth/access_token", new HttpEntity<>(body, headers), Map.class);
            String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;

            String handle = null;
            String igId = "ig_" + System.currentTimeMillis();
            Long followers = 0L;

            // Attempt to retrieve linked Instagram Business Account details
            try {
                Map<String, Object> accountsRes = restTemplate.getForObject(
                        "https://graph.facebook.com/v19.0/me/accounts?fields=id,name,instagram_business_account{id,username,followers_count}&access_token=" + accessToken,
                        Map.class);
                if (accountsRes != null && accountsRes.containsKey("data")) {
                    List<Map<String, Object>> pages = (List<Map<String, Object>>) accountsRes.get("data");
                    if (pages != null) {
                        for (Map<String, Object> page : pages) {
                            if (page.containsKey("instagram_business_account")) {
                                Map<String, Object> igAcc = (Map<String, Object>) page.get("instagram_business_account");
                                if (igAcc != null) {
                                    if (igAcc.containsKey("username")) {
                                        handle = "@" + igAcc.get("username");
                                    }
                                    if (igAcc.containsKey("id")) {
                                        igId = String.valueOf(igAcc.get("id"));
                                    }
                                    if (igAcc.containsKey("followers_count") && igAcc.get("followers_count") instanceof Number) {
                                        followers = ((Number) igAcc.get("followers_count")).longValue();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception igEx) {
                log.warn("Could not query Instagram business account via me/accounts: {}", igEx.getMessage());
            }

            // Fallback to Meta user profile
            if (handle == null) {
                Map<String, Object> me = restTemplate.getForObject("https://graph.facebook.com/v19.0/me?fields=id,name&access_token=" + accessToken, Map.class);
                String name = me != null && me.containsKey("name") ? (String) me.get("name") : "Instagram User";
                if (me != null && me.containsKey("id")) {
                    igId = String.valueOf(me.get("id"));
                }
                handle = "@" + name.toLowerCase().replaceAll("\\s+", "_");
            }

            return saveOAuthAccount("Instagram", handle, "instagram", "#e1306c", followers, 0.0, 0.0, 0, accessToken, igId);
        } catch (Exception e) {
            log.error("Instagram OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("Instagram authentication failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleTwitterCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twitterClientId, twitterClientSecret);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", twitterRedirectUri);
            body.add("code_verifier", "challenge");

            Map<String, Object> tokenRes = restTemplate.postForObject("https://api.twitter.com/2/oauth2/token", new HttpEntity<>(body, headers), Map.class);
            String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;

            HttpHeaders pHeaders = new HttpHeaders();
            pHeaders.setBearerAuth(accessToken);
            Map<String, Object> userRes = restTemplate.exchange("https://api.twitter.com/2/users/me", HttpMethod.GET, new HttpEntity<>(pHeaders), Map.class).getBody();

            Map<String, Object> data = userRes != null ? (Map<String, Object>) userRes.get("data") : null;
            String username = data != null && data.containsKey("username") ? "@" + data.get("username") : "@x_user";
            String id = data != null ? String.valueOf(data.get("id")) : "tw_" + System.currentTimeMillis();

            return saveOAuthAccount("X (Twitter)", username, "twitter", "#0f1419", 0L, 0.0, 0.0, 0, accessToken, id);
        } catch (Exception e) {
            log.error("Twitter OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("Twitter / X authentication failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleFacebookCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", facebookClientId);
            body.add("client_secret", facebookClientSecret);
            body.add("redirect_uri", facebookRedirectUri);
            body.add("code", code);

            Map<String, Object> tokenRes = restTemplate.postForObject("https://graph.facebook.com/v19.0/oauth/access_token", new HttpEntity<>(body, headers), Map.class);
            String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;

            String name = null;
            String id = "fb_" + System.currentTimeMillis();
            Long followers = 0L;

            // Attempt to retrieve managed Facebook Page details
            try {
                Map<String, Object> accountsRes = restTemplate.getForObject(
                        "https://graph.facebook.com/v19.0/me/accounts?fields=id,name,fan_count,followers_count&access_token=" + accessToken,
                        Map.class);
                if (accountsRes != null && accountsRes.containsKey("data")) {
                    List<Map<String, Object>> pages = (List<Map<String, Object>>) accountsRes.get("data");
                    if (pages != null && !pages.isEmpty()) {
                        Map<String, Object> page = pages.get(0);
                        if (page.containsKey("name")) name = (String) page.get("name");
                        if (page.containsKey("id")) id = String.valueOf(page.get("id"));
                        if (page.containsKey("followers_count") && page.get("followers_count") instanceof Number) {
                            followers = ((Number) page.get("followers_count")).longValue();
                        } else if (page.containsKey("fan_count") && page.get("fan_count") instanceof Number) {
                            followers = ((Number) page.get("fan_count")).longValue();
                        }
                    }
                }
            } catch (Exception pEx) {
                log.warn("Could not query Facebook Page via me/accounts: {}", pEx.getMessage());
            }

            if (name == null) {
                Map<String, Object> me = restTemplate.getForObject("https://graph.facebook.com/v19.0/me?fields=id,name&access_token=" + accessToken, Map.class);
                name = me != null && me.containsKey("name") ? (String) me.get("name") : "Facebook Page";
                if (me != null && me.containsKey("id")) id = String.valueOf(me.get("id"));
            }

            return saveOAuthAccount("Facebook", name, "facebook", "#1877f2", followers, 0.0, 0.0, 0, accessToken, id);
        } catch (Exception e) {
            log.error("Facebook OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("Facebook authentication failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleYouTubeCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", youtubeRedirectUri);
            body.add("client_id", youtubeClientId);
            body.add("client_secret", youtubeClientSecret);

            Map<String, Object> tokenRes = restTemplate.postForObject("https://oauth2.googleapis.com/token", new HttpEntity<>(body, headers), Map.class);
            String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;

            HttpHeaders pHeaders = new HttpHeaders();
            pHeaders.setBearerAuth(accessToken);
            Map<String, Object> userInfo = restTemplate.exchange("https://www.googleapis.com/oauth2/v2/userinfo", HttpMethod.GET, new HttpEntity<>(pHeaders), Map.class).getBody();

            String name = userInfo != null && userInfo.containsKey("name") ? (String) userInfo.get("name") : "YouTube Channel";
            String id = userInfo != null ? String.valueOf(userInfo.get("id")) : "yt_" + System.currentTimeMillis();

            return saveOAuthAccount("YouTube", name, "youtube", "#ff0000", 0L, 0.0, 0.0, 0, accessToken, id);
        } catch (Exception e) {
            log.error("YouTube OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("YouTube authentication failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SocialAccount handleTikTokCallback(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_key", tiktokClientId);
            body.add("client_secret", tiktokClientSecret);
            body.add("code", code);
            body.add("grant_type", "authorization_code");
            body.add("redirect_uri", tiktokRedirectUri);

            Map<String, Object> tokenRes = restTemplate.postForObject("https://open.tiktokapis.com/v2/oauth/token/", new HttpEntity<>(body, headers), Map.class);
            Map<String, Object> data = tokenRes != null ? (Map<String, Object>) tokenRes.get("data") : null;
            String accessToken = data != null ? (String) data.get("access_token") : null;
            String openId = data != null ? (String) data.get("open_id") : "tiktok_" + System.currentTimeMillis();

            return saveOAuthAccount("TikTok", "@tiktok_user", "tiktok", "#010101", 0L, 0.0, 0.0, 0, accessToken, openId);
        } catch (Exception e) {
            log.error("TikTok OAuth failed: {}", e.getMessage(), e);
            throw new RuntimeException("TikTok authentication failed: " + e.getMessage());
        }
    }

    private SocialAccount saveOAuthAccount(String platform, String handle, String badgeClass, String brandColor,
                                          Long followers, Double growth, Double engagement, Integer posts,
                                          String accessToken, String externalId) {
        SocialAccount account = accountRepository.findByPlatformIgnoreCase(platform)
                .orElseGet(SocialAccount::new);

        account.setPlatform(platform);
        account.setHandle(handle);
        account.setBadgeClass(badgeClass);
        account.setBrandColor(brandColor);
        account.setConnected(true);
        account.setOauthConnected(true);
        account.setAccessToken(accessToken);
        account.setExternalId(externalId);
        if (followers != null && followers > 0) {
            account.setFollowersCount(followers);
        } else if (account.getFollowersCount() == null) {
            account.setFollowersCount(0L);
        }
        if (account.getGrowthRate() == null) account.setGrowthRate(growth);
        if (account.getEngagementRate() == null) account.setEngagementRate(engagement);
        if (account.getPostsCount() == null) account.setPostsCount(posts);
        account.setUpdatedAt(LocalDateTime.now());

        return accountRepository.save(account);
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    private String enc(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
