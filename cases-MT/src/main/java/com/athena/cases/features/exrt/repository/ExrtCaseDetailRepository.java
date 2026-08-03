package com.athena.cases.features.exrt.repository;

import com.athena.cases.features.exrt.entity.ExrtCaseDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExrtCaseDetailRepository extends JpaRepository<ExrtCaseDetailEntity, Long> {

    Optional<ExrtCaseDetailEntity> findByCaseHeaderId(Long caseId);
}
