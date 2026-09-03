package com.mim736.qvs.repo;

import com.mim736.qvs.domain.VerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {

    List<VerificationRecord> findAllByOrderByVerifiedAtDesc();
}
