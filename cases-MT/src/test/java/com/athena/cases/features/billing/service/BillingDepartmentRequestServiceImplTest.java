package com.athena.cases.features.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.athena.cases.casemanagement.CaseManagementService;
import com.athena.cases.casemanagement.CaseRef;
import com.athena.cases.casemanagement.UpdateCaseHeaderCommand;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.entity.BillingDepartmentRequest;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.exception.BillingConflictException;
import com.athena.cases.features.billing.exception.BillingResourceNotFoundException;
import com.athena.cases.features.billing.exception.BillingValidationException;
import com.athena.cases.features.billing.mapper.BillingDepartmentRequestMapper;
import com.athena.cases.features.billing.repository.BillingDepartmentRequestRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.permission.PermissionService;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Billing service unit tests — JUnit 5 + Mockito (docs: 02_Backend_Architecture / 09_Coding_Standards).
 * Shared ports are mocked; mapper is real.
 */
@ExtendWith(MockitoExtension.class)
class BillingDepartmentRequestServiceImplTest {

    @Mock private BillingDepartmentRequestRepository billingRepository;
    @Mock private CaseManagementService caseManagementService;
    @Mock private WorkflowService workflowService;
    @Mock private NotificationService notificationService;
    @Mock private LookupService lookupService;
    @Mock private CurrentUserService currentUserService;
    @Mock private PermissionService permissionService;

