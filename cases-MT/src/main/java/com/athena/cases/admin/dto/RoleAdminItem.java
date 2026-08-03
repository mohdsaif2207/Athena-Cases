package com.athena.cases.admin.dto;

import java.util.List;

public record RoleAdminItem(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<String> permissionCodes,
        List<String> caseTypeCodes,
        List<String> initiatingTeamCodes,
        List<String> receivingTeamCodes
) {
}
