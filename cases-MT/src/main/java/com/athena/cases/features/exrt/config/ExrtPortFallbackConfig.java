package com.athena.cases.features.exrt.config;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.notification.NotifyUserCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporary port fallbacks so ExRT can run before Lead ships real adapters.
 * Real beans from Login / Workflow / Notification modules replace these automatically.
 */
@Configuration
public class ExrtPortFallbackConfig {

    private static final Logger log = LoggerFactory.getLogger(ExrtPortFallbackConfig.class);

    @Bean
    @ConditionalOnMissingBean(CurrentUserService.class)
    CurrentUserService exrtDevCurrentUserService() {
        return new CurrentUserService() {
            @Override
            public String requireUserId() {
                return "dev.user";
            }

            @Override
            public String requireDisplayName() {
                return "Dev User";
            }

            @Override
            public boolean hasPermission(String permissionCode) {
                return PermissionCodes.CASES_CREATE.equals(permissionCode)
                        || PermissionCodes.CASES_VIEW.equals(permissionCode);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowService.class)
    WorkflowService exrtDevWorkflowService() {
        AtomicLong seq = new AtomicLong(1);
        Map<Long, WorkflowRef> byCase = new ConcurrentHashMap<>();
        return new WorkflowService() {
            @Override
            public WorkflowRef start(StartWorkflowCommand command) {
                WorkflowRef existing = byCase.get(command.caseId());
                if (existing != null) {
                    log.info("workflow already exists for caseId={}", command.caseId());
                    return existing;
                }
                WorkflowRef created = new WorkflowRef(
                        seq.getAndIncrement(),
                        command.caseId(),
                        command.receiverTeamCode(),
                        command.initialStatusCode()
                );
                byCase.put(command.caseId(), created);
                log.info("dev workflow started - caseId={}, team={}, status={}",
                        command.caseId(), command.receiverTeamCode(), command.initialStatusCode());
                return created;
            }

            @Override
            public WorkflowRef getByCaseId(Long caseId) {
                WorkflowRef ref = byCase.get(caseId);
                if (ref == null) {
                    throw new IllegalStateException("Workflow not found for case " + caseId);
                }
                return ref;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    NotificationService exrtDevNotificationService() {
        return new NotificationService() {
            @Override
            public void notifyTeam(NotifyTeamCommand command) {
                log.info("dev notifyTeam - caseId={}, team={}, message={}",
                        command.caseId(), command.teamCode(), command.message());
            }

            @Override
            public void notifyUser(NotifyUserCommand command) {
                log.info("dev notifyUser - caseId={}, userId={}", command.caseId(), command.userId());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(LookupService.class)
    LookupService exrtDevLookupService() {
        return new LookupService() {
            @Override
            public List<LookupItem> searchClients(String query) {
                return List.of(
                        new LookupItem("C100", "C100", "Acme Credit Union"),
                        new LookupItem("C200", "C200", "Summit Bank"),
                        new LookupItem("C300", "C300", "Harbor Financial")
                );
            }

            @Override
            public List<LookupItem> listActiveCampaigns() {
                return List.of();
            }

            @Override
            public List<LookupItem> listSegments(String clientId) {
                return List.of();
            }

            @Override
            public List<LookupItem> listProducts(String query) {
                return List.of(
                        new LookupItem("P10", "P10", "Term Life"),
                        new LookupItem("P20", "P20", "Disability"),
                        new LookupItem("P30", "P30", "Accident")
                );
            }

            @Override
            public List<LookupItem> findParentCases(String query) {
                return List.of();
            }
        };
    }
}
