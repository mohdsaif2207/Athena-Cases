package com.athena.cases.idallocation;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessCaseIdSequenceRepository extends JpaRepository<BusinessCaseIdSequenceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from BusinessCaseIdSequenceEntity s where s.caseTypeCode = :caseTypeCode")
    Optional<BusinessCaseIdSequenceEntity> findByCaseTypeCodeForUpdate(
            @Param("caseTypeCode") String caseTypeCode);
}
