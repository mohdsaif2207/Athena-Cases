package com.athena.cases.notification.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.notification.NotifyUserCommand;
import com.athena.cases.notification.entity.Notification;
import com.athena.cases.notification.repository.NotificationRepository;
import com.athena.cases.security.CurrentUserService;

/**
 * Shared notification persistence adapter. Features call {@link NotificationService} only.
 * {@code deepLink} is accepted on commands but not persisted (no column in V1 schema).
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final int MESSAGE_MAX_LENGTH = 500;

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public void notifyTeam(NotifyTeamCommand command) {
        if (command == null || command.caseId() == null) {
            throw new IllegalStateException("caseId is required to notify a team");
        }
        if (isBlank(command.teamCode())) {
            throw new IllegalStateException("teamCode is required to notify a team");
        }
        if (isBlank(command.message())) {
            throw new IllegalStateException("message is required to notify a team");
        }

        persist(command.caseId(), command.teamCode().trim(), command.message());
    }

    @Override
    public void notifyUser(NotifyUserCommand command) {
        if (command == null || command.caseId() == null) {
            throw new IllegalStateException("caseId is required to notify a user");
        }
        if (isBlank(command.userId())) {
            throw new IllegalStateException("userId is required to notify a user");
        }
        if (isBlank(command.message())) {
            throw new IllegalStateException("message is required to notify a user");
        }

        // V1 schema has recipient_team only — store userId there until a user column exists.
        persist(command.caseId(), command.userId().trim(), command.message());
    }

    private void persist(Long caseId, String recipient, String message) {
        Instant now = Instant.now();
        String actor = currentUserService.requireUserId();

        Notification notification = new Notification();
        notification.setCaseId(caseId);
        notification.setRecipientTeam(recipient);
        notification.setMessage(truncateMessage(message));
        notification.setRead(false);
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notification.setCreatedBy(actor);
        notification.setUpdatedBy(actor);

        notificationRepository.save(notification);
    }

    private static String truncateMessage(String message) {
        String trimmed = message.trim();
        if (trimmed.length() <= MESSAGE_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MESSAGE_MAX_LENGTH);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
