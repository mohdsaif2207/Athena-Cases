package com.athena.cases.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal carried in the SecurityContext and JWT claims.
 * Authorization scopes are resolved dynamically from the database at login /me.
 */
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final boolean active;
    private final List<String> roleCodes;
    private final List<String> permissionCodes;
    private final List<String> groupCodes;
    private final List<String> teamCodes;
    private final List<String> caseTypeCodes;
    private final List<String> initiatingTeamCodes;
    private final List<String> receivingTeamCodes;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(
            Long userId,
            String username,
            String passwordHash,
            String displayName,
            boolean active,
            List<String> roleCodes,
            List<String> permissionCodes,
            List<String> groupCodes,
            List<String> teamCodes,
            List<String> caseTypeCodes,
            List<String> initiatingTeamCodes,
            List<String> receivingTeamCodes) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.active = active;
        this.roleCodes = List.copyOf(roleCodes);
        this.permissionCodes = List.copyOf(permissionCodes);
        this.groupCodes = List.copyOf(groupCodes);
        this.teamCodes = List.copyOf(teamCodes);
        this.caseTypeCodes = List.copyOf(caseTypeCodes);
        this.initiatingTeamCodes = List.copyOf(initiatingTeamCodes);
        this.receivingTeamCodes = List.copyOf(receivingTeamCodes);
        this.authorities = permissionCodes.stream()
                .map(code -> new SimpleGrantedAuthority("PERM_" + code))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public List<String> getGroupCodes() {
        return groupCodes;
    }

    public List<String> getTeamCodes() {
        return teamCodes;
    }

    public List<String> getCaseTypeCodes() {
        return caseTypeCodes;
    }

    public List<String> getInitiatingTeamCodes() {
        return initiatingTeamCodes;
    }

    public List<String> getReceivingTeamCodes() {
        return receivingTeamCodes;
    }

    public boolean hasPermission(String permissionCode) {
        return permissionCodes.contains(permissionCode);
    }

    public boolean canAccessCaseType(String caseTypeCode) {
        return caseTypeCodes.contains(caseTypeCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
