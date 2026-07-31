package com.athena.cases.features.dbm.service;

/**
 * Application service for DBM Work Order Request create / update / get.
 * Request and response types are placeholders until DTOs are introduced.
 */
public interface DbmWorkOrderService {

    /**
     * Creates a DBM Work Order Request case and related detail rows.
     *
     * @param request placeholder create payload (DTO TBD)
     * @return placeholder create result (DTO TBD)
     */
    Object create(Object request);

    /**
     * Updates an existing DBM Work Order Request.
     *
     * @param caseId  internal {@code cases.id}
     * @param request placeholder update payload (DTO TBD)
     * @return placeholder update result (DTO TBD)
     */
    Object update(Long caseId, Object request);

    /**
     * Loads a DBM Work Order Request by internal case id.
     *
     * @param caseId internal {@code cases.id}
     * @return placeholder response (DTO TBD)
     */
    Object getByCaseId(Long caseId);
}
