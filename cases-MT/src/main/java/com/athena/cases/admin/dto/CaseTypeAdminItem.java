package com.athena.cases.admin.dto;

import java.util.List;

public record CaseTypeAdminItem(
        Long id,
        String code,
        String name,
        String moduleKey,
        boolean active,
        List<String> initiatingTeamCodes,
        List<String> receivingTeamCodes
) {
}
