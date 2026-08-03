package com.athena.cases.security;

/**
 * Resolves the authenticated principal for service-layer use.
 */
public interface CurrentUserService {

    String requireUserId();

    String requireDisplayName();

    boolean hasPermission(String permissionCode);

    UserPrincipal requirePrincipal();
}
