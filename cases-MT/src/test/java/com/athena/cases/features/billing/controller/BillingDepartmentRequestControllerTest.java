package com.athena.cases.features.billing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.athena.cases.common.constants.HttpHeaderNames;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestCreateRequest;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestResponse;
import com.athena.cases.features.billing.dto.BillingDepartmentRequestUpdateRequest;
import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.features.billing.enums.BillingHoldType;
import com.athena.cases.features.billing.enums.BillingRequestType;
import com.athena.cases.features.billing.exception.BillingExceptionHandler;
import com.athena.cases.features.billing.exception.BillingResourceNotFoundException;
import com.athena.cases.features.billing.exception.BillingValidationException;
import com.athena.cases.features.billing.service.BillingDepartmentRequestService;
import com.athena.cases.lookup.LookupItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Billing controller HTTP tests.
 *
 * <p>Uses standalone {@link MockMvc} (same stack as {@code spring-boot-starter-webmvc-test})
 * rather than {@code @WebMvcTest}: current pom has no Spring Security starter, so
 * {@code @PreAuthorize} / {@code @WithMockUser} slice tests cannot run until Lead adds
 * security test dependencies. Method-security behaviour is therefore out of scope here.
 */
@ExtendWith(MockitoExtension.class)
class BillingDepartmentRequestControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Mock
    private BillingDepartmentRequestService billingDepartmentRequestService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BillingDepartmentRequestController controller =
                new BillingDepartmentRequestController(billingDepartmentRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BillingExceptionHandler())
                .build();
    }

    @Test
    void should_return201_when_createSucceeds() throws Exception {
        when(billingDepartmentRequestService.create(any(BillingDepartmentRequestCreateRequest.class)))
                .thenReturn(sampleResponse(10L));

        mockMvc.perform(post("/api/v1/billing-department-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaderNames.REQUEST_ID, "req-create-1")
                        .content(OBJECT_MAPPER.writeValueAsString(minimalCreateJson())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/billing-department-requests/10"))
                .andExpect(jsonPath("$.requestId").value("req-create-1"))
                .andExpect(jsonPath("$.data.caseId").value(10))
                .andExpect(jsonPath("$.data.caseNumber").value("CASE-10"))
                .andExpect(jsonPath("$.data.businessCaseId").value("BIL000010"));

        verify(billingDepartmentRequestService).create(any(BillingDepartmentRequestCreateRequest.class));
    }

    @Test
    void should_return400_when_serviceThrowsBillingValidation() throws Exception {
        when(billingDepartmentRequestService.create(any(BillingDepartmentRequestCreateRequest.class)))
                .thenThrow(new BillingValidationException(
                        "businessCaseId",
                        "businessCaseId must be provided by shared Case Management"));

        mockMvc.perform(post("/api/v1/billing-department-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaderNames.REQUEST_ID, "req-create-2")
                        .content(OBJECT_MAPPER.writeValueAsString(minimalCreateJson())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.field").value("businessCaseId"))
                .andExpect(jsonPath("$.error.requestId").value("req-create-2"));
    }

    @Test
    void should_return200_when_getByCaseId() throws Exception {
        when(billingDepartmentRequestService.getByCaseId(10L)).thenReturn(sampleResponse(10L));

        mockMvc.perform(get("/api/v1/billing-department-requests/10")
                        .header(HttpHeaderNames.REQUEST_ID, "req-get-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caseId").value(10))
                .andExpect(jsonPath("$.data.requestType").value("Research"));
    }

    @Test
    void should_return404_when_billingNotFound() throws Exception {
        when(billingDepartmentRequestService.getByCaseId(404L))
                .thenThrow(new BillingResourceNotFoundException(
                        "Billing Department Request not found for caseId=404"));

        mockMvc.perform(get("/api/v1/billing-department-requests/404")
                        .header(HttpHeaderNames.REQUEST_ID, "req-get-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.requestId").value("req-get-404"));
    }

    @Test
    void should_return200_when_updateSucceeds() throws Exception {
        when(billingDepartmentRequestService.update(eq(10L), any(BillingDepartmentRequestUpdateRequest.class)))
                .thenReturn(sampleResponse(10L));

        mockMvc.perform(put("/api/v1/billing-department-requests/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaderNames.REQUEST_ID, "req-put-1")
                        .content(OBJECT_MAPPER.writeValueAsString(minimalUpdateJson())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caseId").value(10));

        verify(billingDepartmentRequestService).update(eq(10L), any(BillingDepartmentRequestUpdateRequest.class));
    }

    @Test
    void should_returnHoldLevels_when_lookupRequested() throws Exception {
        when(billingDepartmentRequestService.listHoldLevels(isNull()))
                .thenReturn(List.of(BillingHoldLevelCode.BILLING, BillingHoldLevelCode.REBILL));

        mockMvc.perform(get("/api/v1/billing-department-requests/lookups/hold-levels")
                        .header(HttpHeaderNames.REQUEST_ID, "req-hl-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available[0]").value("Billing"))
                .andExpect(jsonPath("$.data.available[1]").value("Rebill"));
    }

    @Test
    void should_returnHoldLevelsFilteredParam_when_holdTypeProvided() throws Exception {
        when(billingDepartmentRequestService.listHoldLevels(BillingHoldType.CLIENT_LEVEL))
                .thenReturn(List.of(BillingHoldLevelCode.PRE_NOTE));

        mockMvc.perform(get("/api/v1/billing-department-requests/lookups/hold-levels")
                        .param("holdType", "CLIENT_LEVEL")
                        .header(HttpHeaderNames.REQUEST_ID, "req-hl-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available[0]").value("Pre Note"));

        verify(billingDepartmentRequestService).listHoldLevels(BillingHoldType.CLIENT_LEVEL);
    }

    @Test
    void should_returnEmptyAssignees_when_lookupApiMissing() throws Exception {
        when(billingDepartmentRequestService.listAssignees()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/billing-department-requests/lookups/assignees")
                        .header(HttpHeaderNames.REQUEST_ID, "req-as-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void should_mapAssigneeLookup_when_serviceReturnsItems() throws Exception {
        when(billingDepartmentRequestService.listAssignees())
                .thenReturn(List.of(new LookupItem("u1", "jdoe", "Jane Doe")));

        mockMvc.perform(get("/api/v1/billing-department-requests/lookups/assignees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("jdoe"))
                .andExpect(jsonPath("$.data[0].displayName").value("Jane Doe"));
    }

    private static BillingDepartmentRequestResponse sampleResponse(Long caseId) {
        return new BillingDepartmentRequestResponse(
                caseId,
                "CASE-" + caseId,
                "BIL000010",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                BillingRequestType.RESEARCH,
                null,
                null,
                null,
                null,
                false,
                null,
                (BigDecimal) null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Need research on billing reject",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                Instant.parse("2026-07-31T10:00:00Z"),
                Instant.parse("2026-07-31T10:00:00Z"),
                9L,
                "PENDING_ASSIGNMENT"
        );
    }

    private static BillingDepartmentRequestCreateRequest minimalCreateJson() {
        return new BillingDepartmentRequestCreateRequest(
                BillingRequestType.RESEARCH,
                null,
                null,
                null,
                "Medium",
                "Requested",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Need research on billing reject",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static BillingDepartmentRequestUpdateRequest minimalUpdateJson() {
        return new BillingDepartmentRequestUpdateRequest(
                BillingRequestType.RESEARCH,
                null,
                null,
                null,
                "Medium",
                "Requested",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Need research on billing reject",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1
        );
    }
}
