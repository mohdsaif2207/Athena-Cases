package com.athena.cases.security;

/**
 * Resolves the authenticated principal for service-layer use.
 * ExRT + platform auth share this port.
 */
public interface CurrentUserService {

    String requireUserId();

    String requireDisplayName();

    boolean hasPermission(String permissionCode);

    /** Full security principal (JWT-backed). Required by workflow/notification queue APIs. */
    UserPrincipal requirePrincipal();
}
