package com.athena.cases.security.service.impl;

import org.springframework.stereotype.Service;

import com.athena.cases.security.CurrentUserService;

/**
 * Temporary CurrentUserService until authentication is implemented.
 * Consistent with existing DBM temporary actor convention ({@code SYSTEM}).
 *
 * <p>TODO(auth): replace with JWT / session-backed implementation.
 */
@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private static final String SYSTEM_USER = "SYSTEM";

    @Override
    public String requireUserId() {
        return SYSTEM_USER;
    }

    @Override
    public String requireDisplayName() {
        return SYSTEM_USER;
    }

    @Override
    public boolean hasPermission(String permissionCode) {
        return true;
    }
}
