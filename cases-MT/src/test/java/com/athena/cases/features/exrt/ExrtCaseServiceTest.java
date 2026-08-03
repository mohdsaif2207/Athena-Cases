package com.athena.cases.features.exrt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.entity.CaseHeaderEntity;
import com.athena.cases.features.exrt.exception.ExrtBusinessException;
import com.athena.cases.features.exrt.mapper.ExrtCaseMapper;
import com.athena.cases.features.exrt.repository.CaseHeaderRepository;
import com.athena.cases.features.exrt.repository.ExrtCaseDetailRepository;
import com.athena.cases.features.exrt.serviceImpl.ExrtCaseServiceImpl;
import com.athena.cases.features.exrt.util.ExrtCaseNumberGenerator;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
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
        when(workflowService.start(any())).thenReturn(new WorkflowRef(9L, 42L, "DBM", "PENDING_ASSIGNMENT"));

        var response = service.create(sampleRequest());

        assertThat(response.caseId()).isEqualTo(42L);
        assertThat(response.caseNumber()).isEqualTo("ExR000003");
        assertThat(response.message()).isEqualTo("Case created successfully with Case ID ExR000003.");
        verify(workflowService).start(any());
        verify(notificationService).notifyTeam(any());
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
