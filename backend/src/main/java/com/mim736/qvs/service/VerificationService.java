package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.domain.VerificationRecord;
import com.mim736.qvs.domain.VerificationResult;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.repo.VerificationRecordRepository;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.NotFoundException;
import com.mim736.qvs.web.dto.AuditRecordResponse;
import com.mim736.qvs.web.dto.QualificationResponse;
import com.mim736.qvs.web.dto.VerifyRequest;
import com.mim736.qvs.web.dto.VerifyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class VerificationService {

    private final QualificationRepository qualificationRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final UserAccountRepository userAccountRepository;

    public VerificationService(
            QualificationRepository qualificationRepository,
            VerificationRecordRepository verificationRecordRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.qualificationRepository = qualificationRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public VerifyResponse verify(VerifyRequest request, String username) {
        if (isBlank(request.getVerificationCode()) && isBlank(request.getCredentialId())) {
            throw new BusinessException("Provide a verification code or credential ID");
        }
        UserAccount verifier = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Verifier not found"));

        Qualification qualification = null;
        String method = "CODE";
        if (!isBlank(request.getVerificationCode())) {
            qualification = qualificationRepository.findByVerificationCode(request.getVerificationCode().trim())
                    .orElse(null);
            method = "CODE";
        }
        if (qualification == null && !isBlank(request.getVerificationCode())) {
            qualification = qualificationRepository.findByCredentialId(request.getVerificationCode().trim())
                    .orElse(null);
        }
        if (qualification == null && !isBlank(request.getCredentialId())) {
            qualification = qualificationRepository.findByCredentialId(request.getCredentialId().trim()).orElse(null);
            method = "CREDENTIAL_ID";
        }

        if (qualification == null) {
            persistAudit(null, request, verifier, VerificationResult.NOT_FOUND, method, "No matching record");
            return new VerifyResponse(VerificationResult.NOT_FOUND,
                    "No qualification matches the supplied identifier.", null, false);
        }

        String recomputed = CredentialHashService.hash(qualification);
        boolean hashMatch = recomputed.equalsIgnoreCase(qualification.getCredentialHash());
        if (!isBlank(request.getExpectedHash())) {
            hashMatch = hashMatch && recomputed.equalsIgnoreCase(request.getExpectedHash().trim());
            method = "HASH";
        }

        VerificationResult result;
        String message;
        if (!hashMatch) {
            result = VerificationResult.TAMPERED;
            message = "Stored integrity hash does not match the canonical credential payload.";
        } else if (qualification.getStatus() == QualificationStatus.REVOKED) {
            result = VerificationResult.REVOKED;
            message = "This qualification has been revoked by the issuing authority.";
        } else if (qualification.getStatus() == QualificationStatus.EXPIRED
                || (qualification.getExpiryDate() != null && qualification.getExpiryDate().isBefore(LocalDate.now()))) {
            result = VerificationResult.EXPIRED;
            message = "This qualification has expired.";
        } else {
            result = VerificationResult.VALID;
            message = "Qualification is authentic and currently active.";
        }

        persistAudit(qualification, request, verifier, result, method, message);
        return new VerifyResponse(result, message, QualificationResponse.from(qualification), hashMatch);
    }

    @Transactional(readOnly = true)
    public List<AuditRecordResponse> auditTrail() {
        return verificationRecordRepository.findAllByOrderByVerifiedAtDesc()
                .stream()
                .map(AuditRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verificationReport() {
        List<VerificationRecord> records = verificationRecordRepository.findAll();
        long valid = records.stream().filter(item -> item.getResult() == VerificationResult.VALID).count();
        long invalid = records.size() - valid;
        return Map.of(
                "totalChecks", records.size(),
                "valid", valid,
                "notValid", invalid,
                "generatedBy", "QVS automated verification report"
        );
    }

    private void persistAudit(
            Qualification qualification,
            VerifyRequest request,
            UserAccount verifier,
            VerificationResult result,
            String method,
            String notes
    ) {
        VerificationRecord record = new VerificationRecord();
        record.setQualification(qualification);
        record.setCredentialIdAttempted(request.getCredentialId());
        record.setVerificationCodeAttempted(request.getVerificationCode());
        record.setVerifiedBy(verifier);
        record.setResult(result);
        record.setMethod(method);
        record.setNotes(notes);
        verificationRecordRepository.save(record);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
