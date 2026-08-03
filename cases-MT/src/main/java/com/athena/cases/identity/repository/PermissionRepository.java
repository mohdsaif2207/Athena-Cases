package com.athena.cases.identity.repository;

import com.athena.cases.identity.entity.PermissionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    Optional<PermissionEntity> findByCode(String code);
}
