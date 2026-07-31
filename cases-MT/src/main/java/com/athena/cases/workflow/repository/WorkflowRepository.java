package com.athena.cases.workflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athena.cases.workflow.entity.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByCaseId(Long caseId);

    boolean existsByCaseId(Long caseId);
}
