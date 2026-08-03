package com.athena.cases.features.exrt.serviceImpl;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.features.exrt.constant.ExrtConstants;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateResponse;
import com.athena.cases.features.exrt.dto.ExrtCaseDetailsResponse;
import com.athena.cases.features.exrt.dto.ExrtLookupItemResponse;
import com.athena.cases.features.exrt.entity.CaseHeaderEntity;
import com.athena.cases.features.exrt.entity.ExrtCaseDetailEntity;
import com.athena.cases.features.exrt.enums.ExrtActionNeeded;
import com.athena.cases.features.exrt.enums.ExrtCaseOrigin;
import com.athena.cases.features.exrt.enums.ExrtDisposition;
import com.athena.cases.features.exrt.enums.ExrtInquirySource;
import com.athena.cases.features.exrt.enums.ExrtInquiryType;
import com.athena.cases.features.exrt.enums.ExrtPriority;
import com.athena.cases.features.exrt.enums.ExrtReasonForEscalation;
import com.athena.cases.features.exrt.enums.ExrtStatus;
import com.athena.cases.features.exrt.exception.ExrtBusinessException;
import com.athena.cases.features.exrt.exception.ExrtResourceNotFoundException;
import com.athena.cases.features.exrt.mapper.ExrtCaseMapper;
import com.athena.cases.features.exrt.repository.CaseHeaderRepository;
import com.athena.cases.features.exrt.repository.ExrtCaseDetailRepository;
import com.athena.cases.features.exrt.service.ExrtCaseService;
import com.athena.cases.features.exrt.util.ExrtCaseNumberGenerator;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupService;
import com.athena.cases.notification.NotificationService;
import com.athena.cases.notification.NotifyTeamCommand;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.workflow.StartWorkflowCommand;
import com.athena.cases.workflow.WorkflowRef;
import com.athena.cases.workflow.WorkflowService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExrtCaseServiceImpl implements ExrtCaseService {

    private static final Logger log = LoggerFactory.getLogger(ExrtCaseServiceImpl.class);

    private final CaseHeaderRepository caseHeaderRepository;
    private final ExrtCaseDetailRepository exrtCaseDetailRepository;
    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final ExrtCaseMapper exrtCaseMapper;
    private final ExrtCaseNumberGenerator exrtCaseNumberGenerator;
    private final CurrentUserService currentUserService;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;
    private final LookupService lookupService;

    public ExrtCaseServiceImpl(
            CaseHeaderRepository caseHeaderRepository,
            ExrtCaseDetailRepository exrtCaseDetailRepository,
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            ExrtCaseMapper exrtCaseMapper,
            ExrtCaseNumberGenerator exrtCaseNumberGenerator,
            CurrentUserService currentUserService,
            WorkflowService workflowService,
            NotificationService notificationService,
            LookupService lookupService) {
        this.caseHeaderRepository = caseHeaderRepository;
        this.exrtCaseDetailRepository = exrtCaseDetailRepository;
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.exrtCaseMapper = exrtCaseMapper;
        this.exrtCaseNumberGenerator = exrtCaseNumberGenerator;
        this.currentUserService = currentUserService;
        this.workflowService = workflowService;
        this.notificationService = notificationService;
        this.lookupService = lookupService;
    }

    @Override
    @Transactional
    public ExrtCaseCreateResponse create(ExrtCaseCreateRequest request) {
        if (!currentUserService.hasPermission(PermissionCodes.CASES_CREATE)) {
            throw new ExrtBusinessException("FORBIDDEN", "User is not permitted to create ExRT cases");
        }

        validateBusinessRules(request);

        String actor = currentUserService.requireUserId();
        String caseOwner = currentUserService.requireDisplayName();
        String caseNumber = exrtCaseNumberGenerator.next();

        CaseHeaderEntity header = exrtCaseMapper.toHeader(request, caseOwner, actor);
        header.setCaseNumber(caseNumber);
        CaseHeaderEntity savedHeader = caseHeaderRepository.save(header);

        ExrtCaseDetailEntity detail = exrtCaseMapper.toDetail(request, savedHeader, actor);
        exrtCaseDetailRepository.save(detail);

        // Platform `cases` row required: workflows/notifications FK to cases(id); also feeds Cases Grid.
        CaseEntity platformCase = syncPlatformCase(request, caseNumber, caseOwner, actor);
        Long platformCaseId = platformCase.getId();

        log.info("exrt case created - exrtCaseId={}, platformCaseId={}, caseNumber={}, owner={}",
                savedHeader.getId(), platformCaseId, caseNumber, caseOwner);

        WorkflowRef workflowRef = workflowService.start(new StartWorkflowCommand(
                platformCaseId,
                ExrtConstants.WORKFLOW_TYPE_EXRT,
                ExrtConstants.RECEIVER_TEAM_DBM,
                ExrtConstants.WORKFLOW_STATUS_PENDING_ASSIGNMENT
        ));
        savedHeader.setWorkflowTriggered(true);
        caseHeaderRepository.save(savedHeader);

        String message = "A new ExRT Request (Case #" + caseNumber + ") has been assigned to your team.";
        notificationService.notifyTeam(new NotifyTeamCommand(
                platformCaseId,
                ExrtConstants.RECEIVER_TEAM_DBM,
                message,
                ExrtConstants.CASE_DETAILS_DEEP_LINK.formatted(platformCaseId)
        ));
        log.info("exrt workflow+notification created - platformCaseId={}, workflowId={}",
                platformCaseId, workflowRef.workflowInstanceId());

        return exrtCaseMapper.toCreateResponse(savedHeader);
    }

    /**
     * Mirrors the ExRT case into the shared {@code cases} table so workflow/notification FKs
     * and the Cases Grid remain consistent — without schema changes.
     */
    private CaseEntity syncPlatformCase(
            ExrtCaseCreateRequest request, String caseNumber, String caseOwner, String actor) {
        CaseTypeEntity caseType = caseTypeRepository.findByCode(CaseTypeCode.EXRT_REQUEST.name())
                .orElseThrow(() -> new ResourceNotFoundException("CaseType", CaseTypeCode.EXRT_REQUEST.name()));

        Instant now = Instant.now();
        CaseEntity entity = new CaseEntity();
        entity.setCaseNumber(caseNumber);
        entity.setCaseTypeId(caseType.getId());
        entity.setClientId(request.clientId());
        entity.setSubject(request.subject());
        entity.setDescription(request.description());
        entity.setCaseOwner(caseOwner);
        entity.setCaseStatus(blankToDefault(request.status(), ExrtConstants.DEFAULT_STATUS));
        entity.setPriority(toPlatformPriority(request.priority()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        entity.setVersion(1);
        return caseRepository.save(entity);
    }

    private static String toPlatformPriority(String priority) {
        String raw = priority == null || priority.isBlank() ? ExrtConstants.DEFAULT_PRIORITY : priority.trim();
        return switch (raw.toUpperCase(Locale.ROOT)) {
            case "HIGH" -> "High";
            case "LOW" -> "Low";
            default -> "Medium";
        };
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public ExrtCaseDetailsResponse getById(Long caseId) {
        CaseHeaderEntity header = caseHeaderRepository.findById(caseId)
                .orElseThrow(() -> new ExrtResourceNotFoundException("ExRT case not found: " + caseId));
        ExrtCaseDetailEntity detail = exrtCaseDetailRepository.findByCaseHeaderId(caseId)
                .orElseThrow(() -> new ExrtResourceNotFoundException("ExRT case detail not found: " + caseId));
        return exrtCaseMapper.toDetailsResponse(header, detail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listStaticLookups(String lookupType) {
        return switch (lookupType.toLowerCase(Locale.ROOT)) {
            case "statuses" -> Arrays.stream(ExrtStatus.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), v.getLabel()))
                    .toList();
            case "priorities" -> Arrays.stream(ExrtPriority.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "types" -> Arrays.stream(ExrtInquiryType.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "dispositions" -> Arrays.stream(ExrtDisposition.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "inquiry-sources" -> Arrays.stream(ExrtInquirySource.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "action-needed" -> Arrays.stream(ExrtActionNeeded.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "escalation-reasons" -> Arrays.stream(ExrtReasonForEscalation.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "case-origins" -> Arrays.stream(ExrtCaseOrigin.values())
                    .map(v -> new ExrtLookupItemResponse(v.name(), v.name(), title(v.name())))
                    .toList();
            case "clients" -> lookupService.searchClients("").stream().map(this::toLookup).toList();
            case "products" -> lookupService.listProducts("").stream().map(this::toLookup).toList();
            default -> throw new ExrtBusinessException("VALIDATION_ERROR", "Unknown lookup type: " + lookupType);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listCarriers() {
        return List.of(
                new ExrtLookupItemResponse("CR01", "CR01", "Acme Carrier"),
                new ExrtLookupItemResponse("CR02", "CR02", "Northstar Insurance"),
                new ExrtLookupItemResponse("CR03", "CR03", "Premier Life")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listTierIiAgents() {
        return List.of(
                new ExrtLookupItemResponse("A100", "A100", "Alex Rivera"),
                new ExrtLookupItemResponse("A101", "A101", "Jordan Lee"),
                new ExrtLookupItemResponse("A102", "A102", "Sam Patel")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listAssignees() {
        return List.of(
                new ExrtLookupItemResponse("user.jdoe", "user.jdoe", "Jane Doe"),
                new ExrtLookupItemResponse("user.asmith", "user.asmith", "Alex Smith"),
                new ExrtLookupItemResponse("user.mchen", "user.mchen", "Morgan Chen")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listContacts(String clientId) {
        return List.of(
                new ExrtLookupItemResponse("CT01", "CT01", "Primary Contact"),
                new ExrtLookupItemResponse("CT02", "CT02", "Billing Contact")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExrtLookupItemResponse> listReasonCodes() {
        return List.of(
                new ExrtLookupItemResponse("RC_BILLING", "RC_BILLING", "Billing discrepancy"),
                new ExrtLookupItemResponse("RC_CANCEL", "RC_CANCEL", "Cancellation request"),
                new ExrtLookupItemResponse("RC_CLAIMS", "RC_CLAIMS", "Claims inquiry"),
                new ExrtLookupItemResponse("RC_REFUND", "RC_REFUND", "Refund request"),
                new ExrtLookupItemResponse("RC_COVERAGE", "RC_COVERAGE", "Coverage update"),
                new ExrtLookupItemResponse("RC_OTHER", "RC_OTHER", "Other")
        );
    }

    private void validateBusinessRules(ExrtCaseCreateRequest request) {
        requireEnumName(ExrtInquiryType.class, request.type(), "type");
        requireEnumName(ExrtDisposition.class, request.disposition(), "disposition");
        requireEnumName(ExrtInquirySource.class, request.inquirySource(), "inquirySource");
        requireEnumName(ExrtActionNeeded.class, request.actionNeeded(), "actionNeeded");
        requireEnumName(ExrtReasonForEscalation.class, request.reasonForEscalation(), "reasonForEscalation");
        requireEnumName(ExrtCaseOrigin.class, request.caseOrigin(), "caseOrigin");
        requireEnumName(ExrtPriority.class, request.priority().toUpperCase(Locale.ROOT), "priority");
        requireEmailIfPresent(request.customerContactEmail(), "customerContactEmail");
        requireEmailIfPresent(request.webMail(), "webMail");
    }

    private static void requireEmailIfPresent(String value, String field) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ExrtBusinessException("VALIDATION_ERROR", "Please enter a valid email address.", field);
        }
    }

    private static <E extends Enum<E>> void requireEnumName(Class<E> type, String value, String field) {
        try {
            Enum.valueOf(type, value.trim());
        } catch (Exception ex) {
            throw new ExrtBusinessException("VALIDATION_ERROR", "Invalid value for " + field, field);
        }
    }

    private ExrtLookupItemResponse toLookup(LookupItem item) {
        return new ExrtLookupItemResponse(item.id(), item.code(), item.label());
    }

    private static String title(String enumName) {
        String lower = enumName.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
