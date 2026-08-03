package com.athena.cases.identity.repository;

import com.athena.cases.identity.entity.GroupEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    Optional<GroupEntity> findByCode(String code);

    boolean existsByCodeIgnoreCase(String code);
}
