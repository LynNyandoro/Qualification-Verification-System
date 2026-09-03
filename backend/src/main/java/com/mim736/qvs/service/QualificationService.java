package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.StudentStage;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.NotFoundException;
import com.mim736.qvs.web.dto.QualificationRequest;
import com.mim736.qvs.web.dto.QualificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class QualificationService {

    private final QualificationRepository qualificationRepository;
    private final UserAccountRepository userAccountRepository;

    public QualificationService(
            QualificationRepository qualificationRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.qualificationRepository = qualificationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public QualificationResponse register(QualificationRequest request, String registrarUsername) {
        validateDates(request.getIssueDate(), request.getExpiryDate());
        if (request.getNqfLevel() != null && (request.getNqfLevel() < 1 || request.getNqfLevel() > 10)) {
            throw new BusinessException("NQF level must be between 1 and 10");
        }
        UserAccount registrar = userAccountRepository.findByUsername(registrarUsername)
                .orElseThrow(() -> new NotFoundException("Registrar not found"));

        UserAccount holder = null;
        String holderUsername = emptyToNull(request.getHolderUsername());
        if (holderUsername != null) {
            holder = userAccountRepository.findByUsername(holderUsername)
                    .orElseThrow(() -> new NotFoundException("Student account not found"));
            if (holder.getRole() != Role.STUDENT) {
                throw new BusinessException("Credentials can only be linked to a student account");
            }
            if (holder.getStudentStage() == StudentStage.ENROLLED) {
                throw new BusinessException("Newly enrolled students cannot be awarded a qualification yet");
            }
            if (registrar.getRole() == Role.ISSUER
                    && registrar.getInstitution() != null
                    && holder.getInstitution() != null
                    && !registrar.getInstitution().getCode().equals(holder.getInstitution().getCode())) {
                throw new BusinessException("Issuers may only award credentials for students at their institution");
            }
        }

        String institutionName = request.getIssuingInstitution().trim();
        if (holder != null && holder.getInstitution() != null) {
            institutionName = holder.getInstitution().getName();
        } else if (registrar.getInstitution() != null && institutionName.isBlank()) {
            institutionName = registrar.getInstitution().getName();
        }
        UserAccount institutionSource = holder != null ? holder : registrar;

        Qualification qualification = new Qualification();
        String shareId = CredentialIds.next(
                qualificationRepository,
                CredentialIds.programCode(request.getTitle()),
                CredentialIds.institutionCode(institutionSource, institutionName)
        );
        qualification.setCredentialId(shareId);
        qualification.setVerificationCode(CredentialIds.nextVerificationCode(qualificationRepository));
        qualification.setHolderName(request.getHolderName().trim());
        qualification.setHolderNationalId(blankToNull(request.getHolderNationalId()));
        qualification.setHolderUsername(holderUsername);
        qualification.setTitle(request.getTitle().trim());
        qualification.setType(request.getType());
        qualification.setIssuingInstitution(institutionName);
        qualification.setIssueDate(request.getIssueDate());
        qualification.setExpiryDate(request.getExpiryDate());
        qualification.setNqfLevel(request.getNqfLevel());
        qualification.setStatus(deriveStatus(request.getExpiryDate()));
        qualification.setRegisteredBy(registrar);
        qualification.setCredentialHash(CredentialHashService.hash(qualification));
        Qualification saved = qualificationRepository.save(qualification);
        if (holder != null && holder.getStudentStage() == StudentStage.GRADUATING) {
            holder.setStudentStage(StudentStage.ALUMNI);
            userAccountRepository.save(holder);
        }
        return QualificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<QualificationResponse> search(
            String query,
            String holderName,
            String title,
            String institution,
            QualificationType type,
            QualificationStatus status,
            String actorUsername
    ) {
        UserAccount actor = requireUser(actorUsername);
        if (actor.getRole() == Role.STUDENT) {
            return qualificationRepository.findByHolderUsernameIgnoreCaseOrderByCreatedAtDesc(actor.getUsername())
                    .stream()
                    .map(QualificationResponse::from)
                    .toList();
        }
        return qualificationRepository.search(
                        emptyToNull(query),
                        emptyToNull(holderName),
                        emptyToNull(title),
                        emptyToNull(institution),
                        type,
                        status
                ).stream()
                .map(QualificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QualificationResponse getById(Long id, String actorUsername) {
        Qualification qualification = find(id);
        assertStudentCanView(qualification, requireUser(actorUsername));
        return QualificationResponse.from(qualification);
    }

    @Transactional
    public QualificationResponse revoke(Long id) {
        Qualification qualification = find(id);
        qualification.setStatus(QualificationStatus.REVOKED);
        qualification.setUpdatedAt(Instant.now());
        return QualificationResponse.from(qualificationRepository.save(qualification));
    }

    public Qualification find(Long id) {
        return qualificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Qualification not found"));
    }

    private UserAccount requireUser(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private static void assertStudentCanView(Qualification qualification, UserAccount actor) {
        if (actor.getRole() != Role.STUDENT) {
            return;
        }
        if (qualification.getHolderUsername() == null
                || !qualification.getHolderUsername().equalsIgnoreCase(actor.getUsername())) {
            throw new NotFoundException("Qualification not found");
        }
    }

    private static void validateDates(LocalDate issueDate, LocalDate expiryDate) {
        if (issueDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Issue date cannot be in the future");
        }
        if (expiryDate != null && expiryDate.isBefore(issueDate)) {
            throw new BusinessException("Expiry date cannot be before the issue date");
        }
    }

    private static QualificationStatus deriveStatus(LocalDate expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            return QualificationStatus.EXPIRED;
        }
        return QualificationStatus.ACTIVE;
    }

    private static String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
