package com.athena.cases.notification;

public record NotifyTeamCommand(
        Long caseId,
        String teamCode,
        String message,
        String deepLink
) {
}
