package com.athena.cases.idallocation;

/**
 * Shared port for feature display identifiers (e.g. {@code BIL######}).
 *
 * <p>Case Management owns {@code cases.case_number} ({@code CASE-xxxx}).
 * This port owns per–case-type business display IDs without coupling CM to feature columns.
 */
public interface BusinessCaseIdService {

    /**
     * Allocates the next display id for the given case-type code.
     *
     * @param caseTypeCode canonical code (e.g. {@code BILLING_DEPARTMENT_REQUEST})
     * @return formatted id (e.g. {@code BIL000001})
     */
    String allocate(String caseTypeCode);
}
