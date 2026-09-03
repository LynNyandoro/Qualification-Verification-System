package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.VerificationRecord;
import com.mim736.qvs.domain.VerificationResult;

import java.time.Instant;

public class AuditRecordResponse {

    private Long id;
    private String credentialId;
    private String verificationCode;
    private String verifiedBy;
    private VerificationResult result;
    private String method;
    private String notes;
    private Instant verifiedAt;

    public static AuditRecordResponse from(VerificationRecord record) {
        AuditRecordResponse response = new AuditRecordResponse();
        response.id = record.getId();
        response.credentialId = record.getQualification() != null
                ? record.getQualification().getCredentialId()
                : record.getCredentialIdAttempted();
        response.verificationCode = record.getVerificationCodeAttempted();
        response.verifiedBy = record.getVerifiedBy().getUsername();
        response.result = record.getResult();
        response.method = record.getMethod();
        response.notes = record.getNotes();
        response.verifiedAt = record.getVerifiedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public VerificationResult getResult() {
        return result;
    }

    public String getMethod() {
        return method;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}
