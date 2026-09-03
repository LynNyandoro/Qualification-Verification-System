package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.VerificationResult;

public class VerifyResponse {

    private VerificationResult result;
    private String message;
    private QualificationResponse qualification;
    private boolean hashMatch;

    public VerifyResponse(VerificationResult result, String message, QualificationResponse qualification,
                          boolean hashMatch) {
        this.result = result;
        this.message = message;
        this.qualification = qualification;
        this.hashMatch = hashMatch;
    }

    public VerificationResult getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public QualificationResponse getQualification() {
        return qualification;
    }

    public boolean isHashMatch() {
        return hashMatch;
    }
}
