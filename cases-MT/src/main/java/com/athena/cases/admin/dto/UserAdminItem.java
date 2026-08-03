package com.athena.cases.admin.dto;

import java.time.Instant;
import java.util.List;

public record UserAdminItem(
        Long id,
        String username,
        String displayName,
        String email,
        String status,
        List<String> roleCodes,
        List<String> groupCodes,
        List<String> teamCodes,
        List<String> effectivePermissionCodes,
        List<String> effectiveCaseTypeCodes,
        List<String> effectiveInitiatingTeamCodes,
        List<String> effectiveReceivingTeamCodes,
        Instant createdAt,
        Instant updatedAt
) {
}
