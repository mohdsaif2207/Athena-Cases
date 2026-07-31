package com.athena.cases.features.billing.service;

import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.lookup.LookupItem;
import java.util.List;

/**
 * Billing Department Request application service — LLD §17.
 */
public interface BillingDepartmentRequestService {

    BillingDepartmentRequestResponse create(BillingDepartmentRequestCreateRequest request);

    BillingDepartmentRequestResponse getByCaseId(Long caseId);

    BillingDepartmentRequestResponse update(Long caseId, BillingDepartmentRequestUpdateRequest request);

    /**
     * Available Hold Level codes for the dual listbox.
     *
     * <p>TODO Replace after BA finalizes Hold Type mapping — interim flat set.
     */
    List<BillingHoldLevelCode> listHoldLevels(BillingHoldType holdType);

    List<LookupItem> listAssignees();
}
