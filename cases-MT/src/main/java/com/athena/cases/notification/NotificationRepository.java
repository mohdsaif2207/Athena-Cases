package com.athena.cases.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE (:teamIdsEmpty = true OR n.receivingTeamId IN :teamIds)
            ORDER BY n.receivedAt DESC
            """)
    List<NotificationEntity> findAuthorized(
            @Param("teamIds") List<Long> teamIds,
            @Param("teamIdsEmpty") boolean teamIdsEmpty);
}
