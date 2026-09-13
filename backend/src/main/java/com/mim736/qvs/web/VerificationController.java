package com.mim736.qvs.web;

import com.mim736.qvs.service.AgenticInsightsService;
import com.mim736.qvs.service.VerificationService;
import com.mim736.qvs.web.dto.AgentInsightRequest;
import com.mim736.qvs.web.dto.AgentInsightResponse;
import com.mim736.qvs.web.dto.AuditRecordResponse;
import com.mim736.qvs.web.dto.VerifyRequest;
import com.mim736.qvs.web.dto.VerifyResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final VerificationService verificationService;
    private final AgenticInsightsService agenticInsightsService;

    public VerificationController(
            VerificationService verificationService,
            AgenticInsightsService agenticInsightsService
    ) {
        this.verificationService = verificationService;
        this.agenticInsightsService = agenticInsightsService;
    }

    @PostMapping("/verify")
    public VerifyResponse verify(@RequestBody VerifyRequest request, Authentication authentication) {
        return verificationService.verify(request, authentication.getName());
    }

    @GetMapping("/audit")
    @PreAuthorize("hasAnyRole('ADMIN','VERIFIER','ISSUER')")
    public List<AuditRecordResponse> audit() {
        return verificationService.auditTrail();
    }

    @GetMapping("/reports/verification")
    @PreAuthorize("hasAnyRole('ADMIN','VERIFIER')")
    public Map<String, Object> report() {
        return verificationService.verificationReport();
    }

    @PostMapping("/ai/verification-insights")
    @PreAuthorize("hasAnyRole('ADMIN','VERIFIER','ISSUER')")
    public AgentInsightResponse insight(
            @RequestBody(required = false) AgentInsightRequest request
    ) {
        String prompt = request == null ? null : request.getPrompt();
        Integer maxEvents = request == null ? null : request.getMaxEvents();
        return agenticInsightsService.generateVerificationInsight(prompt, maxEvents);
    }
}
