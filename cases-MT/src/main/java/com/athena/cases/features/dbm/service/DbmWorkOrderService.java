package com.athena.cases.features.dbm.service;

import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;

/**
 * Application service for DBM Work Order Request create / update / get.
 */
public interface DbmWorkOrderService {

    /**
     * Creates a DBM Work Order Request case and related detail rows.
     *
     * @param request create payload
     * @return created DBM work order view
     */
    DbmWorkOrderResponse create(CreateDbmWorkOrderRequest request);

    /**
     * Updates an existing DBM Work Order Request.
     *
     * @param caseId  internal {@code cases.id}
     * @param request update payload
     * @return updated DBM work order view
     */
    DbmWorkOrderResponse update(Long caseId, UpdateDbmWorkOrderRequest request);

    /**
     * Loads a DBM Work Order Request by internal case id.
     *
     * @param caseId internal {@code cases.id}
     * @return DBM work order view
     */
    DbmWorkOrderResponse getByCaseId(Long caseId);
}
