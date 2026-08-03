package com.athena.cases.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GroupUpsertRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        boolean active,
        List<String> roleCodes,
        List<String> usernames
) {
}
