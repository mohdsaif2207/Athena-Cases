package com.athena.cases.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UserUpsertRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(max = 120) String displayName,
        @Size(max = 255) String email,
        @Size(max = 100) String password,
        @NotBlank String status,
        List<String> roleCodes,
        List<String> groupCodes,
        List<String> teamCodes
) {
}
