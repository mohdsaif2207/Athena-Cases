package com.athena.cases.workflow;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, Long> {

    Optional<WorkflowEntity> findFirstByCaseIdOrderByReceivedAtDesc(Long caseId);

    @Query("""
            SELECT w FROM WorkflowEntity w
            WHERE (:teamIdsEmpty = true OR w.receivingTeamId IN :teamIds)
            ORDER BY w.receivedAt DESC
            """)
    List<WorkflowEntity> findAuthorized(
            @Param("teamIds") List<Long> teamIds,
            @Param("teamIdsEmpty") boolean teamIdsEmpty);
}
