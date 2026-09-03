package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.NotFoundException;
import com.mim736.qvs.web.dto.QualificationRequest;
import com.mim736.qvs.web.dto.QualificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class QualificationService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final QualificationRepository qualificationRepository;
    private final UserAccountRepository userAccountRepository;
    private final SecureRandom random = new SecureRandom();

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

        Qualification qualification = new Qualification();
        qualification.setCredentialId(UUID.randomUUID().toString());
        qualification.setVerificationCode(generateUniqueCode());
        qualification.setHolderName(request.getHolderName().trim());
        qualification.setHolderNationalId(blankToNull(request.getHolderNationalId()));
        qualification.setTitle(request.getTitle().trim());
        qualification.setType(request.getType());
        qualification.setIssuingInstitution(request.getIssuingInstitution().trim());
        qualification.setIssueDate(request.getIssueDate());
        qualification.setExpiryDate(request.getExpiryDate());
        qualification.setNqfLevel(request.getNqfLevel());
        qualification.setStatus(deriveStatus(request.getExpiryDate()));
        qualification.setRegisteredBy(registrar);
        qualification.setCredentialHash(CredentialHashService.hash(qualification));
        return QualificationResponse.from(qualificationRepository.save(qualification));
    }

    @Transactional(readOnly = true)
    public List<QualificationResponse> search(
            String holderName,
            String title,
            String institution,
            QualificationType type,
            QualificationStatus status
    ) {
        return qualificationRepository.search(
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
    public QualificationResponse getById(Long id) {
        return QualificationResponse.from(find(id));
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

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder builder = new StringBuilder("QVS-");
            for (int i = 0; i < 10; i++) {
                builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            String code = builder.toString();
            if (!qualificationRepository.existsByVerificationCode(code)) {
                return code;
            }
        }
        throw new BusinessException("Unable to allocate a unique verification code");
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
