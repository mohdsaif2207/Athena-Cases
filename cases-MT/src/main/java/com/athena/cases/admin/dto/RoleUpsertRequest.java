package com.athena.cases.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoleUpsertRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        boolean active,
        List<String> permissionCodes,
        List<String> caseTypeCodes,
        List<String> initiatingTeamCodes,
        List<String> receivingTeamCodes
) {
}
