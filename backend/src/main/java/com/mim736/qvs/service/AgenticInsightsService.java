package com.mim736.qvs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mim736.qvs.domain.VerificationRecord;
import com.mim736.qvs.domain.VerificationResult;
import com.mim736.qvs.repo.VerificationRecordRepository;
import com.mim736.qvs.web.dto.AgentInsightResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgenticInsightsService {

    private static final Set<VerificationResult> FLAGGED_RESULTS = EnumSet.of(
            VerificationResult.REVOKED,
            VerificationResult.EXPIRED,
            VerificationResult.TAMPERED,
            VerificationResult.NOT_FOUND,
            VerificationResult.INVALID
    );
    private static final String DEFAULT_PROMPT = "Identify high-risk verification patterns and recommend next actions.";
    private static final int DEFAULT_MAX_EVENTS = 120;

    private final VerificationRecordRepository verificationRecordRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AgenticInsightsService(
            VerificationRecordRepository verificationRecordRepository,
            ObjectMapper objectMapper,
            Environment environment
    ) {
        this.verificationRecordRepository = verificationRecordRepository;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @Transactional(readOnly = true)
    public AgentInsightResponse generateVerificationInsight(String prompt, Integer maxEvents) {
        String effectivePrompt = isBlank(prompt) ? DEFAULT_PROMPT : prompt.trim();
        int limit = clamp(maxEvents == null ? DEFAULT_MAX_EVENTS : maxEvents, 20, 250);
        List<VerificationRecord> allEvents = verificationRecordRepository.findAllByOrderByVerifiedAtDesc();
        List<VerificationRecord> events = allEvents.stream()
                .limit(limit)
                .toList();
        List<VerificationRecord> previousEvents = allEvents.stream()
                .skip(limit)
                .limit(limit)
                .toList();

        Map<String, Long> resultBreakdown = events.stream()
                .map(VerificationRecord::getResult)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Enum::name, LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> methodBreakdown = events.stream()
                .map(VerificationRecord::getMethod)
                .map(method -> isBlank(method) ? "UNKNOWN" : method)
                .collect(Collectors.groupingBy(method -> method, LinkedHashMap::new, Collectors.counting()));

        long flagged = events.stream()
                .map(VerificationRecord::getResult)
                .filter(Objects::nonNull)
                .filter(FLAGGED_RESULTS::contains)
                .count();

        long previousFlagged = previousEvents.stream()
                .map(VerificationRecord::getResult)
                .filter(Objects::nonNull)
                .filter(FLAGGED_RESULTS::contains)
                .count();
        AgentInsightResponse.TrendWindow trend = buildTrendWindow(
                events.size(),
                flagged,
                previousEvents.size(),
                previousFlagged
        );

        List<String> detectedRisks = detectRisks(events, resultBreakdown, methodBreakdown, trend);
        List<String> recommendedActions = recommendActions(events, resultBreakdown, methodBreakdown, detectedRisks);
        int confidenceScore = calculateConfidenceScore(events.size(), methodBreakdown.size(), trend);
        String summary = buildSummary(events.size(), flagged, resultBreakdown, methodBreakdown, confidenceScore, trend);

        AgentInsightResponse response = new AgentInsightResponse();
        response.setMode("RULE_BASED");
        response.setModel("local-heuristics");
        response.setPrompt(effectivePrompt);
        response.setSummary(summary);
        response.setDetectedRisks(detectedRisks);
        response.setRecommendedActions(recommendedActions);
        response.setResultBreakdown(resultBreakdown);
        response.setMethodBreakdown(methodBreakdown);
        response.setAnalysedEvents(events.size());
        response.setFlaggedEvents(flagged);
        response.setConfidenceScore(confidenceScore);
        response.setTrend(trend);
        response.setGeneratedAt(Instant.now());
        response.setNote("Local mode is used when no LLM key is configured.");

        LlmSettings llmSettings = resolveLlmSettings();
        if (llmSettings == null) {
            return response;
        }

        try {
            LlmInsight llm = requestLlmInsight(
                    llmSettings.endpoint(),
                    llmSettings.apiKey(),
                    llmSettings.model(),
                    effectivePrompt,
                    response
            );
            response.setMode("LLM_ASSISTED");
            response.setModel(llmSettings.model());
            if (!isBlank(llm.summary())) {
                response.setSummary(llm.summary());
            }
            if (!llm.detectedRisks().isEmpty()) {
                response.setDetectedRisks(llm.detectedRisks());
            }
            if (!llm.recommendedActions().isEmpty()) {
                response.setRecommendedActions(llm.recommendedActions());
            }
            response.setNote("LLM summary generated from aggregate metrics and recent events.");
        } catch (Exception ex) {
            response.setNote("Fell back to local heuristics because LLM call failed: " + ex.getMessage());
        }
        return response;
    }

    private LlmSettings resolveLlmSettings() {
        String groqKey = environment.getProperty("qvs.ai.groq-api-key", "");
        if (!isBlank(groqKey)) {
            String model = firstNonBlank(
                    environment.getProperty("qvs.ai.model", ""),
                    environment.getProperty("qvs.ai.groq-model", "openai/gpt-oss-20b")
            );
            String endpoint = environment.getProperty(
                    "qvs.ai.groq-endpoint",
                    "https://api.groq.com/openai/v1/chat/completions"
            );
            return new LlmSettings(groqKey.trim(), endpoint, model);
        }
        String openaiKey = environment.getProperty("qvs.ai.openai-api-key", "");
        if (!isBlank(openaiKey)) {
            String model = firstNonBlank(environment.getProperty("qvs.ai.model", ""), "gpt-4o-mini");
            String endpoint = environment.getProperty(
                    "qvs.ai.openai-endpoint",
                    "https://api.openai.com/v1/chat/completions"
            );
            return new LlmSettings(openaiKey.trim(), endpoint, model);
        }
        return null;
    }

    private String firstNonBlank(String preferred, String fallback) {
        return isBlank(preferred) ? fallback : preferred.trim();
    }

    private LlmInsight requestLlmInsight(
            String endpoint,
            String apiKey,
            String model,
            String prompt,
            AgentInsightResponse baseline
    ) throws IOException, InterruptedException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("temperature", 0.2);
        payload.put("response_format", Map.of("type", "json_object"));
        payload.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are an audit-risk copilot. Return strict JSON with keys: summary,detectedRisks,recommendedActions."),
                Map.of("role", "user", "content", buildLlmInput(prompt, baseline))
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IOException("LLM request failed with HTTP " + response.statusCode()
                    + ": " + response.body().substring(0, Math.min(180, response.body().length())));
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode messageContent = root.path("choices").path(0).path("message").path("content");
        if (messageContent.isMissingNode() || messageContent.isNull()) {
            throw new IOException("LLM response content was empty");
        }

        JsonNode parsed = objectMapper.readTree(messageContent.asText());
        String summary = parsed.path("summary").asText("");
        List<String> risks = toStringList(parsed.path("detectedRisks"));
        List<String> actions = toStringList(parsed.path("recommendedActions"));
        return new LlmInsight(summary, risks, actions);
    }

    private String buildLlmInput(String prompt, AgentInsightResponse baseline) throws IOException {
        Map<String, Object> context = Map.of(
                "prompt", prompt,
                "summary", baseline.getSummary(),
                "resultBreakdown", baseline.getResultBreakdown(),
                "methodBreakdown", baseline.getMethodBreakdown(),
                "flaggedEvents", baseline.getFlaggedEvents(),
                "analysedEvents", baseline.getAnalysedEvents(),
                "confidenceScore", baseline.getConfidenceScore(),
                "trend", baseline.getTrend(),
                "detectedRisks", baseline.getDetectedRisks(),
                "recommendedActions", baseline.getRecommendedActions()
        );
        return objectMapper.writeValueAsString(context);
    }

    private List<String> toStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("").trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private List<String> detectRisks(
            List<VerificationRecord> events,
            Map<String, Long> resultBreakdown,
            Map<String, Long> methodBreakdown,
            AgentInsightResponse.TrendWindow trend
    ) {
        if (events.isEmpty()) {
            return List.of("No verification events are available yet, so risk profiling is not statistically reliable.");
        }

        List<String> risks = new ArrayList<>();
        long total = events.size();
        long tampered = resultBreakdown.getOrDefault(VerificationResult.TAMPERED.name(), 0L);
        long notFound = resultBreakdown.getOrDefault(VerificationResult.NOT_FOUND.name(), 0L);
        long revoked = resultBreakdown.getOrDefault(VerificationResult.REVOKED.name(), 0L);
        double notFoundRate = ((double) notFound / total) * 100.0;

        if (tampered > 0) {
            risks.add("Detected " + tampered + " tampered credential checks that need registrar investigation.");
        }
        if (notFoundRate >= 35.0 && total >= 10) {
            risks.add("High NOT_FOUND rate (" + Math.round(notFoundRate) + "%) suggests invalid submissions or abuse attempts.");
        }
        if (revoked > 0) {
            risks.add("Revoked credentials are still being presented (" + revoked + " attempts), requiring verifier reminders.");
        }

        List<Map.Entry<String, Long>> heavyFailureActors = events.stream()
                .filter(event -> event.getVerifiedBy() != null)
                .filter(event -> event.getResult() != null && FLAGGED_RESULTS.contains(event.getResult()))
                .collect(Collectors.groupingBy(event -> event.getVerifiedBy().getUsername(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() >= 4)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();
        if (!heavyFailureActors.isEmpty()) {
            Map.Entry<String, Long> top = heavyFailureActors.get(0);
            risks.add("User " + top.getKey() + " generated " + top.getValue()
                    + " flagged outcomes; review account activity and source documents.");
        }

        String dominantMethod = methodBreakdown.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
        if ("CANDIDATE_NAME".equals(dominantMethod)) {
            risks.add("Most checks rely on candidate-name matching, which can increase false positives without code confirmation.");
        }
        if ("WORSENING".equals(trend.getDirection())) {
            risks.add("Flagged-result trend is worsening compared with the previous window; increase manual reviews.");
        }
        return risks.isEmpty()
                ? List.of("No critical anomaly spike was detected in the analysed verification window.")
                : risks;
    }

    private List<String> recommendActions(
            List<VerificationRecord> events,
            Map<String, Long> resultBreakdown,
            Map<String, Long> methodBreakdown,
            List<String> detectedRisks
    ) {
        if (events.isEmpty()) {
            return List.of(
                    "Run at least 10 verification checks before requesting AI risk analytics.",
                    "Ask issuers to register newly completed cohorts so baseline data is available."
            );
        }

        List<String> actions = new ArrayList<>();
        long tampered = resultBreakdown.getOrDefault(VerificationResult.TAMPERED.name(), 0L);
        long notFound = resultBreakdown.getOrDefault(VerificationResult.NOT_FOUND.name(), 0L);
        long codeBased = methodBreakdown.getOrDefault("CODE", 0L) + methodBreakdown.getOrDefault("CREDENTIAL_ID", 0L);

        if (tampered > 0) {
            actions.add("Escalate all tampered outcomes to issuer admins and cross-check stored hashes against source records.");
        }
        if (notFound > 0) {
            actions.add("Add a pre-check step: run candidate-name search before manual code entry to reduce NOT_FOUND volume.");
        }
        if (codeBased < events.size() / 2L) {
            actions.add("Encourage code or credential-ID verification as primary method for stronger matching confidence.");
        }

        actions.add("Review flagged events in the audit table daily and revoke compromised credentials immediately.");
        actions.add("Use the AI insight prompt each week and compare trend changes with the previous period.");

        if (detectedRisks.stream().anyMatch(risk -> risk.contains("No critical anomaly"))) {
            actions.add(0, "Maintain current controls and keep sampling random VALID outcomes for manual spot-checking.");
        }
        return actions.stream().distinct().toList();
    }

    private String buildSummary(
            int analysedEvents,
            long flaggedEvents,
            Map<String, Long> resultBreakdown,
            Map<String, Long> methodBreakdown,
            int confidenceScore,
            AgentInsightResponse.TrendWindow trend
    ) {
        if (analysedEvents == 0) {
            return "No audit events available yet. Run verification flows first, then request insights again.";
        }

        String topResult = resultBreakdown.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("n/a");
        String topMethod = methodBreakdown.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("n/a");
        long valid = resultBreakdown.getOrDefault(VerificationResult.VALID.name(), 0L);
        long validRate = Math.round(((double) valid / analysedEvents) * 100.0);
        return "Analysed " + analysedEvents + " recent checks: " + validRate + "% were VALID, "
                + flaggedEvents + " were flagged. Dominant result is " + topResult
                + " and dominant method is " + topMethod + ". Confidence score is "
                + confidenceScore + "%. Trend: " + trend.getSummary();
    }

    private AgentInsightResponse.TrendWindow buildTrendWindow(
            int currentSize,
            long currentFlagged,
            int previousSize,
            long previousFlagged
    ) {
        double currentRate = roundedPercent(currentFlagged, currentSize);
        double previousRate = roundedPercent(previousFlagged, previousSize);
        double delta = roundToOneDecimal(currentRate - previousRate);

        String direction;
        if (previousSize == 0) {
            direction = "NO_BASELINE";
        } else if (Math.abs(delta) < 2.0) {
            direction = "STABLE";
        } else if (delta > 0) {
            direction = "WORSENING";
        } else {
            direction = "IMPROVING";
        }

        AgentInsightResponse.TrendWindow trend = new AgentInsightResponse.TrendWindow();
        trend.setCurrentFlagRate(currentRate);
        trend.setPreviousFlagRate(previousRate);
        trend.setDelta(delta);
        trend.setDirection(direction);
        trend.setSummary(buildTrendSummary(direction, currentRate, previousRate, delta, previousSize));
        return trend;
    }

    private String buildTrendSummary(
            String direction,
            double currentRate,
            double previousRate,
            double delta,
            int previousSize
    ) {
        if (previousSize == 0) {
            return "No previous window baseline yet.";
        }
        if ("STABLE".equals(direction)) {
            return "Flagged rate is stable at " + currentRate + "% (previous " + previousRate + "%).";
        }
        String trendWord = "IMPROVING".equals(direction) ? "down" : "up";
        return "Flagged rate is " + trendWord + " by " + Math.abs(delta) + " points ("
                + previousRate + "% -> " + currentRate + "%).";
    }

    private int calculateConfidenceScore(
            int analysedEvents,
            int uniqueMethods,
            AgentInsightResponse.TrendWindow trend
    ) {
        double sampleFactor = Math.min(1.0, analysedEvents / 60.0);
        double diversityFactor = Math.min(1.0, uniqueMethods / 3.0);
        double stabilityFactor;
        if ("NO_BASELINE".equals(trend.getDirection())) {
            stabilityFactor = 0.65;
        } else {
            stabilityFactor = Math.max(0.0, 1.0 - (Math.abs(trend.getDelta()) / 60.0));
        }
        int score = (int) Math.round((sampleFactor * 0.6 + diversityFactor * 0.2 + stabilityFactor * 0.2) * 100.0);
        return clamp(score, 25, 99);
    }

    private double roundedPercent(long part, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return roundToOneDecimal((part * 100.0) / total);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record LlmSettings(String apiKey, String endpoint, String model) {
    }

    private record LlmInsight(String summary, List<String> detectedRisks, List<String> recommendedActions) {
    }
}
