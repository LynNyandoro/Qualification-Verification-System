package com.mim736.qvs.web.dto;

public class InstitutionSummaryResponse {

    private Long id;
    private String code;
    private String name;
    private long studentCount;
    private long credentialCount;

    public InstitutionSummaryResponse(Long id, String code, String name, long studentCount, long credentialCount) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.studentCount = studentCount;
        this.credentialCount = credentialCount;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public long getCredentialCount() {
        return credentialCount;
    }
}
