package com.athena.cases.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.TeamRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private WorkflowServiceImpl service;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(
                1L,
                "fm_admin",
                "hash",
                "ADMKL",
                true,
                List.of("SYSTEM_ADMINISTRATOR"),
                List.of("WF_VIEW"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("BILLING_OPS_TEAM"));
    }

    @Test
    void should_returnQueueRows_when_receivingTeamAuthorized() {
        when(currentUserService.hasPermission("WF_VIEW")).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(principal);

        TeamEntity team = new TeamEntity();
        team.setId(5L);
        team.setCode("BILLING_OPS_TEAM");
        when(teamRepository.findByCodeIn(List.of("BILLING_OPS_TEAM"))).thenReturn(List.of(team));

        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(9L);
        entity.setCaseId(1L);
        entity.setReceivingTeamId(5L);
        entity.setMessageKey("WF-BILLING-001");
        entity.setMessageId("MSG-9001");
        entity.setMessageName("Billing Workflow");
        entity.setStatus("Pending Assignment");
        entity.setPriority("Medium");
        entity.setReceivedAt(Instant.parse("2026-07-31T12:00:00Z"));
        when(workflowRepository.findAuthorized(anyList(), eq(false))).thenReturn(List.of(entity));

        List<WorkflowQueueItem> rows = service.listAuthorizedQueue();

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().workflowId()).isEqualTo("WF-9");
        assertThat(rows.getFirst().messageKey()).isEqualTo("WF-BILLING-001");
    }

    @Test
    void should_throwForbidden_when_missingWfView() {
        when(currentUserService.hasPermission("WF_VIEW")).thenReturn(false);

        assertThatThrownBy(() -> service.listAuthorizedQueue())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("WF_VIEW");
        verifyNoInteractions(workflowRepository);
    }
}
