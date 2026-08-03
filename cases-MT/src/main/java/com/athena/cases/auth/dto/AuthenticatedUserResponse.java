package com.athena.cases.auth.dto;

import java.util.List;

public record AuthenticatedUserResponse(
        Long id,
        String username,
        String displayName,
        List<String> roles,
        List<String> permissions,
        List<String> groups,
        List<String> teams,
        List<String> caseTypes,
        List<String> initiatingTeams,
        List<String> receivingTeams
) {
}
