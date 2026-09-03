package com.mim736.qvs.service;

import com.mim736.qvs.domain.Institution;
import com.mim736.qvs.domain.Role;
import com.mim736.qvs.repo.InstitutionRepository;
import com.mim736.qvs.repo.QualificationRepository;
import com.mim736.qvs.repo.UserAccountRepository;
import com.mim736.qvs.web.dto.InstitutionSummaryResponse;
import com.mim736.qvs.web.dto.UserSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDirectoryService {

    private final UserAccountRepository userAccountRepository;
    private final InstitutionRepository institutionRepository;
    private final QualificationRepository qualificationRepository;

    public AdminDirectoryService(
            UserAccountRepository userAccountRepository,
            InstitutionRepository institutionRepository,
            QualificationRepository qualificationRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.institutionRepository = institutionRepository;
        this.qualificationRepository = qualificationRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> users() {
        return userAccountRepository.findAllByOrderByRoleAscFullNameAsc()
                .stream()
                .map(UserSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InstitutionSummaryResponse> institutions() {
        return institutionRepository.findAll().stream().map(this::toSummary).toList();
    }

    private InstitutionSummaryResponse toSummary(Institution institution) {
        return new InstitutionSummaryResponse(
                institution.getId(),
                institution.getCode(),
                institution.getName(),
                userAccountRepository.countStudentsAtInstitution(Role.STUDENT, institution.getCode()),
                qualificationRepository.countByIssuingInstitution(institution.getName())
        );
    }
}
