package com.athena.cases.identity.repository;

import com.athena.cases.identity.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Active users assigned to a team via {@code user_teams} (IAM team membership).
     */
    @Query("""
            select distinct u from UserEntity u
            join u.teams t
            where t.code = :teamCode
              and t.active = true
              and upper(u.status) = 'ACTIVE'
            order by u.displayName asc, u.username asc
            """)
    List<UserEntity> findActiveByTeamCode(@Param("teamCode") String teamCode);
}
