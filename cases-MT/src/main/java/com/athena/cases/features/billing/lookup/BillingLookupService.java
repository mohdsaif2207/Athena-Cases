package com.athena.cases.features.billing.lookup;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.lookup.LookupItem;
import java.util.List;

/**
 * Billing-owned lookup port — campaigns, products, segments, parent cases, hold levels, assignees.
 *
 * <p>Does not replace shared {@code LookupService} (clients, case types, event ids, spoken keys).
 */
public interface BillingLookupService {

    List<LookupItem> listCampaigns();

    List<LookupItem> listProducts(String query);

    List<LookupItem> listSegments(String clientId);

    /** Real {@code cases.id} values the caller may use as parentCaseId. */
    List<LookupItem> findParentCases(String query);

    List<BillingHoldLevelCode> listHoldLevels(BillingHoldType holdType);

    List<LookupItem> listAssignees();
}
