package com.athena.cases.notification;

public record NotifyUserCommand(
        Long caseId,
        String userId,
        String message,
        String deepLink
) {
}
