package com.athena.cases.lookup;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;

/**
 * Combined lookup service: RBAC case-types + in-memory mock masters for DBM dropdowns.
 */
@Service
public class LookupServiceImpl implements LookupService {

    private static final Map<String, Integer> CASE_TYPE_DISPLAY_ORDER = Map.of(
            "DBM_WORK_ORDER_REQUEST", 1,
            "EXRT_REQUEST", 2,
            "BILLING_DEPARTMENT_REQUEST", 3);

    private final CaseTypeRepository caseTypeRepository;
    private final CurrentUserService currentUserService;

    public LookupServiceImpl(
            CaseTypeRepository caseTypeRepository,
            CurrentUserService currentUserService) {
        this.caseTypeRepository = caseTypeRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<LookupItem> searchClients(String query) {
        return filterByQuery(LookupMockData.CLIENTS, query);
    }

    @Override
    public List<LookupItem> listActiveCampaigns() {
        return LookupMockData.CAMPAIGNS;
    }

    @Override
    public List<LookupItem> listSegments(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }
        return List.of(
                new LookupItem(clientId + "-SEG01", clientId + "-SEG01", "Retail Segment"),
                new LookupItem(clientId + "-SEG02", clientId + "-SEG02", "Commercial Segment"));
    }

    @Override
    public List<LookupItem> listProducts(String query) {
        return filterByQuery(LookupMockData.PRODUCTS, query);
    }

    @Override
    public List<LookupItem> findParentCases(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalized = query.trim().toUpperCase(Locale.ROOT);
        return List.of(new LookupItem(normalized, normalized, "Parent case " + normalized));
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

    private static List<LookupItem> filterByQuery(List<LookupItem> source, String query) {
        if (query == null || query.isBlank()) {
            return source;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(item -> containsIgnoreCase(item.code(), needle)
                        || containsIgnoreCase(item.label(), needle))
                .toList();
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
