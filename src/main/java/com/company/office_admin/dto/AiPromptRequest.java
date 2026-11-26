package com.company.office_admin.dto;

import java.util.List;

public class AiPromptRequest {

    private String topic;
    private String tone; // professional, viral, casual, inspiring, promotional
    private String platform; // instagram, linkedin, twitter, youtube, general
    private List<String> channels;
    private String existingContent;

    public AiPromptRequest() {}

    public AiPromptRequest(String topic, String tone, String platform, List<String> channels) {
        this.topic = topic;
        this.tone = tone;
        this.platform = platform;
        this.channels = channels;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public String getExistingContent() {
        return existingContent;
    }

    public void setExistingContent(String existingContent) {
        this.existingContent = existingContent;
    }
}
