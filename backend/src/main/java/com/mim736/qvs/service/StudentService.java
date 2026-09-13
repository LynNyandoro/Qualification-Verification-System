package com.mim736.qvs.service;

import com.mim736.qvs.domain.Institution;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.InstitutionRepository;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.NotFoundException;
import com.mim736.qvs.web.dto.EnrollStudentRequest;
import com.mim736.qvs.web.dto.QualificationResponse;
import com.mim736.qvs.web.dto.StudentDetailResponse;
import com.mim736.qvs.web.dto.StudentResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final UserAccountRepository userAccountRepository;
    private final InstitutionRepository institutionRepository;
    private final QualificationRepository qualificationRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            UserAccountRepository userAccountRepository,
            InstitutionRepository institutionRepository,
            QualificationRepository qualificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.institutionRepository = institutionRepository;
        this.qualificationRepository = qualificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> list(String actorUsername) {
        UserAccount actor = requireUser(actorUsername);
        List<UserAccount> students;
        if (actor.getRole() == Role.ADMIN) {
            students = userAccountRepository.findByRoleOrderByFullNameAsc(Role.STUDENT);
        } else if (actor.getRole() == Role.ISSUER) {
            if (actor.getInstitution() == null) {
                throw new BusinessException("Issuer is not linked to an institution");
            }
            students = userAccountRepository.findStudentsAtInstitution(
                    Role.STUDENT, actor.getInstitution().getCode());
        } else {
            throw new BusinessException("Only issuers and administrators can list students");
        }
        return students.stream().map(this::toResponse).toList();
    }

    @Transactional
    public StudentResponse enroll(EnrollStudentRequest request, String actorUsername) {
        UserAccount actor = requireUser(actorUsername);
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.ISSUER) {
            throw new BusinessException("Only issuers and administrators can enrol students");
        }
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken");
        }
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }
        if (userAccountRepository.existsByRoleAndFullNameIgnoreCase(Role.STUDENT, request.getFullName().trim())) {
            throw new BusinessException("A student with this name already exists");
        }
        Institution institution = resolveInstitution(request.getInstitutionCode(), actor);
        UserAccount student = new UserAccount();
        student.setUsername(request.getUsername().trim());
        student.setEmail(request.getEmail().trim().toLowerCase());
        student.setFullName(request.getFullName().trim());
        student.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        student.setRole(Role.STUDENT);
        student.setEnabled(true);
        student.setInstitution(institution);
        student.setStudentStage(request.getStudentStage());
        userAccountRepository.save(student);
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse get(Long id, String actorUsername) {
        UserAccount actor = requireUser(actorUsername);
        UserAccount student = userAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        if (student.getRole() != Role.STUDENT) {
            throw new NotFoundException("Student not found");
        }
        if (actor.getRole() == Role.ISSUER) {
            if (actor.getInstitution() == null || student.getInstitution() == null
                    || !actor.getInstitution().getCode().equals(student.getInstitution().getCode())) {
                throw new NotFoundException("Student not found");
            }
        } else if (actor.getRole() != Role.ADMIN) {
            throw new BusinessException("Only issuers and administrators can view student records");
        }
        List<QualificationResponse> credentials = qualificationRepository
                .findByHolderUsernameIgnoreCaseOrderByCreatedAtDesc(student.getUsername())
                .stream()
                .map(QualificationResponse::from)
                .toList();
        return new StudentDetailResponse(toResponse(student), credentials);
    }

    private Institution resolveInstitution(String requestedCode, UserAccount actor) {
        if (actor.getRole() == Role.ISSUER) {
            if (actor.getInstitution() == null) {
                throw new BusinessException("Issuer is not linked to an institution");
            }
            if (requestedCode != null && !requestedCode.isBlank()
                    && !requestedCode.equalsIgnoreCase(actor.getInstitution().getCode())) {
                throw new BusinessException("Issuers may only enrol students at their own institution");
            }
            return actor.getInstitution();
        }
        if (requestedCode == null || requestedCode.isBlank()) {
            throw new BusinessException("Institution code is required");
        }
        return institutionRepository.findByCode(requestedCode.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Unknown institution"));
    }

    private StudentResponse toResponse(UserAccount student) {
        return StudentResponse.from(
                student,
                qualificationRepository.countByHolderUsernameIgnoreCase(student.getUsername())
        );
    }

    private UserAccount requireUser(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
