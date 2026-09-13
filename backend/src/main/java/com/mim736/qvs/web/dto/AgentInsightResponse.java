package com.mim736.qvs.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AgentInsightResponse {

    private String mode;
    private String model;
    private String prompt;
    private String summary;
    private List<String> recommendedActions;
    private List<String> detectedRisks;
    private Map<String, Long> resultBreakdown;
    private Map<String, Long> methodBreakdown;
    private long analysedEvents;
    private long flaggedEvents;
    private int confidenceScore;
    private TrendWindow trend;
    private String generatedAt;
    private String note;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public List<String> getDetectedRisks() {
        return detectedRisks;
    }

    public void setDetectedRisks(List<String> detectedRisks) {
        this.detectedRisks = detectedRisks;
    }

    public Map<String, Long> getResultBreakdown() {
        return resultBreakdown;
    }

    public void setResultBreakdown(Map<String, Long> resultBreakdown) {
        this.resultBreakdown = resultBreakdown;
    }

    public Map<String, Long> getMethodBreakdown() {
        return methodBreakdown;
    }

    public void setMethodBreakdown(Map<String, Long> methodBreakdown) {
        this.methodBreakdown = methodBreakdown;
    }

    public long getAnalysedEvents() {
        return analysedEvents;
    }

    public void setAnalysedEvents(long analysedEvents) {
        this.analysedEvents = analysedEvents;
    }

    public long getFlaggedEvents() {
        return flaggedEvents;
    }

    public void setFlaggedEvents(long flaggedEvents) {
        this.flaggedEvents = flaggedEvents;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public TrendWindow getTrend() {
        return trend;
    }

    public void setTrend(TrendWindow trend) {
        this.trend = trend;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt == null ? null : generatedAt.toString();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static class TrendWindow {
        private double currentFlagRate;
        private double previousFlagRate;
        private double delta;
        private String direction;
        private String summary;

        public double getCurrentFlagRate() {
            return currentFlagRate;
        }

        public void setCurrentFlagRate(double currentFlagRate) {
            this.currentFlagRate = currentFlagRate;
        }

        public double getPreviousFlagRate() {
            return previousFlagRate;
        }

        public void setPreviousFlagRate(double previousFlagRate) {
            this.previousFlagRate = previousFlagRate;
        }

        public double getDelta() {
            return delta;
        }

        public void setDelta(double delta) {
            this.delta = delta;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}
