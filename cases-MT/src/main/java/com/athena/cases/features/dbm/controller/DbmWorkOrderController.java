package com.athena.cases.features.dbm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.service.DbmWorkOrderService;

import jakarta.validation.Valid;

/**
 * REST API for DBM Work Order Request create / get / update.
 */
@RestController
@RequestMapping("/api/dbm/work-orders")
public class DbmWorkOrderController {

    private final DbmWorkOrderService dbmWorkOrderService;

    public DbmWorkOrderController(DbmWorkOrderService dbmWorkOrderService) {
        this.dbmWorkOrderService = dbmWorkOrderService;
    }

    @PostMapping
    public ResponseEntity<DbmWorkOrderResponse> create(
            @Valid @RequestBody CreateDbmWorkOrderRequest request) {
        DbmWorkOrderResponse created = dbmWorkOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<DbmWorkOrderResponse> getByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(dbmWorkOrderService.getByCaseId(caseId));
    }

    @PutMapping("/{caseId}")
    public ResponseEntity<DbmWorkOrderResponse> update(
            @PathVariable Long caseId,
            @Valid @RequestBody UpdateDbmWorkOrderRequest request) {
        return ResponseEntity.ok(dbmWorkOrderService.update(caseId, request));
    }
}
