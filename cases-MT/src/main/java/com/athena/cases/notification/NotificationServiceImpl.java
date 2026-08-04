package com.athena.cases.notification;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.TeamRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import com.athena.cases.workflow.WorkflowRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final CaseRepository caseRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserService currentUserService;
    private final WorkflowRepository workflowRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            CaseRepository caseRepository,
            TeamRepository teamRepository,
            CurrentUserService currentUserService,
            WorkflowRepository workflowRepository) {
        this.notificationRepository = notificationRepository;
        this.caseRepository = caseRepository;
        this.teamRepository = teamRepository;
        this.currentUserService = currentUserService;
        this.workflowRepository = workflowRepository;
    }

    @Override
    @Transactional
    public void notifyTeam(NotifyTeamCommand command) {
        TeamEntity team = teamRepository.findByCode(command.teamCode())
                .orElseThrow(() -> new ResourceNotFoundException("Team", command.teamCode()));

        // Platform `cases` row optional — ExRT persists in case_header with its own id
        CaseEntity caseEntity = caseRepository.findById(command.caseId()).orElse(null);
        String caseNumber = caseEntity != null ? caseEntity.getCaseNumber() : "CASE-" + command.caseId();
        String subject = caseEntity != null ? caseEntity.getSubject() : "ExRT Request";

        Instant now = Instant.now();
        String actor = currentUserService.requirePrincipal().getUsername();
        NotificationEntity entity = new NotificationEntity();
        entity.setCaseId(command.caseId());
        entity.setReceivingTeamId(team.getId());
        entity.setMessageKey("NTF-" + caseNumber);
        entity.setMessageId("MSG-" + command.caseId());
        entity.setMessageName(subject + " Notice");
        entity.setMessageObject(caseNumber);
        entity.setMessage(command.message());
        entity.setReceivedAt(now);
        entity.setDetails(command.deepLink());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        entity.setVersion(1);

        notificationRepository.save(entity);
        log.info("team notification created - caseId={} team={}", command.caseId(), team.getCode());
    }

    @Override
    @Transactional
    public void notifyUser(NotifyUserCommand command) {
        // User-targeted channel reuses team queue storage until a dedicated user inbox lands.
        log.info("notifyUser deferred to team channel - caseId={} userId={}",
                command.caseId(), command.userId());
        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> teamCodes = principal.getReceivingTeamCodes();
        if (teamCodes.isEmpty()) {
            throw new ForbiddenException("No receiving team available for notification");
        }
        notifyTeam(new NotifyTeamCommand(
                command.caseId(),
                teamCodes.getFirst(),
                command.message(),
                command.deepLink()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueueItem> listAuthorizedQueue() {
        requireNotifView();
        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> receivingTeamCodes = principal.getReceivingTeamCodes();
        if (receivingTeamCodes.isEmpty()) {
            return List.of();
        }

        List<Long> teamIds = teamRepository.findByCodeIn(receivingTeamCodes).stream()
                .map(TeamEntity::getId)
                .toList();
        if (teamIds.isEmpty()) {
            return List.of();
        }

        return notificationRepository.findAuthorized(teamIds, false).stream()
                .map(this::toQueueItem)
                .toList();
    }

    @Override
    @Transactional
    public NotificationQueueItem updateQueueItem(Long notificationId, UpdateNotificationCommand command) {
        requireNotifView();
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", String.valueOf(notificationId)));
        assertReceivingTeamAccess(entity.getReceivingTeamId());

        if (command.messageName() != null && !command.messageName().isBlank()) {
            entity.setMessageName(command.messageName().trim());
        }
        if (command.message() != null && !command.message().isBlank()) {
            entity.setMessage(command.message().trim());
        }
        if (command.details() != null) {
            entity.setDetails(command.details().trim());
        }
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(currentUserService.requirePrincipal().getUsername());

        NotificationEntity saved = notificationRepository.save(entity);
        syncRelatedWorkflowMessage(saved);
        log.info("notification queue updated - notificationId={} user={}",
                saved.getId(), entity.getUpdatedBy());
        return toQueueItem(saved);
    }

    private void syncRelatedWorkflowMessage(NotificationEntity notification) {
        // Keep the latest workflow message name aligned when notification title/body changes.
        workflowRepository.findFirstByCaseIdOrderByReceivedAtDesc(notification.getCaseId())
                .ifPresent(w -> {
                    if (notification.getMessageName() != null && !notification.getMessageName().isBlank()) {
                        w.setMessageName(notification.getMessageName());
                    }
                    if (notification.getMessage() != null && !notification.getMessage().isBlank()) {
                        w.setLogs(notification.getMessage());
                    }
                    w.setUpdatedAt(Instant.now());
                    w.setUpdatedBy(currentUserService.requirePrincipal().getUsername());
                    workflowRepository.save(w);
                });
    }

    private void assertReceivingTeamAccess(Long receivingTeamId) {
        TeamEntity team = teamRepository.findById(receivingTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", String.valueOf(receivingTeamId)));
        if (!currentUserService.requirePrincipal().getReceivingTeamCodes().contains(team.getCode())) {
            throw new ForbiddenException("Not authorized for receiving team " + team.getCode());
        }
    }

    private NotificationQueueItem toQueueItem(NotificationEntity n) {
        String teamCode = teamRepository.findById(n.getReceivingTeamId())
                .map(TeamEntity::getCode)
                .orElse("");
        return new NotificationQueueItem(
                n.getId(),
                "NTF-" + n.getId(),
                n.getCaseId(),
                n.getMessageKey(),
                n.getMessageId(),
                n.getMessageName(),
                nullToEmpty(n.getMessageObject()),
                n.getMessage(),
                n.getReceivedAt(),
                n.getUpdatedAt(),
                nullToEmpty(n.getDetails()),
                nullToEmpty(n.getCreatedBy()),
                "",
                "",
                teamCode);
    }

    private void requireNotifView() {
        if (!currentUserService.hasPermission(PermissionCodes.NOTIF_VIEW)) {
            throw new ForbiddenException("NOTIF_VIEW required");
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
