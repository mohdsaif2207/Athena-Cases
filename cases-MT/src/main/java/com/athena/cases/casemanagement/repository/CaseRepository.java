package com.athena.cases.casemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athena.cases.casemanagement.entity.Case;

public interface CaseRepository extends JpaRepository<Case, Long> {

    Optional<Case> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);
}
