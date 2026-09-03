package com.mim736.qvs.web;

import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.service.QualificationService;
import com.mim736.qvs.web.dto.QualificationRequest;
import com.mim736.qvs.web.dto.QualificationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/qualifications")
public class QualificationController {

    private final QualificationService qualificationService;

    public QualificationController(QualificationService qualificationService) {
        this.qualificationService = qualificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    public QualificationResponse register(
            @Valid @RequestBody QualificationRequest request,
            Authentication authentication
    ) {
        return qualificationService.register(request, authentication.getName());
    }

    @GetMapping
    public List<QualificationResponse> search(
            @RequestParam(required = false) String holderName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String institution,
            @RequestParam(required = false) QualificationType type,
            @RequestParam(required = false) QualificationStatus status
    ) {
        return qualificationService.search(holderName, title, institution, type, status);
    }

    @GetMapping("/{id}")
    public QualificationResponse get(@PathVariable Long id) {
        return qualificationService.getById(id);
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    public QualificationResponse revoke(@PathVariable Long id) {
        return qualificationService.revoke(id);
    }
}
