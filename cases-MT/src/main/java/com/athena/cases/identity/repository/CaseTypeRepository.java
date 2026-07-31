package com.athena.cases.identity.repository;

import com.athena.cases.identity.entity.CaseTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, Long> {

    Optional<CaseTypeEntity> findByCode(String code);
}
