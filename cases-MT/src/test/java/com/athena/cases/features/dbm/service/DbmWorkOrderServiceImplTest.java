package com.athena.cases.features.dbm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.features.dbm.DbmConstants;
import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.entity.DbmWorkOrder;
import com.athena.cases.features.dbm.repository.DbmWorkOrderRepository;
import com.athena.cases.features.dbm.service.impl.DbmWorkOrderServiceImpl;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.TeamEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;

/**
 * DBM Work Order Request service unit tests — maps to US Scenarios 4, 6, 8, 10, 12, 13.
 */
@ExtendWith(MockitoExtension.class)
class DbmWorkOrderServiceImplTest {

    @Mock private CaseRepository caseRepository;
    @Mock private CaseTypeRepository caseTypeRepository;
    @Mock private DbmWorkOrderRepository dbmWorkOrderRepository;
    @Mock private WorkflowService workflowService;
    @Mock private NotificationService notificationService;
    @Mock private CurrentUserService currentUserService;

    private DbmWorkOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DbmWorkOrderServiceImpl(
                caseRepository,
                caseTypeRepository,
                dbmWorkOrderRepository,
                workflowService,
                notificationService,
                currentUserService);
    }

    @Test
    void should_createCase_when_mandatoryFieldsProvided() {
        stubInitiatingUserWithCreatePermission();
        CaseTypeEntity caseType = seededCaseTypeWithDbmTeam();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(caseType));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.of("DBM000010"));
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> {
            DbmWorkOrder d = inv.getArgument(0);
            d.setCaseId(42L);
            return d;
        });
        when(workflowService.start(any(StartWorkflowCommand.class)))
                .thenReturn(new WorkflowRef(9L, 42L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        DbmWorkOrderResponse response = service.create(minimalCreateRequest("Account Update File"));

        assertThat(response.caseId()).isEqualTo(42L);
        assertThat(response.caseNumber()).isEqualTo("DBM000011");
        assertThat(response.caseOwner()).isEqualTo("Ada Lovelace");
        assertThat(response.vendor()).isEqualTo("Acxiom");
        assertThat(response.priority()).isEqualTo("Medium");
        assertThat(response.status()).isEqualTo("Requested");
        assertThat(response.frequency()).isEqualTo("Once");
        assertThat(response.pendingDbmApproval()).isFalse();
        assertThat(response.dbmWorkOrderNumber()).isNull();
        assertThat(response.coverageLevels()).containsExactly("Complementary");
        assertThat(response.requestedAccountTypes()).containsExactly("Share/ESHAR");
        assertThat(response.spokenKeys()).containsExactly("SK-001");

        ArgumentCaptor<CaseEntity> caseCaptor = ArgumentCaptor.forClass(CaseEntity.class);
        verify(caseRepository).saveAndFlush(caseCaptor.capture());
        assertThat(caseCaptor.getValue().getCaseNumber()).isEqualTo("DBM000011");
        assertThat(caseCaptor.getValue().getCaseOwner()).isEqualTo("Ada Lovelace");
        verify(currentUserService).hasPermission(PermissionCodes.CASES_CREATE);
    }

    @Test
    void should_startWorkflowAndNotifyTeam_when_createSucceeds() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(1L, 1L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        service.create(minimalCreateRequest("Account Update File"));

        ArgumentCaptor<StartWorkflowCommand> wfCaptor = ArgumentCaptor.forClass(StartWorkflowCommand.class);
        verify(workflowService).start(wfCaptor.capture());
        assertThat(wfCaptor.getValue().caseId()).isEqualTo(1L);
        assertThat(wfCaptor.getValue().workflowType()).isEqualTo("DBM_WORK_ORDER_REQUEST");
        assertThat(wfCaptor.getValue().receiverTeamCode()).isEqualTo(DbmConstants.RECEIVER_TEAM_DBM);
        assertThat(wfCaptor.getValue().initialStatusCode())
                .isEqualTo(DbmConstants.WORKFLOW_STATUS_PENDING_ASSIGNMENT);

        ArgumentCaptor<NotifyTeamCommand> ntfCaptor = ArgumentCaptor.forClass(NotifyTeamCommand.class);
        verify(notificationService).notifyTeam(ntfCaptor.capture());
        assertThat(ntfCaptor.getValue().teamCode()).isEqualTo(DbmConstants.RECEIVER_TEAM_DBM);
        assertThat(ntfCaptor.getValue().message())
                .isEqualTo(DbmConstants.newCaseNotificationMessage("DBM000001"));
        assertThat(ntfCaptor.getValue().deepLink()).isEqualTo(DbmConstants.CASE_DEEP_LINK);
    }

    @Test
    void should_setPendingDbmApproval_when_transferTypeIsCustom() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(1L, 5L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        DbmWorkOrderResponse response = service.create(
                minimalCreateRequest(DbmConstants.TRANSFER_TYPE_CUSTOM));

        assertThat(response.pendingDbmApproval()).isTrue();
        ArgumentCaptor<CaseEntity> caseCaptor = ArgumentCaptor.forClass(CaseEntity.class);
        verify(caseRepository).saveAndFlush(caseCaptor.capture());
        assertThat(caseCaptor.getValue().isPendingDbmApproval()).isTrue();
    }

    @Test
    void should_setPendingDbmApproval_when_transferTypeIsCustomRequiresApproval() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(6L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(1L, 6L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        DbmWorkOrderResponse response = service.create(
                minimalCreateRequest(DbmConstants.TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL));

        assertThat(response.pendingDbmApproval()).isTrue();
    }

    @Test
    void should_throwForbidden_when_createWithoutPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)).thenReturn(false);

        assertThatThrownBy(() -> service.create(minimalCreateRequest("Account Update File")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(PermissionCodes.CASES_CREATE);

        verifyNoInteractions(caseRepository, dbmWorkOrderRepository, workflowService, notificationService);
    }

    @Test
    void should_throwIllegalState_when_caseTypeNotSeeded() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(minimalCreateRequest("Account Update File")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Case type not seeded");

        verify(caseRepository, never()).saveAndFlush(any());
        verifyNoInteractions(workflowService, notificationService);
    }

    @Test
    void should_skipInactiveReceivingTeam_when_create() {
        stubInitiatingUserWithCreatePermission();
        CaseTypeEntity caseType = seededCaseTypeWithDbmTeam();
        caseType.getReceivingTeams().iterator().next().setActive(false);
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(caseType));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(minimalCreateRequest("Account Update File"));

        verify(workflowService, never()).start(any());
        verify(notificationService, never()).notifyTeam(any());
    }

    @Test
    void should_returnCase_when_getByCaseIdExists() {
        stubInitiatingUserWithViewPermission();
        CaseEntity caseEntity = existingCase(10L, "DBM000010", false);
        DbmWorkOrder detail = existingDbm(caseEntity, "Account Update File");
        detail.setDbmWorkOrderNumber("WO-999");
        detail.setDbmCompletionNotes("secret notes");
        detail.setTotalRecordsUpdated(100);

        when(caseRepository.findById(10L)).thenReturn(Optional.of(caseEntity));
        when(dbmWorkOrderRepository.findByCaseId(10L)).thenReturn(Optional.of(detail));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));

        DbmWorkOrderResponse response = service.getByCaseId(10L);

        assertThat(response.caseId()).isEqualTo(10L);
        assertThat(response.caseNumber()).isEqualTo("DBM000010");
        assertThat(response.dbmWorkOrderNumber()).isNull();
        assertThat(response.dbmCompletionNotes()).isNull();
        assertThat(response.totalRecordsUpdated()).isNull();
        verify(currentUserService).hasPermission(PermissionCodes.CASES_VIEW);
    }

    @Test
    void should_exposeSection4_when_dbmReceivingUserGetsCase() {
        stubDbmUserWithViewPermission();
        CaseEntity caseEntity = existingCase(10L, "DBM000010", false);
        DbmWorkOrder detail = existingDbm(caseEntity, "Account Update File");
        detail.setDbmWorkOrderNumber("WO-999");
        detail.setDbmCompletionNotes("internal");
        detail.setTotalRecordsUpdated(50);

        when(caseRepository.findById(10L)).thenReturn(Optional.of(caseEntity));
        when(dbmWorkOrderRepository.findByCaseId(10L)).thenReturn(Optional.of(detail));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));

        DbmWorkOrderResponse response = service.getByCaseId(10L);

        assertThat(response.dbmWorkOrderNumber()).isEqualTo("WO-999");
        assertThat(response.dbmCompletionNotes()).isEqualTo("internal");
        assertThat(response.totalRecordsUpdated()).isEqualTo(50);
    }

    @Test
    void should_throwNotFound_when_caseMissingOnGet() {
        stubInitiatingUserWithViewPermission();
        when(caseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCaseId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void should_throwNotFound_when_dbmDetailMissingOnGet() {
        stubInitiatingUserWithViewPermission();
        when(caseRepository.findById(99L)).thenReturn(Optional.of(existingCase(99L, "DBM000099", false)));
        when(dbmWorkOrderRepository.findByCaseId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCaseId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DBM Work Order");
    }

    @Test
    void should_throwForbidden_when_getWithoutPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)).thenReturn(false);

        assertThatThrownBy(() -> service.getByCaseId(1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(PermissionCodes.CASES_VIEW);

        verify(caseRepository, never()).findById(any());
    }

    @Test
    void should_updateCaseAndNotify_when_editSucceeds() {
        stubInitiatingUserWithEditPermission();
        CaseEntity caseEntity = existingCase(20L, "DBM000020", false);
        DbmWorkOrder detail = existingDbm(caseEntity, "Account Update File");
        detail.setDbmWorkOrderNumber("KEEP-ME");

        when(caseRepository.findById(20L)).thenReturn(Optional.of(caseEntity));
        when(dbmWorkOrderRepository.findByCaseId(20L)).thenReturn(Optional.of(detail));
        when(caseRepository.save(any(CaseEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dbmWorkOrderRepository.save(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));

        UpdateDbmWorkOrderRequest request = updateRequest(
                DbmConstants.TRANSFER_TYPE_CUSTOM,
                "WO-HACK",
                "hacked notes",
                999);

        DbmWorkOrderResponse response = service.update(20L, request);

        assertThat(response.pendingDbmApproval()).isTrue();
        assertThat(response.subject()).isEqualTo("Updated subject");
        assertThat(response.dbmWorkOrderNumber()).isNull();
        assertThat(detail.getDbmWorkOrderNumber()).isEqualTo("KEEP-ME");

        verify(workflowService, never()).start(any());
        ArgumentCaptor<NotifyTeamCommand> ntfCaptor = ArgumentCaptor.forClass(NotifyTeamCommand.class);
        verify(notificationService).notifyTeam(ntfCaptor.capture());
        assertThat(ntfCaptor.getValue().message())
                .isEqualTo(DbmConstants.updatedCaseNotificationMessage("DBM000020"));
        verify(currentUserService).hasPermission(PermissionCodes.CASES_EDIT);
    }

    @Test
    void should_allowSection4Update_when_dbmReceivingUserEdits() {
        stubDbmUserWithEditPermission();
        CaseEntity caseEntity = existingCase(21L, "DBM000021", false);
        DbmWorkOrder detail = existingDbm(caseEntity, "Account Update File");

        when(caseRepository.findById(21L)).thenReturn(Optional.of(caseEntity));
        when(dbmWorkOrderRepository.findByCaseId(21L)).thenReturn(Optional.of(detail));
        when(caseRepository.save(any(CaseEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dbmWorkOrderRepository.save(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));

        DbmWorkOrderResponse response = service.update(21L, updateRequest(
                "Account Update File", "WO-100", "done", 25));

        assertThat(response.dbmWorkOrderNumber()).isEqualTo("WO-100");
        assertThat(response.dbmCompletionNotes()).isEqualTo("done");
        assertThat(response.totalRecordsUpdated()).isEqualTo(25);
    }

    @Test
    void should_replaceMultiSelectChildren_when_update() {
        stubInitiatingUserWithEditPermission();
        CaseEntity caseEntity = existingCase(22L, "DBM000022", false);
        DbmWorkOrder detail = existingDbm(caseEntity, "Account Update File");

        when(caseRepository.findById(22L)).thenReturn(Optional.of(caseEntity));
        when(dbmWorkOrderRepository.findByCaseId(22L)).thenReturn(Optional.of(detail));
        when(caseRepository.save(any(CaseEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dbmWorkOrderRepository.save(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(caseTypeRepository.findById(1L)).thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));

        UpdateDbmWorkOrderRequest request = new UpdateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 9, 1),
                "High",
                "Updated subject",
                "In Progress",
                "notes",
                false,
                "Cancel File",
                List.of("Voluntary", "Cancels"),
                "No",
                "Yes",
                List.of("Checking/ECHK"),
                10,
                "Weekly",
                null,
                "C100",
                List.of("SK-002", "SK-002", "  "),
                "E1",
                "M1",
                "2026-09",
                2,
                true,
                "criteria",
                "field",
                "change",
                null,
                null,
                null);

        DbmWorkOrderResponse response = service.update(22L, request);

        assertThat(response.coverageLevels()).containsExactlyInAnyOrder("Voluntary", "Cancels");
        assertThat(response.requestedAccountTypes()).containsExactly("Checking/ECHK");
        assertThat(response.spokenKeys()).containsExactly("SK-002");
        assertThat(response.frequency()).isEqualTo("Weekly");
    }

    @Test
    void should_throwForbidden_when_updateWithoutPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_EDIT)).thenReturn(false);

        assertThatThrownBy(() -> service.update(1L, updateRequest("Account Update File", null, null, null)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(PermissionCodes.CASES_EDIT);

        verifyNoInteractions(caseRepository, dbmWorkOrderRepository, notificationService);
    }

    @Test
    void should_defaultFrequencyToOnce_when_blankOnCreate() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(30L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(1L, 30L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        CreateDbmWorkOrderRequest request = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject line",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "  ",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        DbmWorkOrderResponse response = service.create(request);

        assertThat(response.frequency()).isEqualTo("Once");
    }

    @Test
    void should_allocateFirstCaseNumber_when_noExistingDbmCases() {
        stubInitiatingUserWithCreatePermission();
        when(caseTypeRepository.findByCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name()))
                .thenReturn(Optional.of(seededCaseTypeWithDbmTeam()));
        when(caseRepository.findMaxDbmCaseNumber()).thenReturn(Optional.empty());
        when(caseRepository.saveAndFlush(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(dbmWorkOrderRepository.saveAndFlush(any(DbmWorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.start(any())).thenReturn(
                new WorkflowRef(1L, 1L, "DBM_WORK_ORDER_REQUEST", "DBM_TEAM", "Pending Assignment"));

        DbmWorkOrderResponse response = service.create(minimalCreateRequest("Account Update File"));

        assertThat(response.caseNumber()).isEqualTo("DBM000001");
    }

    // --- helpers ---

    private void stubInitiatingUserWithCreatePermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)).thenReturn(true);
        when(currentUserService.requireDisplayName()).thenReturn("Ada Lovelace");
        when(currentUserService.requirePrincipal()).thenReturn(initiatingPrincipal());
    }

    private void stubInitiatingUserWithViewPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(initiatingPrincipal());
    }

    private void stubInitiatingUserWithEditPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_EDIT)).thenReturn(true);
        when(currentUserService.requireDisplayName()).thenReturn("Ada Lovelace");
        when(currentUserService.requirePrincipal()).thenReturn(initiatingPrincipal());
    }

    private void stubDbmUserWithViewPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(dbmPrincipal());
    }

    private void stubDbmUserWithEditPermission() {
        when(currentUserService.hasPermission(PermissionCodes.CASES_EDIT)).thenReturn(true);
        when(currentUserService.requireDisplayName()).thenReturn("DBM Operator");
        when(currentUserService.requirePrincipal()).thenReturn(dbmPrincipal());
    }

    private static UserPrincipal initiatingPrincipal() {
        return new UserPrincipal(
                1L, "ada", "hash", "Ada Lovelace", true,
                List.of("CLIENT_SUCCESS_USER"),
                List.of(PermissionCodes.CASES_CREATE, PermissionCodes.CASES_VIEW, PermissionCodes.CASES_EDIT),
                List.of(),
                List.of("CLIENT_SUCCESS"),
                List.of("DBM_WORK_ORDER_REQUEST"),
                List.of("CLIENT_SUCCESS"),
                List.of());
    }

    private static UserPrincipal dbmPrincipal() {
        return new UserPrincipal(
                2L, "saif", "hash", "DBM Operator", true,
                List.of("DBM_CASE_USER"),
                List.of(PermissionCodes.CASES_CREATE, PermissionCodes.CASES_VIEW, PermissionCodes.CASES_EDIT),
                List.of(),
                List.of(DbmConstants.RECEIVER_TEAM_DBM),
                List.of("DBM_WORK_ORDER_REQUEST"),
                List.of("CLIENT_SUCCESS"),
                List.of(DbmConstants.RECEIVER_TEAM_DBM));
    }

    private static CaseTypeEntity seededCaseTypeWithDbmTeam() {
        CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setId(1L);
        caseType.setCode(CaseTypeCode.DBM_WORK_ORDER_REQUEST.name());
        caseType.setName("DBM Work Order Request");
        caseType.setModuleKey("DBM");
        caseType.setActive(true);

        TeamEntity dbmTeam = new TeamEntity();
        dbmTeam.setId(10L);
        dbmTeam.setCode(DbmConstants.RECEIVER_TEAM_DBM);
        dbmTeam.setName("DBM");
        dbmTeam.setTeamType("RECEIVING");
        dbmTeam.setActive(true);

        caseType.setReceivingTeams(new HashSet<>(Set.of(dbmTeam)));
        return caseType;
    }

    private static CaseEntity existingCase(Long id, String caseNumber, boolean pendingApproval) {
        CaseEntity c = new CaseEntity();
        c.setId(id);
        c.setCaseNumber(caseNumber);
        c.setCaseTypeId(1L);
        c.setSubject("Existing subject");
        c.setDescription("desc");
        c.setCaseOwner("Ada Lovelace");
        c.setCaseStatus("Requested");
        c.setPriority("Medium");
        c.setClientId("C100");
        c.setRequestedDueDate(LocalDate.of(2026, 8, 15));
        c.setPendingDbmApproval(pendingApproval);
        c.setCreatedAt(Instant.parse("2026-08-01T10:00:00Z"));
        c.setUpdatedAt(Instant.parse("2026-08-01T10:00:00Z"));
        c.setCreatedBy("Ada Lovelace");
        c.setUpdatedBy("Ada Lovelace");
        c.setVersion(1);
        return c;
    }

    private static DbmWorkOrder existingDbm(CaseEntity caseEntity, String transferType) {
        DbmWorkOrder d = new DbmWorkOrder();
        d.setCaseEntity(caseEntity);
        d.setCaseId(caseEntity.getId());
        d.setVendor("Acxiom");
        d.setCoreProcessorConversion(false);
        d.setTransferType(transferType);
        d.setReturnFileExpected("Yes");
        d.setFrequency("Once");
        d.setChangesToMatchbackDb(false);
        d.setCreatedAt(Instant.parse("2026-08-01T10:00:00Z"));
        d.setUpdatedAt(Instant.parse("2026-08-01T10:00:00Z"));
        d.setCreatedBy("Ada Lovelace");
        d.setUpdatedBy("Ada Lovelace");
        return d;
    }

    private static CreateDbmWorkOrderRequest minimalCreateRequest(String transferType) {
        return new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject line",
                "Requested",
                "Description notes",
                false,
                transferType,
                List.of("Complementary"),
                "Yes",
                "No",
                List.of("Share/ESHAR"),
                100,
                "Once",
                null,
                "C100",
                List.of("SK-001"),
                "EVT-1",
                "MEDIA-1",
                "2026-08",
                5,
                false,
                "criteria",
                "matchback",
                "changeTo",
                "WO-IGNORED",
                "notes ignored",
                99);
    }

    private static UpdateDbmWorkOrderRequest updateRequest(
            String transferType,
            String dbmWo,
            String notes,
            Integer totals) {
        return new UpdateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 9, 1),
                "High",
                "Updated subject",
                "In Progress",
                "updated desc",
                true,
                transferType,
                List.of("Complementary"),
                "Yes",
                "Yes",
                List.of("Share/ESHAR"),
                50,
                "Daily",
                null,
                "C100",
                List.of("SK-001"),
                "EVT-2",
                "MEDIA-2",
                "2026-09",
                3,
                true,
                "sel",
                "field",
                "to",
                dbmWo,
                notes,
                totals);
    }
}
