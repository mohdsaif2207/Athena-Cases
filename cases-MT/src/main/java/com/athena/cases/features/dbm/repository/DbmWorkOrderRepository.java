package com.athena.cases.features.dbm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athena.cases.features.dbm.entity.DbmWorkOrder;

public interface DbmWorkOrderRepository extends JpaRepository<DbmWorkOrder, Long> {

    Optional<DbmWorkOrder> findByCaseId(Long caseId);
}
