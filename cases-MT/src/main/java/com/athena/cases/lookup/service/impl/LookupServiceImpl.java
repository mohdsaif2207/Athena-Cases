package com.athena.cases.lookup.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.athena.cases.lookup.EventIdLookupItem;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.lookup.LookupMockData;
import com.athena.cases.lookup.LookupService;

/**
 * Stub LookupService backed by in-memory mock data (no DB).
 */
@Service
public class LookupServiceImpl implements LookupService {

    @Override
    public List<LookupItem> searchClients(String query) {
        return filterByQuery(LookupMockData.CLIENTS, query);
    }

    @Override
    public List<LookupItem> listActiveCampaigns() {
        return LookupMockData.CAMPAIGNS;
    }

    @Override
    public List<LookupItem> listSegments(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return List.of();
        }
        return List.of(
                new LookupItem(clientId + "-SEG01", clientId + "-SEG01", "Retail Segment"),
                new LookupItem(clientId + "-SEG02", clientId + "-SEG02", "Commercial Segment"));
    }

    @Override
    public List<LookupItem> listProducts(String query) {
        return filterByQuery(LookupMockData.PRODUCTS, query);
    }

    @Override
    public List<LookupItem> findParentCases(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalized = query.trim().toUpperCase(Locale.ROOT);
        return List.of(new LookupItem(normalized, normalized, "Parent case " + normalized));
    }

    @Override
    public List<LookupItem> listActiveClients() {
        return LookupMockData.CLIENTS;
    }

    @Override
    public List<EventIdLookupItem> listActiveEventIds() {
        return LookupMockData.EVENT_IDS;
    }

    @Override
    public List<LookupItem> listActiveSpokenKeys() {
        return LookupMockData.SPOKEN_KEYS;
    }

    private static List<LookupItem> filterByQuery(List<LookupItem> source, String query) {
        if (query == null || query.isBlank()) {
            return source;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return source.stream()
                .filter(item -> containsIgnoreCase(item.code(), needle)
                        || containsIgnoreCase(item.label(), needle))
                .toList();
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
