package com.athena.cases.preference;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, Long> {

    Optional<UserPreferenceEntity> findByUserIdAndPrefKey(Long userId, String prefKey);

    void deleteByUserIdAndPrefKey(Long userId, String prefKey);
}
