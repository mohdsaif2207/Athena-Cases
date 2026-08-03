package com.athena.cases.lookup;

import java.util.List;

/**
 * Port for read-only reference data used by case-type forms.
 * Master data ownership is platform/Lead — features store IDs only.
 */
public interface LookupService {

    List<LookupItem> searchClients(String query);

    List<LookupItem> listActiveCampaigns();

    List<LookupItem> listSegments(String clientId);

    List<LookupItem> listProducts(String query);

    List<LookupItem> findParentCases(String query);

    List<LookupItem> listAuthorizedCaseTypes();
}
