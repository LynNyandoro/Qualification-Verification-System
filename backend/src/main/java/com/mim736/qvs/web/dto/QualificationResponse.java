package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;

import java.time.Instant;
import java.time.LocalDate;

public class QualificationResponse {

    private Long id;
    private String credentialId;
    private String verificationCode;
    private String holderName;
    private String holderNationalId;
    private String holderUsername;
    private String title;
    private QualificationType type;
    private String issuingInstitution;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Integer nqfLevel;
    private QualificationStatus status;
    private String credentialHash;
    private String registeredBy;
    private Instant createdAt;

    public static QualificationResponse from(Qualification qualification) {
        QualificationResponse response = new QualificationResponse();
        response.id = qualification.getId();
        response.credentialId = qualification.getCredentialId();
        response.verificationCode = qualification.getVerificationCode();
        response.holderName = qualification.getHolderName();
        response.holderNationalId = qualification.getHolderNationalId();
        response.holderUsername = qualification.getHolderUsername();
        response.title = qualification.getTitle();
        response.type = qualification.getType();
        response.issuingInstitution = qualification.getIssuingInstitution();
        response.issueDate = qualification.getIssueDate();
        response.expiryDate = qualification.getExpiryDate();
        response.nqfLevel = qualification.getNqfLevel();
        response.status = qualification.getStatus();
        response.credentialHash = qualification.getCredentialHash();
        response.registeredBy = qualification.getRegisteredBy().getUsername();
        response.createdAt = qualification.getCreatedAt();
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

    public String getHolderName() {
        return holderName;
    }

    public String getHolderNationalId() {
        return holderNationalId;
    }

    public String getHolderUsername() {
        return holderUsername;
    }

    public String getTitle() {
        return title;
    }

    public QualificationType getType() {
        return type;
    }

    public String getIssuingInstitution() {
        return issuingInstitution;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Integer getNqfLevel() {
        return nqfLevel;
    }

    public QualificationStatus getStatus() {
        return status;
    }

    public String getCredentialHash() {
        return credentialHash;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
