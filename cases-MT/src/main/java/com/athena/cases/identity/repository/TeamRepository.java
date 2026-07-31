package com.athena.cases.identity.repository;

import com.athena.cases.identity.entity.TeamEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    Optional<TeamEntity> findByCode(String code);
}
