package com.athena.cases.auth;

import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.config.AdminSeedProperties;
import com.athena.cases.identity.entity.GroupEntity;
import com.athena.cases.identity.entity.RoleEntity;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.GroupRepository;
import com.athena.cases.identity.repository.RoleRepository;
import com.athena.cases.identity.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds module users: charan (Billing), saif (DBM), umar (ExRT) with role/group scopes from DB.
 */
@Component
@Order(2)
public class CasesViewerSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CasesViewerSeedRunner.class);

    // updatePasswordOnSync=true: keep known demo passwords in sync so Utilities edits / autofill
    // cannot leave charan/saif/umar unable to log in with the documented credentials.
    private static final List<ModuleUserSeed> USERS = List.of(
            new ModuleUserSeed("charan", "MTcharan", "FranklinCharan@123", true, "BILLING_CASE_USER", "BILLING_CASE_USERS"),
            new ModuleUserSeed("saif", "MTsaif", "FranklinSaif@123", true, "DBM_CASE_USER", "DBM_CASE_USERS"),
            new ModuleUserSeed("umar", "MTumar", "FranklinUmar@123", true, "EXRT_CASE_USER", "EXRT_CASE_USERS"));

    private final AdminSeedProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public CasesViewerSeedRunner(
            AdminSeedProperties properties,
            UserRepository userRepository,
            RoleRepository roleRepository,
            GroupRepository groupRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isSeedEnabled()) {
            log.info("module case user seed skipped - seed-enabled=false");
            return;
        }

        for (ModuleUserSeed seed : USERS) {
            RoleEntity role = roleRepository.findByCode(seed.roleCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", seed.roleCode()));
            GroupEntity group = groupRepository.findByCode(seed.groupCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Group", seed.groupCode()));

            userRepository.findByUsernameIgnoreCase(seed.username()).ifPresentOrElse(existing -> {
                existing.setDisplayName(seed.displayName());
                existing.setStatus("ACTIVE");
                if (seed.updatePasswordOnSync()) {
                    existing.setPasswordHash(passwordEncoder.encode(seed.password()));
                }
                // Replace prior demo roles/groups with the module-specific assignment
                existing.setRoles(new HashSet<>(Set.of(role)));
                existing.setGroups(new HashSet<>(Set.of(group)));
                existing.setUpdatedAt(Instant.now());
                existing.setUpdatedBy("SYSTEM");
                userRepository.save(existing);
                log.info("module case user synced - username={} role={} passwordReset={}",
                        seed.username(), seed.roleCode(), seed.updatePasswordOnSync());
            }, () -> {
                Instant now = Instant.now();
                UserEntity user = new UserEntity();
                user.setUsername(seed.username());
                user.setPasswordHash(passwordEncoder.encode(seed.password()));
                user.setDisplayName(seed.displayName());
                user.setStatus("ACTIVE");
                user.setRoles(new HashSet<>(Set.of(role)));
                user.setGroups(new HashSet<>(Set.of(group)));
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                user.setCreatedBy("SYSTEM");
                user.setUpdatedBy("SYSTEM");
                user.setVersion(1);
                userRepository.save(user);
                log.info("module case user seeded - username={} role={}", seed.username(), seed.roleCode());
            });
        }
    }

    private record ModuleUserSeed(
            String username,
            String displayName,
            String password,
            boolean updatePasswordOnSync,
            String roleCode,
            String groupCode) {}
}
