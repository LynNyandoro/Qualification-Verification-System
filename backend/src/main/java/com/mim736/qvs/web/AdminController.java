package com.mim736.qvs.web;

import com.mim736.qvs.service.AdminDirectoryService;
import com.mim736.qvs.web.dto.InstitutionSummaryResponse;
import com.mim736.qvs.web.dto.UserSummaryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminDirectoryService adminDirectoryService;

    public AdminController(AdminDirectoryService adminDirectoryService) {
        this.adminDirectoryService = adminDirectoryService;
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> users() {
        return adminDirectoryService.users();
    }

    @GetMapping("/institutions")
    public List<InstitutionSummaryResponse> institutions() {
        return adminDirectoryService.institutions();
    }
}
