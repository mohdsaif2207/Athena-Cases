package com.athena.cases.auth;

import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.GroupEntity;
import com.athena.cases.identity.entity.RoleEntity;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.GroupRepository;
import com.athena.cases.identity.repository.RoleRepository;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.security.AdminSeedProperties;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Seeds/updates the default System Administrator (BCrypt password + role/group assignment).
 */
@Component
public class DefaultAdminSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeedRunner.class);
    private static final String SYSTEM_ADMIN_ROLE = "SYSTEM_ADMINISTRATOR";
    private static final String SYSTEM_ADMIN_GROUP = "SYSTEM_ADMINISTRATORS";

    private final AdminSeedProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminSeedRunner(
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
            log.info("default admin seed skipped - seed-enabled=false");
            return;
        }
        if (!StringUtils.hasText(properties.getUsername()) || !StringUtils.hasText(properties.getPassword())) {
            log.warn("default admin seed skipped - username/password not configured");
            return;
        }

        RoleEntity adminRole = roleRepository.findByCode(SYSTEM_ADMIN_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role", SYSTEM_ADMIN_ROLE));
        GroupEntity adminGroup = groupRepository.findByCode(SYSTEM_ADMIN_GROUP)
                .orElseThrow(() -> new ResourceNotFoundException("Group", SYSTEM_ADMIN_GROUP));

        String username = properties.getUsername().trim();
        String displayName = StringUtils.hasText(properties.getDisplayName())
                ? properties.getDisplayName().trim()
                : username;

        userRepository.findByUsernameIgnoreCase(username).ifPresentOrElse(existing -> {
            boolean changed = false;
            if (!displayName.equals(existing.getDisplayName())) {
                existing.setDisplayName(displayName);
                changed = true;
            }
            if (existing.getRoles().stream().noneMatch(r -> SYSTEM_ADMIN_ROLE.equals(r.getCode()))) {
                existing.getRoles().add(adminRole);
                changed = true;
            }
            if (existing.getGroups().stream().noneMatch(g -> SYSTEM_ADMIN_GROUP.equals(g.getCode()))) {
                existing.getGroups().add(adminGroup);
                changed = true;
            }
            if (changed) {
                existing.setUpdatedAt(Instant.now());
                existing.setUpdatedBy("SYSTEM");
                userRepository.save(existing);
                log.info("default admin synced - username={} displayName={}", username, displayName);
            } else {
                log.info("default admin already exists - username={}", username);
            }
        }, () -> {
            Instant now = Instant.now();
            UserEntity admin = new UserEntity();
            admin.setUsername(username);
            admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
            admin.setDisplayName(displayName);
            admin.setStatus("ACTIVE");
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            admin.setGroups(new HashSet<>(Set.of(adminGroup)));
            admin.setCreatedAt(now);
            admin.setUpdatedAt(now);
            admin.setCreatedBy("SYSTEM");
            admin.setUpdatedBy("SYSTEM");
            admin.setVersion(1);
            userRepository.save(admin);
            log.info(
                    "default system administrator seeded - username={} displayName={} group={}",
                    username,
                    displayName,
                    SYSTEM_ADMIN_GROUP);
        });
    }
}
