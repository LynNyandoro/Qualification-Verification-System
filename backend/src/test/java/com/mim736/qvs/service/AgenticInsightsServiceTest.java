package com.mim736.qvs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.domain.VerificationRecord;
import com.mim736.qvs.domain.VerificationResult;
import com.mim736.qvs.repo.VerificationRecordRepository;
import com.mim736.qvs.web.dto.AgentInsightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgenticInsightsServiceTest {

    @Mock
    private VerificationRecordRepository verificationRecordRepository;

    private AgenticInsightsService service;

    @BeforeEach
    void setUp() {
        MockEnvironment environment = new MockEnvironment();
        service = new AgenticInsightsService(
                verificationRecordRepository,
                new ObjectMapper(),
                environment
        );
    }

    @Test
    void returnsRuleBasedInsightsWithoutLlmKey() {
        UserAccount verifier = new UserAccount();
        verifier.setUsername("verifier");

        VerificationRecord valid = new VerificationRecord();
        valid.setVerifiedBy(verifier);
        valid.setMethod("CODE");
        valid.setResult(VerificationResult.VALID);

        VerificationRecord missing = new VerificationRecord();
        missing.setVerifiedBy(verifier);
        missing.setMethod("CANDIDATE_NAME");
        missing.setResult(VerificationResult.NOT_FOUND);

        when(verificationRecordRepository.findAllByOrderByVerifiedAtDesc())
                .thenReturn(List.of(valid, missing));

        AgentInsightResponse response = service.generateVerificationInsight("Highlight risk", 50);

        assertEquals("RULE_BASED", response.getMode());
        assertEquals(2L, response.getAnalysedEvents());
        assertEquals(1L, response.getFlaggedEvents());
        assertEquals(1L, response.getResultBreakdown().get("VALID"));
        assertEquals(1L, response.getResultBreakdown().get("NOT_FOUND"));
        assertTrue(response.getConfidenceScore() > 0);
        assertEquals("NO_BASELINE", response.getTrend().getDirection());
        assertFalse(response.getRecommendedActions().isEmpty());
        assertFalse(response.getDetectedRisks().isEmpty());
    }

    @Test
    void reportsNoDataWhenAuditTrailIsEmpty() {
        when(verificationRecordRepository.findAllByOrderByVerifiedAtDesc())
                .thenReturn(List.of());

        AgentInsightResponse response = service.generateVerificationInsight("", null);

        assertEquals("RULE_BASED", response.getMode());
        assertEquals(0L, response.getAnalysedEvents());
        assertEquals(0L, response.getFlaggedEvents());
        assertTrue(response.getConfidenceScore() > 0);
        assertEquals("NO_BASELINE", response.getTrend().getDirection());
        assertTrue(response.getSummary().contains("No audit events available yet"));
    }
}
