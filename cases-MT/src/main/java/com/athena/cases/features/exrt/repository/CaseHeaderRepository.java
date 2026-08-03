package com.athena.cases.features.exrt.repository;

import com.athena.cases.features.exrt.entity.CaseHeaderEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseHeaderRepository extends JpaRepository<CaseHeaderEntity, Long> {

    Optional<CaseHeaderEntity> findByCaseNumber(String caseNumber);
}
