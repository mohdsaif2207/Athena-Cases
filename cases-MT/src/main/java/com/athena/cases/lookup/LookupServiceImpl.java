package com.athena.cases.lookup;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Combined lookup service: RBAC case-types + Billing parent-case search + ExRT/DBM mock masters.
 */
@Service
public class LookupServiceImpl implements LookupService {

    private static final int PARENT_CASE_LOOKUP_LIMIT = 50;

    private static final Map<String, Integer> CASE_TYPE_DISPLAY_ORDER = Map.of(
            "DBM_WORK_ORDER_REQUEST", 1,
            "EXRT_REQUEST", 2,
            "BILLING_DEPARTMENT_REQUEST", 3);

    /**
     * ExRT client search corpus (temporary until platform client master exists).
     */
    private static final List<LookupItem> DEV_CLIENTS = List.of(
            new LookupItem("C100", "C100", "Acme Credit Union"),
            new LookupItem("C200", "C200", "Summit Bank"),
            new LookupItem("C300", "C300", "Harbor Financial"));

    /**
     * ExRT product typeahead corpus (temporary until platform product master exists).
     */
    private static final List<LookupItem> DEV_PRODUCTS = List.of(
            new LookupItem("P10", "P10", "Term Life"),
            new LookupItem("P20", "P20", "Disability"),
            new LookupItem("P30", "P30", "Accident"));

    private final CaseTypeRepository caseTypeRepository;
    private final CaseRepository caseRepository;
    private final CurrentUserService currentUserService;

    public LookupServiceImpl(
            CaseTypeRepository caseTypeRepository,
            CaseRepository caseRepository,
            CurrentUserService currentUserService) {
        this.caseTypeRepository = caseTypeRepository;
        this.caseRepository = caseRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<LookupItem> searchClients(String query) {
        return filterByQuery(DEV_CLIENTS, query);
    }

    @Override
    public List<LookupItem> listActiveCampaigns() {
        return LookupMockData.CAMPAIGNS;
    }

    @Override
    public List<LookupItem> listSegments(String clientId) {
        return LookupMockData.segmentsForClient(clientId);
    }

    @Override
    public List<LookupItem> listProducts(String query) {
        return filterByQuery(DEV_PRODUCTS, query);
    }

    /**
     * Real cases the caller may access — {@code id} is {@code cases.id} for parentCaseId FK validation.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LookupItem> findParentCases(String query) {
        requireCasesRead();
        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> caseTypeCodes = principal.getCaseTypeCodes();
        if (caseTypeCodes.isEmpty()) {
            return List.of();
        }

        List<CaseTypeEntity> types = caseTypeRepository.findByCodeIn(caseTypeCodes);
        if (types.isEmpty()) {
            return List.of();
        }
        List<Long> typeIds = types.stream().map(CaseTypeEntity::getId).toList();

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return caseRepository.findAuthorized(typeIds, false).stream()
                .filter(c -> matchesParentQuery(c, q))
                .limit(PARENT_CASE_LOOKUP_LIMIT)
                .map(c -> new LookupItem(
                        String.valueOf(c.getId()),
                        c.getCaseNumber(),
                        c.getCaseNumber() + " — " + nullToEmpty(c.getSubject())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupItem> listAuthorizedCaseTypes() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_CREATE)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_CREATE required");
        }

        UserPrincipal principal = currentUserService.requirePrincipal();
        List<String> authorizedCodes = principal.getCaseTypeCodes();
        if (authorizedCodes.isEmpty()) {
            return List.of();
        }

        return caseTypeRepository.findByCodeInAndActiveTrue(authorizedCodes).stream()
                .sorted(Comparator
                        .comparingInt((CaseTypeEntity ct) ->
                                CASE_TYPE_DISPLAY_ORDER.getOrDefault(ct.getCode(), 100))
                        .thenComparing(CaseTypeEntity::getName))
                .map(ct -> new LookupItem(String.valueOf(ct.getId()), ct.getCode(), ct.getName()))
                .toList();
    }

    @Override
    public List<LookupItem> listActiveClients() {
        return LookupMockData.CLIENTS;
    }

    @Override
    public List<EventIdLookupItem> listActiveEventIds() {
        return LookupMockData.EVENT_IDS;
    }

    @Override
    public List<LookupItem> listActiveSpokenKeys() {
        return LookupMockData.SPOKEN_KEYS;
    }

    private void requireCasesRead() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)
                || currentUserService.hasPermission(PermissionCodes.CASES_CREATE)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_VIEW required");
        }
    }

    private static List<LookupItem> filterByQuery(List<LookupItem> source, String query) {
        if (query == null || query.isBlank()) {
            return source;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(item -> containsIgnoreCase(item.code(), needle)
                        || containsIgnoreCase(item.label(), needle)
                        || containsIgnoreCase(item.id(), needle))
                .toList();
    }

    private static boolean matchesParentQuery(CaseEntity c, String queryLower) {
        if (queryLower.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(c.getCaseNumber(), queryLower)
                || containsIgnoreCase(c.getSubject(), queryLower);
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(queryLower);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
