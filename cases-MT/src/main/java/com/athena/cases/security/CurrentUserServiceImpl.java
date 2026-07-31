package com.athena.cases.security;

import com.athena.cases.common.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public String requireUserId() {
        return String.valueOf(requirePrincipal().getUserId());
    }

    @Override
    public String requireDisplayName() {
        return requirePrincipal().getDisplayName();
    }

    @Override
    public boolean hasPermission(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        return principal.getPermissionCodes().contains(permissionCode);
    }

    private UserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("Authenticated user required");
        }
        return principal;
    }
}
