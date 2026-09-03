package com.mim736.qvs.repo;

import com.mim736.qvs.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByCode(String code);
}
