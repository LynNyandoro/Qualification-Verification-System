package com.mim736.qvs.web.dto;

public class AgentInsightRequest {

    private String prompt;
    private Integer maxEvents;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(Integer maxEvents) {
        this.maxEvents = maxEvents;
    }
}
