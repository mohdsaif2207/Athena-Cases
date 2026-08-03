package com.athena.cases.casemanagement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    @Query("""
            SELECT c FROM CaseEntity c
            WHERE (:caseTypeIdsEmpty = true OR c.caseTypeId IN :caseTypeIds)
            ORDER BY c.updatedAt DESC, c.createdAt DESC
            """)
    List<CaseEntity> findAuthorized(
            @Param("caseTypeIds") List<Long> caseTypeIds,
            @Param("caseTypeIdsEmpty") boolean caseTypeIdsEmpty);
}
