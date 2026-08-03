package com.athena.cases.lookup.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.athena.cases.lookup.EventIdLookupItem;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupService;

/**
 * Shared lookup REST API for form dropdowns (mock data until platform masters exist).
 */
@RestController("dbmFormLookupController")
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<LookupItem>> listClients() {
        return ResponseEntity.ok(lookupService.listActiveClients());
    }

    @GetMapping("/event-ids")
    public ResponseEntity<List<EventIdLookupItem>> listEventIds() {
        return ResponseEntity.ok(lookupService.listActiveEventIds());
    }

    @GetMapping("/spoken-keys")
    public ResponseEntity<List<LookupItem>> listSpokenKeys() {
        return ResponseEntity.ok(lookupService.listActiveSpokenKeys());
    }
}
