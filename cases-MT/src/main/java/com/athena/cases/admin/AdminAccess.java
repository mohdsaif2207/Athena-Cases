package com.athena.cases.admin;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SpEL helper: {@code @adminAccess.allow()} for Administration APIs.
 */
@Component("adminAccess")
public class AdminAccess {

    private static final String SYSTEM_ADMIN_ROLE = "SYSTEM_ADMINISTRATOR";

    public boolean allow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return principal.hasPermission(PermissionCodes.ADMIN_ACCESS)
                || principal.getRoleCodes().contains(SYSTEM_ADMIN_ROLE);
    }
}
