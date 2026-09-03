package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.StudentStage;
import com.mim736.qvs.domain.UserAccount;

public class StudentResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String institutionCode;
    private String institutionName;
    private StudentStage studentStage;
    private long credentialCount;

    public static StudentResponse from(UserAccount account, long credentialCount) {
        StudentResponse response = new StudentResponse();
        response.id = account.getId();
        response.username = account.getUsername();
        response.fullName = account.getFullName();
        response.email = account.getEmail();
        if (account.getInstitution() != null) {
            response.institutionCode = account.getInstitution().getCode();
            response.institutionName = account.getInstitution().getName();
        }
        response.studentStage = account.getStudentStage();
        response.credentialCount = credentialCount;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public StudentStage getStudentStage() {
        return studentStage;
    }

    public long getCredentialCount() {
        return credentialCount;
    }
}
