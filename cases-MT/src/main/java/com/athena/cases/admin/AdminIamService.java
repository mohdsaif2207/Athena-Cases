package com.athena.cases.admin;

import com.athena.cases.admin.dto.CaseTypeAdminItem;
import com.athena.cases.admin.dto.CaseTypeUpsertRequest;
import com.athena.cases.admin.dto.GroupAdminItem;
import com.athena.cases.admin.dto.GroupUpsertRequest;
import com.athena.cases.admin.dto.PermissionItem;
import com.athena.cases.admin.dto.ResetPasswordRequest;
import com.athena.cases.admin.dto.RoleAdminItem;
import com.athena.cases.admin.dto.RoleUpsertRequest;
import com.athena.cases.admin.dto.TeamAdminItem;
import com.athena.cases.admin.dto.TeamUpsertRequest;
import com.athena.cases.admin.dto.UserAdminItem;
import com.athena.cases.admin.dto.UserUpsertRequest;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.GroupEntity;
import com.athena.cases.identity.entity.PermissionEntity;
import com.athena.cases.identity.entity.RoleEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.identity.repository.GroupRepository;
import com.athena.cases.identity.repository.PermissionRepository;
import com.athena.cases.identity.repository.RoleRepository;
import com.athena.cases.identity.repository.TeamRepository;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.security.AccessResolutionService;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminIamService {

    private static final Logger log = LoggerFactory.getLogger(AdminIamService.class);
    private static final String SYSTEM_ADMIN_ROLE = "SYSTEM_ADMINISTRATOR";
    private static final String SYSTEM_ADMIN_GROUP = "SYSTEM_ADMINISTRATORS";
    private static final Set<String> VALID_TEAM_TYPES = Set.of("INITIATING", "RECEIVING", "BOTH");
    private static final Set<String> VALID_USER_STATUS = Set.of("ACTIVE", "INACTIVE", "LOCKED");

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessResolutionService accessResolutionService;
    private final CurrentUserService currentUserService;

    public AdminIamService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            CaseTypeRepository caseTypeRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder,
            AccessResolutionService accessResolutionService,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessResolutionService = accessResolutionService;
        this.currentUserService = currentUserService;
    }

    // ---- Users ----

    @Transactional(readOnly = true)
    public List<UserAdminItem> listUsers() {
        return userRepository.findAll().stream().map(this::toUserItem).toList();
    }

    @Transactional
    public UserAdminItem createUser(UserUpsertRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ForbiddenException("Username already exists");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ForbiddenException("Password is required when creating a user");
        }
        validateStatus(request.status());

        Instant now = Instant.now();
        String actor = actor();
        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setDisplayName(request.displayName().trim());
        user.setEmail(blankToNull(request.email()));
        user.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCreatedBy(actor);
        user.setUpdatedBy(actor);
        user.setVersion(1);
        applyUserAssignments(user, request);
        UserEntity saved = userRepository.save(user);
        log.info("admin created user - id={} username={} by={}", saved.getId(), saved.getUsername(), actor);
        return toUserItem(saved);
    }

    @Transactional
    public UserAdminItem updateUser(Long id, UserUpsertRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(id)));
        validateStatus(request.status());

        String newUsername = request.username().trim();
        if (!user.getUsername().equalsIgnoreCase(newUsername)
                && userRepository.existsByUsernameIgnoreCase(newUsername)) {
            throw new ForbiddenException("Username already exists");
        }

        user.setUsername(newUsername);
        user.setDisplayName(request.displayName().trim());
        user.setEmail(blankToNull(request.email()));
        user.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        applyUserAssignments(user, request);
        touch(user);
        UserEntity saved = userRepository.save(user);
        log.info("admin updated user - id={} by={}", saved.getId(), actor());
        return toUserItem(saved);
    }

    @Transactional
    public UserAdminItem setUserStatus(Long id, String status) {
        validateStatus(status);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(id)));
        user.setStatus(status.trim().toUpperCase(Locale.ROOT));
        touch(user);
        return toUserItem(userRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(id)));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        touch(user);
        userRepository.save(user);
        log.info("admin reset password - userId={} by={}", id, actor());
    }

    // ---- Groups ----

    @Transactional(readOnly = true)
    public List<GroupAdminItem> listGroups() {
        return groupRepository.findAll().stream().map(this::toGroupItem).toList();
    }

    @Transactional
    public GroupAdminItem createGroup(GroupUpsertRequest request) {
        if (groupRepository.findByCode(request.code()).isPresent()) {
            throw new ForbiddenException("Group code already exists");
        }
        Instant now = Instant.now();
        String actor = actor();
        GroupEntity group = new GroupEntity();
        group.setCode(request.code().trim());
        group.setName(request.name().trim());
        group.setDescription(blankToNull(request.description()));
        group.setActive(request.active());
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        group.setCreatedBy(actor);
        group.setUpdatedBy(actor);
        group.setVersion(1);
        group.setRoles(resolveRoles(request.roleCodes()));
        GroupEntity saved = groupRepository.save(group);
        syncGroupUsers(saved, request.usernames());
        return toGroupItem(saved);
    }

    @Transactional
    public GroupAdminItem updateGroup(Long id, GroupUpsertRequest request) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", String.valueOf(id)));
        group.setName(request.name().trim());
        group.setDescription(blankToNull(request.description()));
        group.setActive(request.active());
        group.setRoles(resolveRoles(request.roleCodes()));
        touch(group);
        GroupEntity saved = groupRepository.save(group);
        syncGroupUsers(saved, request.usernames());
        return toGroupItem(saved);
    }

    @Transactional
    public void deleteGroup(Long id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group", String.valueOf(id)));
        if (SYSTEM_ADMIN_GROUP.equals(group.getCode())) {
            throw new ForbiddenException("Cannot delete system administrator group");
        }
        // Soft-delete: deactivate and clear memberships
        for (UserEntity user : userRepository.findAll()) {
            if (user.getGroups().removeIf(g -> Objects.equals(g.getId(), id))) {
                touch(user);
                userRepository.save(user);
            }
        }
        group.setActive(false);
        group.getRoles().clear();
        touch(group);
        groupRepository.save(group);
        log.info("admin soft-deleted group - id={} code={} by={}", id, group.getCode(), actor());
    }

    // ---- Roles ----

    @Transactional(readOnly = true)
    public List<RoleAdminItem> listRoles() {
        return roleRepository.findAll().stream().map(this::toRoleItem).toList();
    }

    @Transactional
    public RoleAdminItem createRole(RoleUpsertRequest request) {
        if (roleRepository.findByCode(request.code()).isPresent()) {
            throw new ForbiddenException("Role code already exists");
        }
        Instant now = Instant.now();
        String actor = actor();
        RoleEntity role = new RoleEntity();
        role.setCode(request.code().trim());
        role.setName(request.name().trim());
        role.setDescription(blankToNull(request.description()));
        role.setActive(request.active());
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setCreatedBy(actor);
        role.setUpdatedBy(actor);
        role.setVersion(1);
        applyRoleAssignments(role, request);
        return toRoleItem(roleRepository.save(role));
    }

    @Transactional
    public RoleAdminItem updateRole(Long id, RoleUpsertRequest request) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", String.valueOf(id)));
        role.setName(request.name().trim());
        role.setDescription(blankToNull(request.description()));
        role.setActive(request.active());
        applyRoleAssignments(role, request);
        touch(role);
        return toRoleItem(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", String.valueOf(id)));
        if (SYSTEM_ADMIN_ROLE.equals(role.getCode())) {
            throw new ForbiddenException("Cannot delete system administrator role");
        }
        for (UserEntity user : userRepository.findAll()) {
            if (user.getRoles().removeIf(r -> Objects.equals(r.getId(), id))) {
                touch(user);
                userRepository.save(user);
            }
        }
        for (GroupEntity group : groupRepository.findAll()) {
            if (group.getRoles().removeIf(r -> Objects.equals(r.getId(), id))) {
                touch(group);
                groupRepository.save(group);
            }
        }
        role.setActive(false);
        role.getPermissions().clear();
        role.getCaseTypes().clear();
        role.getInitiatingTeams().clear();
        role.getReceivingTeams().clear();
        touch(role);
        roleRepository.save(role);
        log.info("admin soft-deleted role - id={} code={} by={}", id, role.getCode(), actor());
    }

    // ---- Permissions ----

    @Transactional(readOnly = true)
    public List<PermissionItem> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionItem(p.getId(), p.getCode(), p.getName(), p.getDescription()))
                .toList();
    }

    // ---- Case types ----

    @Transactional(readOnly = true)
    public List<CaseTypeAdminItem> listCaseTypes() {
        return caseTypeRepository.findAll().stream().map(this::toCaseTypeItem).toList();
    }

    @Transactional
    public CaseTypeAdminItem createCaseType(CaseTypeUpsertRequest request) {
        if (caseTypeRepository.findByCode(request.code()).isPresent()) {
            throw new ForbiddenException("Case type code already exists");
        }
        Instant now = Instant.now();
        String actor = actor();
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setModuleKey(request.moduleKey().trim());
        entity.setActive(request.active());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        entity.setVersion(1);
        entity.setInitiatingTeams(resolveTeams(request.initiatingTeamCodes()));
        entity.setReceivingTeams(resolveTeams(request.receivingTeamCodes()));
        return toCaseTypeItem(caseTypeRepository.save(entity));
    }

    @Transactional
    public CaseTypeAdminItem updateCaseType(Long id, CaseTypeUpsertRequest request) {
        CaseTypeEntity entity = caseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CaseType", String.valueOf(id)));
        entity.setName(request.name().trim());
        entity.setModuleKey(request.moduleKey().trim());
        entity.setActive(request.active());
        entity.setInitiatingTeams(resolveTeams(request.initiatingTeamCodes()));
        entity.setReceivingTeams(resolveTeams(request.receivingTeamCodes()));
        touch(entity);
        return toCaseTypeItem(caseTypeRepository.save(entity));
    }

    // ---- Teams ----

    @Transactional(readOnly = true)
    public List<TeamAdminItem> listTeams() {
        return teamRepository.findAll().stream().map(this::toTeamItem).toList();
    }

    @Transactional
    public TeamAdminItem createTeam(TeamUpsertRequest request) {
        validateTeamType(request.teamType());
        if (teamRepository.findByCode(request.code()).isPresent()) {
            throw new ForbiddenException("Team code already exists");
        }
        Instant now = Instant.now();
        String actor = actor();
        TeamEntity team = new TeamEntity();
        team.setCode(request.code().trim());
        team.setName(request.name().trim());
        team.setTeamType(request.teamType().trim().toUpperCase(Locale.ROOT));
        team.setActive(request.active());
        team.setCreatedAt(now);
        team.setUpdatedAt(now);
        team.setCreatedBy(actor);
        team.setUpdatedBy(actor);
        team.setVersion(1);
        return toTeamItem(teamRepository.save(team));
    }

    @Transactional
    public TeamAdminItem updateTeam(Long id, TeamUpsertRequest request) {
        validateTeamType(request.teamType());
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", String.valueOf(id)));
        team.setName(request.name().trim());
        team.setTeamType(request.teamType().trim().toUpperCase(Locale.ROOT));
        team.setActive(request.active());
        touch(team);
        return toTeamItem(teamRepository.save(team));
    }

    // ---- helpers ----

    private void applyUserAssignments(UserEntity user, UserUpsertRequest request) {
        user.setRoles(resolveRoles(request.roleCodes()));
        user.setGroups(resolveGroups(request.groupCodes()));
        user.setTeams(resolveTeams(request.teamCodes()));
    }

    private void applyRoleAssignments(RoleEntity role, RoleUpsertRequest request) {
        role.setPermissions(resolvePermissions(request.permissionCodes()));
        role.setCaseTypes(resolveCaseTypes(request.caseTypeCodes()));
        role.setInitiatingTeams(resolveTeams(request.initiatingTeamCodes()));
        role.setReceivingTeams(resolveTeams(request.receivingTeamCodes()));
    }

    private void syncGroupUsers(GroupEntity group, List<String> usernames) {
        Set<String> target = codesOrEmpty(usernames).stream()
                .map(u -> u.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        for (UserEntity user : userRepository.findAll()) {
            boolean shouldBelong = target.contains(user.getUsername().toLowerCase(Locale.ROOT));
            boolean belongs = user.getGroups().stream().anyMatch(g -> Objects.equals(g.getId(), group.getId()));
            if (shouldBelong && !belongs) {
                user.getGroups().add(group);
                touch(user);
                userRepository.save(user);
            } else if (!shouldBelong && belongs) {
                user.getGroups().removeIf(g -> Objects.equals(g.getId(), group.getId()));
                touch(user);
                userRepository.save(user);
            }
        }
    }

    private Set<RoleEntity> resolveRoles(List<String> codes) {
        return codesOrEmpty(codes).stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", code)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<GroupEntity> resolveGroups(List<String> codes) {
        return codesOrEmpty(codes).stream()
                .map(code -> groupRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Group", code)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<TeamEntity> resolveTeams(List<String> codes) {
        return codesOrEmpty(codes).stream()
                .map(code -> teamRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Team", code)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<PermissionEntity> resolvePermissions(List<String> codes) {
        return codesOrEmpty(codes).stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", code)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<CaseTypeEntity> resolveCaseTypes(List<String> codes) {
        return codesOrEmpty(codes).stream()
                .map(code -> caseTypeRepository.findByCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException("CaseType", code)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private UserAdminItem toUserItem(UserEntity user) {
        UserPrincipal principal = accessResolutionService.toPrincipal(user);
        return new UserAdminItem(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getStatus(),
                sortedCodes(user.getRoles().stream().map(RoleEntity::getCode).toList()),
                sortedCodes(user.getGroups().stream().map(GroupEntity::getCode).toList()),
                sortedCodes(user.getTeams().stream().map(TeamEntity::getCode).toList()),
                principal.getPermissionCodes(),
                principal.getCaseTypeCodes(),
                principal.getInitiatingTeamCodes(),
                principal.getReceivingTeamCodes(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private GroupAdminItem toGroupItem(GroupEntity group) {
        List<String> usernames = userRepository.findAll().stream()
                .filter(u -> u.getGroups().stream().anyMatch(g -> Objects.equals(g.getId(), group.getId())))
                .map(UserEntity::getUsername)
                .sorted()
                .toList();
        return new GroupAdminItem(
                group.getId(),
                group.getCode(),
                group.getName(),
                group.getDescription(),
                group.isActive(),
                sortedCodes(group.getRoles().stream().map(RoleEntity::getCode).toList()),
                usernames);
    }

    private RoleAdminItem toRoleItem(RoleEntity role) {
        return new RoleAdminItem(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                sortedCodes(role.getPermissions().stream().map(PermissionEntity::getCode).toList()),
                sortedCodes(role.getCaseTypes().stream().map(CaseTypeEntity::getCode).toList()),
                sortedCodes(role.getInitiatingTeams().stream().map(TeamEntity::getCode).toList()),
                sortedCodes(role.getReceivingTeams().stream().map(TeamEntity::getCode).toList()));
    }

    private CaseTypeAdminItem toCaseTypeItem(CaseTypeEntity ct) {
        return new CaseTypeAdminItem(
                ct.getId(),
                ct.getCode(),
                ct.getName(),
                ct.getModuleKey(),
                ct.isActive(),
                sortedCodes(ct.getInitiatingTeams().stream().map(TeamEntity::getCode).toList()),
                sortedCodes(ct.getReceivingTeams().stream().map(TeamEntity::getCode).toList()));
    }

    private TeamAdminItem toTeamItem(TeamEntity team) {
        return new TeamAdminItem(team.getId(), team.getCode(), team.getName(), team.getTeamType(), team.isActive());
    }

    private void touch(UserEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor());
    }

    private void touch(GroupEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor());
    }

    private void touch(RoleEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor());
    }

    private void touch(CaseTypeEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor());
    }

    private void touch(TeamEntity entity) {
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor());
    }

    private String actor() {
        return currentUserService.requirePrincipal().getUsername();
    }

    private static void validateStatus(String status) {
        if (status == null || !VALID_USER_STATUS.contains(status.trim().toUpperCase(Locale.ROOT))) {
            throw new ForbiddenException("Invalid user status");
        }
    }

    private static void validateTeamType(String teamType) {
        if (teamType == null || !VALID_TEAM_TYPES.contains(teamType.trim().toUpperCase(Locale.ROOT))) {
            throw new ForbiddenException("Invalid team type");
        }
    }

    private static List<String> codesOrEmpty(List<String> codes) {
        return codes == null ? List.of() : codes.stream().filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).toList();
    }

    private static List<String> sortedCodes(List<String> codes) {
        return codes.stream().sorted().toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