    private BillingDepartmentRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BillingDepartmentRequestServiceImpl(
                billingRepository,
                new BillingDepartmentRequestMapper(),
                caseManagementService,
                workflowService,
                notificationService,
                lookupService,
                currentUserService,
                permissionService
        );
    }

    @Test
    void should_throwBillingValidation_when_createBeforeSharedBusinessCaseIdExists() {
        when(currentUserService.requireUserId()).thenReturn("user-1");

        assertThatThrownBy(() -> service.create(minimalCreateRequest()))
                .isInstanceOf(BillingValidationException.class)
                .hasMessageContaining("businessCaseId");

        verify(permissionService).require("user-1", PermissionCodes.CASES_CREATE);
        verifyNoInteractions(caseManagementService, billingRepository, workflowService, notificationService);
    }

    @Test
    void should_requireCasesCreate_when_createInvoked() {
        doThrow(new RuntimeException("denied"))
                .when(permissionService).require("user-1", PermissionCodes.CASES_CREATE);
        when(currentUserService.requireUserId()).thenReturn("user-1");

        assertThatThrownBy(() -> service.create(minimalCreateRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("denied");

        verify(caseManagementService, never()).createCase(any());
    }

    @Test
    void should_returnResponse_when_getByCaseIdExists() {
        Long caseId = 42L;
        when(currentUserService.requireUserId()).thenReturn("user-1");
        when(caseManagementService.getCase(caseId)).thenReturn(new CaseRef(caseId, "CASE-42", 3L));
        BillingDepartmentRequest entity = persistedEntity(caseId, "BIL000001");
        when(billingRepository.findByCaseId(caseId)).thenReturn(Optional.of(entity));
        when(workflowService.getByCaseId(caseId))
                .thenReturn(new WorkflowRef(9L, caseId, "BILLING_OPS", "PENDING_ASSIGNMENT"));

        BillingDepartmentRequestResponse response = service.getByCaseId(caseId);

        assertThat(response.caseId()).isEqualTo(caseId);
        assertThat(response.caseNumber()).isEqualTo("CASE-42");
        assertThat(response.businessCaseId()).isEqualTo("BIL000001");
        assertThat(response.version()).isEqualTo(3);
        assertThat(response.workflowId()).isEqualTo(9L);
        assertThat(response.workflowStatus()).isEqualTo("PENDING_ASSIGNMENT");
        verify(permissionService).require("user-1", PermissionCodes.CASES_VIEW);
    }

    @Test
    void should_throwNotFound_when_billingExtensionMissing() {
        Long caseId = 99L;
        when(currentUserService.requireUserId()).thenReturn("user-1");
        when(caseManagementService.getCase(caseId)).thenReturn(new CaseRef(caseId, "CASE-99", 1L));
        when(billingRepository.findByCaseId(caseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCaseId(caseId))
                .isInstanceOf(BillingResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void should_throwConflict_when_updateVersionMismatch() {
        Long caseId = 7L;
        when(currentUserService.requireUserId()).thenReturn("user-1");
        when(caseManagementService.getCase(caseId)).thenReturn(new CaseRef(caseId, "CASE-7", 2L));
        when(billingRepository.findByCaseId(caseId))
                .thenReturn(Optional.of(persistedEntity(caseId, "BIL000007")));

        assertThatThrownBy(() -> service.update(caseId, minimalUpdateRequest(1)))
                .isInstanceOf(BillingConflictException.class)
                .hasMessageContaining("Version conflict");

        verify(caseManagementService, never()).updateCaseHeader(eq(caseId), any(UpdateCaseHeaderCommand.class));
        verify(billingRepository, never()).save(any());
    }

    @Test
    void should_persistUpdate_when_versionsMatch() {
        Long caseId = 7L;
        when(currentUserService.requireUserId()).thenReturn("user-1");
        when(caseManagementService.getCase(caseId)).thenReturn(new CaseRef(caseId, "CASE-7", 2L));
        BillingDepartmentRequest entity = persistedEntity(caseId, "BIL000007");
        when(billingRepository.findByCaseId(caseId)).thenReturn(Optional.of(entity));
        when(caseManagementService.updateCaseHeader(eq(caseId), any(UpdateCaseHeaderCommand.class)))
                .thenReturn(new CaseRef(caseId, "CASE-7", 3L));
        when(billingRepository.save(entity)).thenReturn(entity);
        when(workflowService.getByCaseId(caseId)).thenThrow(new RuntimeException("none"));

        BillingDepartmentRequestResponse response = service.update(caseId, minimalUpdateRequest(2));

        assertThat(response.caseId()).isEqualTo(caseId);
        assertThat(response.version()).isEqualTo(3);
        assertThat(response.requestDescription()).isEqualTo("Updated description");
        verify(permissionService).require("user-1", PermissionCodes.CASES_EDIT);
        verify(billingRepository).save(entity);
    }

    @Test
    void should_throwValidation_when_campaignNotActive() {
        Long caseId = 7L;
        when(currentUserService.requireUserId()).thenReturn("user-1");
        when(caseManagementService.getCase(caseId)).thenReturn(new CaseRef(caseId, "CASE-7", 1L));
        when(billingRepository.findByCaseId(caseId))
                .thenReturn(Optional.of(persistedEntity(caseId, "BIL000007")));
        when(lookupService.listActiveCampaigns()).thenReturn(List.of(
                new LookupItem("1", "CAMP-A", "Campaign A")
        ));

        BillingDepartmentRequestUpdateRequest request = new BillingDepartmentRequestUpdateRequest(
                BillingRequestType.RESEARCH,
                null,
                "UNKNOWN-CAMP",
                null,
                "Medium",
                "Requested",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Updated description",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1
        );

        assertThatThrownBy(() -> service.update(caseId, request))
                .isInstanceOf(BillingValidationException.class)
                .satisfies(ex -> assertThat(((BillingValidationException) ex).getField()).isEqualTo("campaignId"));
    }

    @Test
    void should_returnAllHoldLevelCodes_when_listHoldLevels() {
        List<BillingHoldLevelCode> codes = service.listHoldLevels(BillingHoldType.CLIENT_LEVEL);

        assertThat(codes).containsExactly(BillingHoldLevelCode.values());
    }

    @Test
    void should_returnEmptyList_when_listAssigneesUntilLookupApiExists() {
        assertThat(service.listAssignees()).isEmpty();
    }

    private static BillingDepartmentRequestCreateRequest minimalCreateRequest() {
        return new BillingDepartmentRequestCreateRequest(
                BillingRequestType.RESEARCH,
                null,
                null,
                null,
                "Medium",
                "Requested",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Need research on billing reject",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static BillingDepartmentRequestUpdateRequest minimalUpdateRequest(int version) {
        return new BillingDepartmentRequestUpdateRequest(
                BillingRequestType.RESEARCH,
                null,
                null,
                null,
                "Medium",
                "Requested",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Updated description",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                version
        );
    }

    private static BillingDepartmentRequest persistedEntity(Long caseId, String businessCaseId) {
        BillingDepartmentRequest entity = new BillingDepartmentRequest();
        entity.setId(1L);
        entity.setCaseId(caseId);
        entity.setBusinessCaseId(businessCaseId);
        entity.setRequestType(BillingRequestType.RESEARCH);
        entity.setRequestDescription("Need research on billing reject");
        entity.setVersion(1);
        return entity;
    }
}
