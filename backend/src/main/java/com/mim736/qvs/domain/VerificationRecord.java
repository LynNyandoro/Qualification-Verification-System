package com.mim736.qvs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "verification_records")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id")
    private Qualification qualification;

    @Column(length = 36)
    private String credentialIdAttempted;

    @Column(length = 24)
    private String verificationCodeAttempted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by", nullable = false)
    private UserAccount verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationResult result;

    @Column(nullable = false, length = 20)
    private String method;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private Instant verifiedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Qualification getQualification() {
        return qualification;
    }

    public void setQualification(Qualification qualification) {
        this.qualification = qualification;
    }

    public String getCredentialIdAttempted() {
        return credentialIdAttempted;
    }

    public void setCredentialIdAttempted(String credentialIdAttempted) {
        this.credentialIdAttempted = credentialIdAttempted;
    }

    public String getVerificationCodeAttempted() {
        return verificationCodeAttempted;
    }

    public void setVerificationCodeAttempted(String verificationCodeAttempted) {
        this.verificationCodeAttempted = verificationCodeAttempted;
    }

    public UserAccount getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(UserAccount verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public VerificationResult getResult() {
        return result;
    }

    public void setResult(VerificationResult result) {
        this.result = result;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
