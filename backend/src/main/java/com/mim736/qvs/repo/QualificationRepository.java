package com.mim736.qvs.repo;

import com.mim736.qvs.domain.Qualification;
import com.mim736.qvs.domain.QualificationStatus;
import com.mim736.qvs.domain.QualificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QualificationRepository extends JpaRepository<Qualification, Long> {

    Optional<Qualification> findByCredentialId(String credentialId);

    Optional<Qualification> findByVerificationCode(String verificationCode);

    boolean existsByVerificationCode(String verificationCode);

    boolean existsByCredentialId(String credentialId);

    long countByCredentialIdStartingWith(String prefix);

    boolean existsByHolderUsernameIgnoreCaseAndTitle(String holderUsername, String title);

    List<Qualification> findByHolderUsernameIgnoreCaseOrderByCreatedAtDesc(String holderUsername);

    @Query("""
            SELECT q FROM Qualification q
            WHERE (:q IS NULL
                OR LOWER(q.holderName) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(q.verificationCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(q.credentialId) LIKE LOWER(CONCAT('%', :q, '%'))
                OR (q.holderNationalId IS NOT NULL AND LOWER(q.holderNationalId) LIKE LOWER(CONCAT('%', :q, '%'))))
              AND (:holderName IS NULL OR LOWER(q.holderName) LIKE LOWER(CONCAT('%', :holderName, '%')))
              AND (:title IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:institution IS NULL OR LOWER(q.issuingInstitution) LIKE LOWER(CONCAT('%', :institution, '%')))
              AND (:type IS NULL OR q.type = :type)
              AND (:status IS NULL OR q.status = :status)
            ORDER BY q.createdAt DESC
            """)
    List<Qualification> search(
            @Param("q") String query,
            @Param("holderName") String holderName,
            @Param("title") String title,
            @Param("institution") String institution,
            @Param("type") QualificationType type,
            @Param("status") QualificationStatus status
    );

    long countByHolderUsernameIgnoreCase(String holderUsername);

    long countByIssuingInstitution(String issuingInstitution);
}
