package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.StudentStage;
import com.mim736.qvs.domain.UserAccount;

public class UserSummaryResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private String institutionCode;
    private String institutionName;
    private StudentStage studentStage;

    public static UserSummaryResponse from(UserAccount account) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.id = account.getId();
        response.username = account.getUsername();
        response.fullName = account.getFullName();
        response.email = account.getEmail();
        response.role = account.getRole();
        response.studentStage = account.getStudentStage();
        if (account.getInstitution() != null) {
            response.institutionCode = account.getInstitution().getCode();
            response.institutionName = account.getInstitution().getName();
        }
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

    public Role getRole() {
        return role;
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
}
