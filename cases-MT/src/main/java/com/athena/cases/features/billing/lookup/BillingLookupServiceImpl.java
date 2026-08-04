package com.athena.cases.features.billing.lookup;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.common.constants.PermissionCodes;
import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.features.billing.BillingConstants;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.security.CurrentUserService;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingLookupServiceImpl implements BillingLookupService {

    private static final int PARENT_CASE_LOOKUP_LIMIT = 200;

    private final CaseRepository caseRepository;
    private final CaseTypeRepository caseTypeRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public BillingLookupServiceImpl(
            CaseRepository caseRepository,
            CaseTypeRepository caseTypeRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.caseRepository = caseRepository;
        this.caseTypeRepository = caseTypeRepository;
        this.userRepository = userRepository;
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

    /**
     * Parent-case candidates are restricted to Billing Department Request cases only.
     */
    @Override
    @Transactional(readOnly = true)
    public List<LookupItem> findParentCases(String query) {
        requireCasesRead();

        Optional<CaseTypeEntity> billingType = caseTypeRepository.findByCode(BillingConstants.CASE_TYPE_CODE);
        if (billingType.isEmpty()) {
            return List.of();
        }

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return caseRepository.findAuthorized(List.of(billingType.get().getId()), false).stream()
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
    public boolean isBillingCase(Long caseId) {
        if (caseId == null) {
            return false;
        }
        Optional<CaseTypeEntity> billingType = caseTypeRepository.findByCode(BillingConstants.CASE_TYPE_CODE);
        if (billingType.isEmpty()) {
            return false;
        }
        return caseRepository.findById(caseId)
                .map(c -> billingType.get().getId().equals(c.getCaseTypeId()))
                .orElse(false);
    }

    @Override
    public List<BillingHoldLevelCode> listHoldLevels(BillingHoldType holdType) {
        // TODO Replace after BA finalizes Hold Type → Level matrix — interim flat set
        return Arrays.asList(BillingHoldLevelCode.values());
    }

    /**
     * Assignees are IAM users on the Billing receiving team only ({@code BILLING_OPS_TEAM}).
     */
    @Override
    @Transactional(readOnly = true)
    public List<LookupItem> listAssignees() {
        requireCasesRead();
        return userRepository.findActiveByTeamCode(BillingConstants.RECEIVER_TEAM_BILLING_OPS).stream()
                .map(BillingLookupServiceImpl::toAssigneeItem)
                .toList();
    }

    private void requireCasesRead() {
        if (!(currentUserService.hasPermission(PermissionCodes.CASES_VIEW)
                || currentUserService.hasPermission(PermissionCodes.CASES_CREATE)
                || currentUserService.hasPermission(PermissionCodes.CASES_ACCESS))) {
            throw new ForbiddenException("CASES_VIEW required");
        }
    }

    private static LookupItem toAssigneeItem(UserEntity user) {
        String username = user.getUsername();
        String label = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? username
                : user.getDisplayName();
        return new LookupItem(username, username, label);
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
