package com.athena.cases.features.exrt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.features.exrt.constant.ExrtConstants;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.entity.CaseHeaderEntity;
import com.athena.cases.features.exrt.exception.ExrtBusinessException;
import com.athena.cases.features.exrt.mapper.ExrtCaseMapper;
import com.athena.cases.features.exrt.repository.CaseHeaderRepository;
import com.athena.cases.features.exrt.repository.ExrtCaseDetailRepository;
import com.athena.cases.features.exrt.serviceImpl.ExrtCaseServiceImpl;
import com.athena.cases.features.exrt.util.ExrtCaseNumberGenerator;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExrtCaseServiceTest {

    @Mock CaseHeaderRepository caseHeaderRepository;
    @Mock ExrtCaseDetailRepository exrtCaseDetailRepository;
    @Mock CaseRepository caseRepository;
    @Mock CaseTypeRepository caseTypeRepository;
    @Mock ExrtCaseNumberGenerator exrtCaseNumberGenerator;
    @Mock CurrentUserService currentUserService;
    @Mock WorkflowService workflowService;
    @Mock NotificationService notificationService;
    @Mock LookupService lookupService;

    ExrtCaseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExrtCaseServiceImpl(
                caseHeaderRepository,
                exrtCaseDetailRepository,
                caseRepository,
                caseTypeRepository,
                new ExrtCaseMapper(),
                exrtCaseNumberGenerator,
                currentUserService,
                workflowService,
                notificationService,
                lookupService
        );
    }

    @Test
    void should_throwForbidden_when_userLacksCreatePermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)).thenReturn(false);

        assertThatThrownBy(() -> service.create(sampleRequest()))
                .isInstanceOf(ExrtBusinessException.class)
                .hasMessageContaining("not permitted");
    }

    @Test
    void should_createCase_and_triggerWorkflow_when_requestValid() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)).thenReturn(true);
        when(currentUserService.requireUserId()).thenReturn("jdoe");
        when(currentUserService.requireDisplayName()).thenReturn("Jane Doe");
        when(exrtCaseNumberGenerator.next()).thenReturn("ExR000003");
        when(caseHeaderRepository.save(any(CaseHeaderEntity.class))).thenAnswer(inv -> {
            CaseHeaderEntity entity = inv.getArgument(0);
            entity.setId(42L);
            return entity;
        });
        CaseTypeEntity exrtType = new CaseTypeEntity();
        exrtType.setId(2L);
        exrtType.setCode("EXRT_REQUEST");
        when(caseTypeRepository.findByCode("EXRT_REQUEST")).thenReturn(Optional.of(exrtType));
        when(caseRepository.save(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity entity = inv.getArgument(0);
            entity.setId(100L);
            return entity;
        });
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(9L, 100L, ExrtConstants.RECEIVER_TEAM_DBM, "PENDING_ASSIGNMENT"));

        var response = service.create(sampleRequest());

        assertThat(response.caseId()).isEqualTo(42L);
        assertThat(response.caseNumber()).isEqualTo("ExR000003");
        assertThat(response.message()).isEqualTo("Case created successfully with Case ID ExR000003.");

        ArgumentCaptor<StartWorkflowCommand> wfCaptor = ArgumentCaptor.forClass(StartWorkflowCommand.class);
        verify(workflowService).start(wfCaptor.capture());
        assertThat(wfCaptor.getValue().caseId()).isEqualTo(100L);
        assertThat(wfCaptor.getValue().receiverTeamCode()).isEqualTo("DBM_TEAM");

        ArgumentCaptor<NotifyTeamCommand> ntfCaptor = ArgumentCaptor.forClass(NotifyTeamCommand.class);
        verify(notificationService).notifyTeam(ntfCaptor.capture());
        assertThat(ntfCaptor.getValue().caseId()).isEqualTo(100L);
        assertThat(ntfCaptor.getValue().teamCode()).isEqualTo("DBM_TEAM");

        ArgumentCaptor<CaseHeaderEntity> captor = ArgumentCaptor.forClass(CaseHeaderEntity.class);
        verify(caseHeaderRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getCaseNumber()).isEqualTo("ExR000003");
        assertThat(captor.getAllValues().stream().anyMatch(CaseHeaderEntity::isWorkflowTriggered)).isTrue();
    }

    private static ExrtCaseCreateRequest sampleRequest() {
        return new ExrtCaseCreateRequest(
                "C100",
                "Requested - ExRT",
                "Smith",
                "Jane",
                null,
                null,
                "TX",
                "5125551212",
                "P10",
                null,
                "CR01",
                "jane@example.com",
                "INQUIRY",
                null,
                "NO_ACTION_WAS_DEEMED_NECESSARY",
                "CALL",
                "BILLING",
                null,
                "CLAIMS",
                "RC_CLAIMS",
                null,
                null,
                null,
                false,
                "PHONE",
                null,
                "Refund inquiry",
                null,
                "Customer called about refund",
                "MEDIUM"
        );
    }
}
