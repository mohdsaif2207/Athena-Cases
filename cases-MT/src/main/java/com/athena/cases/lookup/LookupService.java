package com.athena.cases.lookup;

import java.util.List;

/**
 * Port for read-only reference data used by case-type forms and shared lookups.
 */
public interface LookupService {

    List<LookupItem> searchClients(String query);

    List<LookupItem> listActiveCampaigns();

    List<LookupItem> listSegments(String clientId);

    List<LookupItem> listProducts(String query);

    List<LookupItem> findParentCases(String query);

    /** Case types the current user may create (RBAC-scoped). */
    List<LookupItem> listAuthorizedCaseTypes();

    /** Active clients for DBM / shared dropdowns. */
    List<LookupItem> listActiveClients();

    /** Active Event IDs including Mail Month (DBM Section 3 auto-populate). */
    List<EventIdLookupItem> listActiveEventIds();

    /** Active spoken keys for DBM multi-select. */
    List<LookupItem> listActiveSpokenKeys();
}
