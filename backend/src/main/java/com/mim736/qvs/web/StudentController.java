package com.mim736.qvs.web;

import com.mim736.qvs.service.StudentService;
import com.mim736.qvs.web.dto.EnrollStudentRequest;
import com.mim736.qvs.web.dto.StudentDetailResponse;
import com.mim736.qvs.web.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    public List<StudentResponse> list(Authentication authentication) {
        return studentService.list(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    public StudentDetailResponse get(@PathVariable Long id, Authentication authentication) {
        return studentService.get(id, authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ISSUER')")
    public StudentResponse enroll(
            @Valid @RequestBody EnrollStudentRequest request,
            Authentication authentication
    ) {
        return studentService.enroll(request, authentication.getName());
    }
}
