package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.Institution;

public class InstitutionResponse {

    private Long id;
    private String code;
    private String name;

    public static InstitutionResponse from(Institution institution) {
        InstitutionResponse response = new InstitutionResponse();
        response.id = institution.getId();
        response.code = institution.getCode();
        response.name = institution.getName();
        return response;
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
}
