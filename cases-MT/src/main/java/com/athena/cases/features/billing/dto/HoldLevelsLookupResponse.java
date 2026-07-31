package com.athena.cases.features.billing.dto;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import java.util.List;

/**
 * Hold-level dual-listbox Available options — LLD §16.5.
 */
public record HoldLevelsLookupResponse(List<BillingHoldLevelCode> available) {
}
