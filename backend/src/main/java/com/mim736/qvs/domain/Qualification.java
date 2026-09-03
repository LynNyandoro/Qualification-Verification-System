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
import java.time.LocalDate;

@Entity
@Table(name = "qualifications")
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 48)
    private String credentialId;

    @Column(nullable = false, unique = true, length = 48)
    private String verificationCode;

    @Column(nullable = false, length = 160)
    private String holderName;

    @Column(length = 40)
    private String holderNationalId;

    @Column(length = 80)
    private String holderUsername;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QualificationType type;

    @Column(nullable = false, length = 200)
    private String issuingInstitution;

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate expiryDate;

    private Integer nqfLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QualificationStatus status = QualificationStatus.ACTIVE;

    @Column(nullable = false, length = 64)
    private String credentialHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", nullable = false)
    private UserAccount registeredBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getHolderNationalId() {
        return holderNationalId;
    }

    public void setHolderNationalId(String holderNationalId) {
        this.holderNationalId = holderNationalId;
    }

    public String getHolderUsername() {
        return holderUsername;
    }

    public void setHolderUsername(String holderUsername) {
        this.holderUsername = holderUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public QualificationType getType() {
        return type;
    }

    public void setType(QualificationType type) {
        this.type = type;
    }

    public String getIssuingInstitution() {
        return issuingInstitution;
    }

    public void setIssuingInstitution(String issuingInstitution) {
        this.issuingInstitution = issuingInstitution;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getNqfLevel() {
        return nqfLevel;
    }

    public void setNqfLevel(Integer nqfLevel) {
        this.nqfLevel = nqfLevel;
    }

    public QualificationStatus getStatus() {
        return status;
    }

    public void setStatus(QualificationStatus status) {
        this.status = status;
    }

    public String getCredentialHash() {
        return credentialHash;
    }

    public void setCredentialHash(String credentialHash) {
        this.credentialHash = credentialHash;
    }

    public UserAccount getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(UserAccount registeredBy) {
        this.registeredBy = registeredBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
