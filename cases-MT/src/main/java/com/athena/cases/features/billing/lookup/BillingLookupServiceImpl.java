package com.athena.cases.features.billing.lookup;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingLookupServiceImpl implements BillingLookupService {

    private static final int PARENT_CASE_LOOKUP_LIMIT = 200;

    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final CurrentUserService currentUserService;

    public BillingLookupServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            CurrentUserService currentUserService
    ) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<LookupItem> listCampaigns() {
        // TEMP: Replace with shared lookup API when available.
        return BillingLookupMockData.CAMPAIGNS;
    }

    @Override
    public List<LookupItem> listProducts(String query) {
        // TEMP: Replace with shared lookup API when available.
        return BillingLookupMockData.filterByQuery(BillingLookupMockData.PRODUCTS, query);
    }

    @Override
    public List<LookupItem> listSegments(String clientId) {
        // TEMP: Replace with shared lookup API when available.
        return BillingLookupMockData.segmentsForClient(clientId);
    }

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
    public List<BillingHoldLevelCode> listHoldLevels(BillingHoldType holdType) {
        // TODO Replace after BA finalizes Hold Type → Level matrix — interim flat set
        return Arrays.asList(BillingHoldLevelCode.values());
    }

    @Override
    public List<LookupItem> listAssignees() {
        // TEMP: Replace with shared lookup API when available —
        // billing-access users are not exposed on LookupService yet.
        return List.of();
    }

    private void requireCasesRead() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)
                || currentUserService.hasPermission(PermissionCodes.CASES_CREATE)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_VIEW required");
        }
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
