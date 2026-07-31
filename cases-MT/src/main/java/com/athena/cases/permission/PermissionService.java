package com.athena.cases.permission;

/**
 * Central RBAC evaluation. Permission codes are owned by the Lead / security package.
 */
public interface PermissionService {

    boolean check(String userId, String permissionCode);

    void require(String userId, String permissionCode);

    java.util.Set<String> listPermissions(String userId);
}
