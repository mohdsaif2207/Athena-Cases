package com.athena.cases.permission;

import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.security.AccessResolutionService;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC evaluation against IAM relationships resolved by {@link AccessResolutionService}.
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final AccessResolutionService accessResolutionService;

    public PermissionServiceImpl(
            UserRepository userRepository,
            AccessResolutionService accessResolutionService) {
        this.userRepository = userRepository;
        this.accessResolutionService = accessResolutionService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean check(String userId, String permissionCode) {
        return listPermissions(userId).contains(permissionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public void require(String userId, String permissionCode) {
        if (!check(userId, permissionCode)) {
            throw new ForbiddenException(permissionCode + " required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> listPermissions(String userId) {
        Long id;
        try {
            id = Long.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new ResourceNotFoundException("User", userId);
        }
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return new LinkedHashSet<>(accessResolutionService.toPrincipal(user).getPermissionCodes());
    }
}
