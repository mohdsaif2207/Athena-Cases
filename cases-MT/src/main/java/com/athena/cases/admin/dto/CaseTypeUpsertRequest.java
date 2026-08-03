package com.athena.cases.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CaseTypeUpsertRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 64) String moduleKey,
        boolean active,
        List<String> initiatingTeamCodes,
        List<String> receivingTeamCodes
) {
}
