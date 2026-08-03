package com.athena.cases.admin.dto;

import java.util.List;

public record GroupAdminItem(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<String> roleCodes,
        List<String> usernames
) {
}
