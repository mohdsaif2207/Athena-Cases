package com.athena.cases.admin.dto;

public record TeamAdminItem(
        Long id,
        String code,
        String name,
        String teamType,
        boolean active
) {
}
