package com.mim736.qvs.web.dto;

import java.util.List;

public class StudentDetailResponse {

    private StudentResponse student;
    private List<QualificationResponse> credentials;

    public StudentDetailResponse(StudentResponse student, List<QualificationResponse> credentials) {
        this.student = student;
        this.credentials = credentials;
    }

    public StudentResponse getStudent() {
        return student;
    }

    public List<QualificationResponse> getCredentials() {
        return credentials;
    }
}
