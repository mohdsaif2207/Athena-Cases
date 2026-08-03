package com.athena.cases.security;

import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.GroupEntity;
import com.athena.cases.identity.entity.PermissionEntity;
import com.athena.cases.identity.entity.RoleEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.entity.UserEntity;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Resolves effective authorization for a user from database relationships:
 * User → Groups → Roles → Permissions / Case Types / Initiating Teams / Receiving Teams
 * plus direct user_roles and user_teams membership.
 */
@Component
public class AccessResolutionService {

    public UserPrincipal toPrincipal(UserEntity user) {
        Set<RoleEntity> effectiveRoles = resolveEffectiveRoles(user);

        List<String> roleCodes = effectiveRoles.stream()
                .map(RoleEntity::getCode)
                .sorted()
                .toList();

        List<String> permissionCodes = effectiveRoles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .distinct()
                .sorted()
                .toList();

        List<String> groupCodes = user.getGroups().stream()
                .filter(GroupEntity::isActive)
                .map(GroupEntity::getCode)
                .sorted()
                .toList();

        List<String> teamCodes = user.getTeams().stream()
                .filter(TeamEntity::isActive)
                .map(TeamEntity::getCode)
                .sorted()
                .toList();

        List<String> caseTypeCodes = effectiveRoles.stream()
                .flatMap(role -> role.getCaseTypes().stream())
                .filter(CaseTypeEntity::isActive)
                .map(CaseTypeEntity::getCode)
                .distinct()
                .sorted()
                .toList();

        List<String> initiatingTeamCodes = effectiveRoles.stream()
                .flatMap(role -> role.getInitiatingTeams().stream())
                .filter(TeamEntity::isActive)
                .map(TeamEntity::getCode)
                .distinct()
                .sorted()
                .toList();

        List<String> receivingTeamCodes = effectiveRoles.stream()
                .flatMap(role -> role.getReceivingTeams().stream())
                .filter(TeamEntity::isActive)
                .map(TeamEntity::getCode)
                .distinct()
                .sorted()
                .toList();

        boolean active = "ACTIVE".equalsIgnoreCase(user.getStatus());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getDisplayName(),
                active,
                roleCodes,
                permissionCodes,
                groupCodes,
                teamCodes,
                caseTypeCodes,
                initiatingTeamCodes,
                receivingTeamCodes);
    }

    /**
     * Effective roles = direct user_roles ∪ roles via active user_groups → group_roles.
     */
    public Set<RoleEntity> resolveEffectiveRoles(UserEntity user) {
        Stream<RoleEntity> direct = user.getRoles().stream().filter(RoleEntity::isActive);
        Stream<RoleEntity> viaGroups = user.getGroups().stream()
                .filter(GroupEntity::isActive)
                .flatMap(group -> group.getRoles().stream())
                .filter(RoleEntity::isActive);

        return Stream.concat(direct, viaGroups)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
