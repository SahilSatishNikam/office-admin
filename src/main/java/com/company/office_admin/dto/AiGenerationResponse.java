package com.company.office_admin.dto;

import java.util.List;

public class AiGenerationResponse {

    private String primaryCaption;
    private List<String> variations;
    private List<String> hashtags;
    private List<String> viralHooks;
    private String platform;
    private int estimatedCharCount;

    public AiGenerationResponse() {}

    public AiGenerationResponse(String primaryCaption, List<String> variations, List<String> hashtags, List<String> viralHooks, String platform) {
        this.primaryCaption = primaryCaption;
        this.variations = variations;
        this.hashtags = hashtags;
        this.viralHooks = viralHooks;
        this.platform = platform;
        this.estimatedCharCount = primaryCaption != null ? primaryCaption.length() : 0;
    }

    public String getPrimaryCaption() {
        return primaryCaption;
    }

    public void setPrimaryCaption(String primaryCaption) {
        this.primaryCaption = primaryCaption;
        this.estimatedCharCount = primaryCaption != null ? primaryCaption.length() : 0;
    }

    public List<String> getVariations() {
        return variations;
    }

    public void setVariations(List<String> variations) {
        this.variations = variations;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<String> getViralHooks() {
        return viralHooks;
    }

    public void setViralHooks(List<String> viralHooks) {
        this.viralHooks = viralHooks;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getEstimatedCharCount() {
        return estimatedCharCount;
    }

    public void setEstimatedCharCount(int estimatedCharCount) {
        this.estimatedCharCount = estimatedCharCount;
    }
}
