package com.mim736.qvs.web.dto;

import com.mim736.qvs.domain.Role;

public class AuthResponse {

    private String token;
    private String username;
    private String fullName;
    private Role role;

    public AuthResponse(String token, String username, String fullName, Role role) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
