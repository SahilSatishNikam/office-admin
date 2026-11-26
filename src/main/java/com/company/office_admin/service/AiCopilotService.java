package com.company.office_admin.service;

import com.company.office_admin.dto.AiGenerationResponse;
import com.company.office_admin.dto.AiPromptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiCopilotService {

    private static final Logger log = LoggerFactory.getLogger(AiCopilotService.class);

    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public AiCopilotService() {
        this.restTemplate = new RestTemplate();
    }

    public AiCopilotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    /**
     * Generates platform-tailored social media captions, variations, and hooks.
     */
    public AiGenerationResponse generateCaptions(AiPromptRequest req) {
        String topic = req.getTopic() != null ? req.getTopic().trim() : "Exciting company updates and industry insights";
        String tone = req.getTone() != null ? req.getTone().trim().toLowerCase() : "professional";
        String platform = resolvePlatform(req);

        // Try Google Gemini AI first if API key is configured
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                AiGenerationResponse geminiRes = callGeminiApi(topic, tone, platform, req.getExistingContent());
                if (geminiRes != null && geminiRes.getPrimaryCaption() != null && !geminiRes.getPrimaryCaption().isBlank()) {
                    return geminiRes;
                }
            } catch (Exception e) {
                log.warn("Gemini API call failed or timed out. Falling back to algorithmic engine: {}", e.getMessage());
            }
        }

        // Smart Built-in Social Copy Generator (Zero External Dependencies)
        return generateAlgorithmicContent(topic, tone, platform, req.getExistingContent());
    }

    /**
     * Generates a curated list of relevant hashtags for a topic or content piece.
     */
    public List<String> suggestHashtags(String topicOrContent, String platform) {
        String text = topicOrContent != null ? topicOrContent.toLowerCase() : "";
        String p = platform != null ? platform.toLowerCase() : "general";

        Set<String> tags = new LinkedHashSet<>();

        // 1. Topic-driven keyword tags
        if (text.contains("ai") || text.contains("artificial intelligence") || text.contains("machine learning")) {
            tags.addAll(List.of("#AI", "#ArtificialIntelligence", "#MachineLearning", "#TechTrends", "#Innovation", "#FutureOfTech"));
        }
        if (text.contains("product") || text.contains("launch") || text.contains("feature") || text.contains("release")) {
            tags.addAll(List.of("#ProductLaunch", "#NewFeature", "#TechLaunch", "#Innovation", "#ProductUpdates", "#BuildingInPublic"));
        }
        if (text.contains("code") || text.contains("developer") || text.contains("software") || text.contains("tech")) {
            tags.addAll(List.of("#SoftwareEngineering", "#CodingLife", "#DevCommunity", "#TechCareers", "#WebDev", "#CleanCode"));
        }
        if (text.contains("design") || text.contains("ui") || text.contains("ux") || text.contains("creative")) {
            tags.addAll(List.of("#UIDesign", "#UXDesign", "#DesignSystem", "#CreativeDirection", "#ProductDesign", "#DesignInspo"));
        }
        if (text.contains("growth") || text.contains("marketing") || text.contains("startup") || text.contains("business")) {
            tags.addAll(List.of("#StartupGrowth", "#DigitalMarketing", "#BusinessStrategy", "#Entrepreneurship", "#Leadership", "#ScaleUp"));
        }
        if (text.contains("hiring") || text.contains("job") || text.contains("career") || text.contains("team")) {
            tags.addAll(List.of("#Hiring", "#JobOpening", "#JoinOurTeam", "#CareersInTech", "#CompanyCulture", "#WorkWithUs"));
        }

        // 2. Platform-specific tags
        if (p.contains("insta")) {
            tags.addAll(List.of("#InstaDaily", "#ContentCreator", "#ExplorePage", "#ViralPost", "#DigitalCreatives"));
        } else if (p.contains("link")) {
            tags.addAll(List.of("#ProfessionalNetworking", "#ThoughtLeadership", "#CareerDevelopment", "#IndustryInsights"));
        } else if (p.contains("twit") || p.contains("x")) {
            tags.addAll(List.of("#TechTwitter", "#BuildInPublic", "#TrendingNow"));
        }

        // Default baseline tags if set is sparse
        if (tags.size() < 6) {
            tags.addAll(List.of("#SocialMediaMarketing", "#DigitalStrategy", "#Trending", "#BrandGrowth", "#CreativeStudio"));
        }

        return new ArrayList<>(tags);
    }

    /**
     * Generates high-converting viral hooks for social media posts.
     */
    public List<String> generateViralHooks(String topic, String tone) {
        String t = topic != null && !topic.isBlank() ? topic : "productivity";
        List<String> hooks = new ArrayList<>();

        if ("viral".equalsIgnoreCase(tone) || "casual".equalsIgnoreCase(tone)) {
            hooks.add("🚨 Stop doing " + t + " the old way. Here is what changed:");
            hooks.add("99% of people get " + t + " wrong. Here’s the 1% secret:");
            hooks.add("I tested 10 different ways to approach " + t + ". Here is what actually worked:");
            hooks.add("The biggest mistake I see in " + t + " (and how to fix it in 2 minutes):");
            hooks.add("Bookmark this before you start your next " + t + " project 📌");
        } else if ("promotional".equalsIgnoreCase(tone)) {
            hooks.add("Big news! We just released something special for " + t + " 🔥");
            hooks.add("Say goodbye to messy workflows. Meet the new standard for " + t + ":");
            hooks.add("Limited release: everything you need for " + t + " is now live!");
            hooks.add("What if you could master " + t + " in half the time? Now you can.");
        } else {
            // Professional / Inspiring
            hooks.add("Three critical insights about " + t + " that every team should know:");
            hooks.add("The landscape of " + t + " is evolving rapidly. Here is our perspective:");
            hooks.add("Leadership lessons learned while executing our latest initiative on " + t + ":");
            hooks.add("Why " + t + " matters more today than ever before:");
            hooks.add("A simple framework we use to consistently deliver high-impact results in " + t + ":");
        }

        return hooks;
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    private String resolvePlatform(AiPromptRequest req) {
        if (req.getPlatform() != null && !req.getPlatform().isBlank()) {
            return req.getPlatform().toLowerCase();
        }
        if (req.getChannels() != null && !req.getChannels().isEmpty()) {
            return req.getChannels().get(0).toLowerCase();
        }
        return "instagram";
    }

    private AiGenerationResponse callGeminiApi(String topic, String tone, String platform, String existingContent) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

        String systemPrompt = String.format(
                "You are an expert social media strategist and viral copywriter. " +
                "Write a high-converting, engaging social media post for platform: %s with tone: %s. " +
                "Topic: %s. %s" +
                "Provide the response in the following exact format:\n" +
                "PRIMARY_CAPTION:\n[Main post text including emojis and line breaks]\n" +
                "VARIATION_1:\n[Shorter punchy alternative]\n" +
                "VARIATION_2:\n[Question-based interactive alternative]\n" +
                "HASHTAGS:\n[5-8 relevant hashtags separated by space]\n" +
                "HOOKS:\n[2 strong first-line hooks]",
                platform, tone, topic,
                (existingContent != null && !existingContent.isBlank()) ? "Incorporate or refine this draft: " + existingContent + ". " : ""
        );

        Map<String, Object> part = Map.of("text", systemPrompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(content),
                "generationConfig", Map.of(
                        "temperature", 0.75,
                        "maxOutputTokens", 1200
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            List candidates = (List) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map first = (Map) candidates.get(0);
                Map c = (Map) first.get("content");
                if (c != null) {
                    List parts = (List) c.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map p = (Map) parts.get(0);
                        String text = (String) p.get("text");
                        return parseGeminiResponse(text, platform);
                    }
                }
            }
        }
        return null;
    }

    private AiGenerationResponse parseGeminiResponse(String rawText, String platform) {
        String primary = "";
        List<String> variations = new ArrayList<>();
        List<String> hashtags = new ArrayList<>();
        List<String> hooks = new ArrayList<>();

        if (rawText == null || rawText.isBlank()) {
            return null;
        }

        // Extract PRIMARY_CAPTION
        Matcher mPrimary = Pattern.compile("PRIMARY_CAPTION:\\s*([\\s\\S]*?)(?=VARIATION_1:|VARIATION_2:|HASHTAGS:|HOOKS:|$)", Pattern.CASE_INSENSITIVE).matcher(rawText);
        if (mPrimary.find()) primary = mPrimary.group(1).trim();

        // Extract VARIATION_1
        Matcher mV1 = Pattern.compile("VARIATION_1:\\s*([\\s\\S]*?)(?=VARIATION_2:|HASHTAGS:|HOOKS:|$)", Pattern.CASE_INSENSITIVE).matcher(rawText);
        if (mV1.find()) variations.add(mV1.group(1).trim());

        // Extract VARIATION_2
        Matcher mV2 = Pattern.compile("VARIATION_2:\\s*([\\s\\S]*?)(?=HASHTAGS:|HOOKS:|$)", Pattern.CASE_INSENSITIVE).matcher(rawText);
        if (mV2.find()) variations.add(mV2.group(1).trim());

        // Extract HASHTAGS
        Matcher mTags = Pattern.compile("HASHTAGS:\\s*([\\s\\S]*?)(?=HOOKS:|$)", Pattern.CASE_INSENSITIVE).matcher(rawText);
        if (mTags.find()) {
            String[] tags = mTags.group(1).trim().split("\\s+");
            for (String t : tags) {
                if (t.startsWith("#")) hashtags.add(t);
                else if (!t.isBlank()) hashtags.add("#" + t);
            }
        }

        // Extract HOOKS
        Matcher mHooks = Pattern.compile("HOOKS:\\s*([\\s\\S]*?)$", Pattern.CASE_INSENSITIVE).matcher(rawText);
        if (mHooks.find()) {
            String[] lines = mHooks.group(1).trim().split("\n");
            for (String line : lines) {
                String clean = line.replaceFirst("^[-*•\\d.]+\\s*", "").trim();
                if (!clean.isBlank()) hooks.add(clean);
            }
        }

        if (primary.isBlank()) {
            primary = rawText.trim();
        }

        return new AiGenerationResponse(primary, variations, hashtags, hooks, platform);
    }

    private AiGenerationResponse generateAlgorithmicContent(String topic, String tone, String platform, String existingContent) {
        String p = platform != null ? platform.toLowerCase() : "instagram";
        String t = tone != null ? tone.toLowerCase() : "professional";

        List<String> hooks = generateViralHooks(topic, tone);
        String selectedHook = hooks.get(0);
        List<String> hashtags = suggestHashtags(topic, platform);

        StringBuilder primary = new StringBuilder();
        List<String> variations = new ArrayList<>();

        if (p.contains("link")) {
            // LinkedIn Style: Professional, spaced paragraphs, clear lessons, discussion CTA
            primary.append(selectedHook).append("\n\n");
            primary.append("Here are 3 key takeaways our team learned this week:\n\n");
            primary.append("1️⃣ Consistency compounds — daily execution beats intermittent perfection.\n");
            primary.append("2️⃣ Feedback loops matter — listen to real audience signals early and often.\n");
            primary.append("3️⃣ Clarity drives results — keep the message simple, direct, and actionable.\n\n");
            primary.append("How are you approaching ").append(topic.toLowerCase()).append(" in your current projects?\n\n");
            primary.append("Let's discuss below 👇\n\n");
            primary.append(String.join(" ", hashtags.subList(0, Math.min(5, hashtags.size()))));

            // Short variation
            variations.add("Quick thought on " + topic + ":\n\nThe teams that win aren't the ones with the most ideas—they are the ones that execute with relentless focus.\n\nAgree or disagree?");

        } else if (p.contains("twit") || p.contains("x")) {
            // X / Twitter Style: Crisp, punchy, under 260 chars
            primary.append(selectedHook).append("\n\n");
            primary.append("Focus on quality, iterate fast, and let the data guide your next move.\n\n");
            primary.append(String.join(" ", hashtags.subList(0, Math.min(3, hashtags.size()))));

            // Short variation
            variations.add("The secret to " + topic + " in 2026? Simplify before you scale.\n\nRT if you agree 🚀");

        } else if (p.contains("you")) {
            // YouTube Community Style
            primary.append("📢 Community Update: ").append(topic).append("!\n\n");
            primary.append("We've been hard at work behind the scenes and wanted to share what's coming next.\n\n");
            primary.append("Drop your thoughts or questions in the comments, and don't forget to stay tuned for our next release!\n\n");
            primary.append(String.join(" ", hashtags.subList(0, Math.min(4, hashtags.size()))));

            variations.add("What topic would you like us to cover next around " + topic + "? Vote in the comments below! 👇");

        } else {
            // Instagram / Facebook Style: Visually engaging, emojis, clear CTA, hashtag group
            primary.append(selectedHook).append("\n\n");
            primary.append("When it comes to ").append(topic.toLowerCase()).append(", small improvements add up to major breakthroughs. ✨\n\n");
            primary.append("Here's your reminder:\n");
            primary.append("• Set clear priorities\n");
            primary.append("• Track what moves the needle\n");
            primary.append("• Celebrate progress along the way\n\n");
            primary.append("Double tap if this resonates ❤️ | Save this post for later 📌\n\n");
            primary.append(".\n.\n");
            primary.append(String.join(" ", hashtags.subList(0, Math.min(8, hashtags.size()))));

            // Variation
            variations.add("Everything you need to know about " + topic + " in one frame. 📸\n\nSave this for your next planning session! 📌");
        }

        return new AiGenerationResponse(primary.toString(), variations, hashtags, hooks, platform);
    }
}
