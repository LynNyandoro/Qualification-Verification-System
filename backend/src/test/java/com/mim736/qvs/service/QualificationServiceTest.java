package com.mim736.qvs.service;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationType;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.UserAccount;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.web.BusinessException;
import com.mim736.qvs.web.dto.QualificationRequest;
import com.mim736.qvs.web.dto.QualificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualificationServiceTest {

    @Mock
    private QualificationRepository qualificationRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    private QualificationService qualificationService;

    @BeforeEach
    void setUp() {
        qualificationService = new QualificationService(qualificationRepository, userAccountRepository);
    }

    @Test
    void registerPersistsHashedCredential() {
        UserAccount issuer = new UserAccount();
        issuer.setUsername("issuer");
        issuer.setRole(Role.ISSUER);
        when(userAccountRepository.findByUsername("issuer")).thenReturn(Optional.of(issuer));
        when(qualificationRepository.countByCredentialIdStartingWith(ArgumentMatchers.anyString())).thenReturn(0L);
        when(qualificationRepository.existsByCredentialId(ArgumentMatchers.anyString())).thenReturn(false);
        when(qualificationRepository.existsByVerificationCode(ArgumentMatchers.anyString())).thenReturn(false);
        when(qualificationRepository.save(ArgumentMatchers.any(Qualification.class))).thenAnswer(invocation -> {
            Qualification qualification = invocation.getArgument(0);
            qualification.setId(10L);
            return qualification;
        });

        QualificationResponse response = qualificationService.register(validRequest(), "issuer");
        assertEquals("Amina Chikomo", response.getHolderName());
        assertNotNull(response.getCredentialHash());
        assertEquals(64, response.getCredentialHash().length());
        assertNotNull(response.getVerificationCode());
        assertTrue(response.getVerificationCode().startsWith("QVS-"));
        assertNotEquals(response.getCredentialId(), response.getVerificationCode());
    }

    @Test
    void futureIssueDateIsRejected() {
        QualificationRequest request = validRequest();
        request.setIssueDate(LocalDate.now().plusDays(2));
        assertThrows(BusinessException.class, () -> qualificationService.register(request, "issuer"));
    }

    @Test
    void invalidNqfLevelIsRejected() {
        QualificationRequest request = validRequest();
        request.setNqfLevel(12);
        assertThrows(BusinessException.class, () -> qualificationService.register(request, "issuer"));
    }

    private static QualificationRequest validRequest() {
        QualificationRequest request = new QualificationRequest();
        request.setHolderName("Amina Chikomo");
        request.setTitle("MSc Information Systems");
        request.setType(QualificationType.DEGREE);
        request.setIssuingInstitution("Midlands State University");
        request.setIssueDate(LocalDate.of(2024, 6, 1));
        request.setNqfLevel(9);
        return request;
    }
}
