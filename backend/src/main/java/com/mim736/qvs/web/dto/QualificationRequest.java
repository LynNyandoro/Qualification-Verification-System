package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.QualificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class QualificationRequest {

    @NotBlank
    private String holderName;

    private String holderNationalId;

    @NotBlank
    private String title;

    @NotNull
    private QualificationType type;

    @NotBlank
    private String issuingInstitution;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expiryDate;

    private Integer nqfLevel;

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
}
