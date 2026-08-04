package com.athena.cases.features.dbm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.common.exception.GlobalExceptionHandler;
import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.features.dbm.DbmConstants;
import com.athena.cases.features.dbm.dto.CreateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.dto.DbmWorkOrderResponse;
import com.athena.cases.features.dbm.dto.UpdateDbmWorkOrderRequest;
import com.athena.cases.features.dbm.service.DbmWorkOrderService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * DBM Work Order Request controller HTTP tests (US Scenarios 3, 4, 6, exception paths).
 *
 * <p>Standalone MockMvc — same pattern as Billing. Method-security (@PreAuthorize) is
 * exercised in service-layer permission tests; here we verify HTTP mapping + Bean Validation
 * + GlobalExceptionHandler mapping.
 */
@ExtendWith(MockitoExtension.class)
class DbmWorkOrderControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    @Mock
    private DbmWorkOrderService dbmWorkOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new DbmWorkOrderController(dbmWorkOrderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void should_return201_when_createSucceeds() throws Exception {
        when(dbmWorkOrderService.create(any(CreateDbmWorkOrderRequest.class)))
                .thenReturn(sampleResponse(42L, "DBM000042", false));

        mockMvc.perform(post("/api/dbm/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(validCreatePayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caseId").value(42))
                .andExpect(jsonPath("$.caseNumber").value("DBM000042"))
                .andExpect(jsonPath("$.vendor").value("Acxiom"))
                .andExpect(jsonPath("$.status").value("Requested"));

        verify(dbmWorkOrderService).create(any(CreateDbmWorkOrderRequest.class));
    }

    @Test
    void should_return400_when_mandatoryVendorMissing() throws Exception {
        CreateDbmWorkOrderRequest invalid = new CreateDbmWorkOrderRequest(
                "  ",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/dbm/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return400_when_transferTypeOtherWithoutSpecialInstructions() throws Exception {
        CreateDbmWorkOrderRequest invalid = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                DbmConstants.TRANSFER_TYPE_OTHER,
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                "   ",
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/dbm/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return400_when_expectedQuantityNegative() throws Exception {
        CreateDbmWorkOrderRequest invalid = new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                -1,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/dbm/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return403_when_serviceDeniesCreate() throws Exception {
        when(dbmWorkOrderService.create(any(CreateDbmWorkOrderRequest.class)))
                .thenThrow(new ForbiddenException("CASES_CREATE required"));

        mockMvc.perform(post("/api/dbm/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(validCreatePayload())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void should_return200_when_getByCaseId() throws Exception {
        when(dbmWorkOrderService.getByCaseId(10L))
                .thenReturn(sampleResponse(10L, "DBM000010", false));

        mockMvc.perform(get("/api/dbm/work-orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value(10))
                .andExpect(jsonPath("$.caseNumber").value("DBM000010"));
    }

    @Test
    void should_return404_when_caseNotFound() throws Exception {
        when(dbmWorkOrderService.getByCaseId(404L))
                .thenThrow(new ResourceNotFoundException("Case", "404"));

        mockMvc.perform(get("/api/dbm/work-orders/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void should_return200_when_updateSucceeds() throws Exception {
        when(dbmWorkOrderService.update(eq(10L), any(UpdateDbmWorkOrderRequest.class)))
                .thenReturn(sampleResponse(10L, "DBM000010", true));

        mockMvc.perform(put("/api/dbm/work-orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(validUpdatePayload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value(10))
                .andExpect(jsonPath("$.pendingDbmApproval").value(true));

        verify(dbmWorkOrderService).update(eq(10L), any(UpdateDbmWorkOrderRequest.class));
    }

    @Test
    void should_return400_when_updateMissingSubject() throws Exception {
        UpdateDbmWorkOrderRequest invalid = new UpdateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "",
                "Requested",
                null,
                false,
                "Account Update File",
                List.of(),
                "Yes",
                null,
                List.of(),
                null,
                "Once",
                null,
                "C100",
                List.of(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);

        mockMvc.perform(put("/api/dbm/work-orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void should_return403_when_serviceDeniesUpdate() throws Exception {
        when(dbmWorkOrderService.update(eq(10L), any(UpdateDbmWorkOrderRequest.class)))
                .thenThrow(new ForbiddenException("CASES_EDIT required"));

        mockMvc.perform(put("/api/dbm/work-orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(validUpdatePayload())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private static CreateDbmWorkOrderRequest validCreatePayload() {
        return new CreateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject line",
                "Requested",
                "Notes",
                false,
                "Account Update File",
                List.of("Complementary"),
                "Yes",
                "No",
                List.of("Share/ESHAR"),
                100,
                "Once",
                null,
                "C100",
                List.of("SK-001"),
                "EVT-1",
                null,
                "2026-08",
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static UpdateDbmWorkOrderRequest validUpdatePayload() {
        return new UpdateDbmWorkOrderRequest(
                "Acxiom",
                LocalDate.of(2026, 9, 1),
                "High",
                "Updated subject",
                "In Progress",
                "Notes",
                true,
                DbmConstants.TRANSFER_TYPE_CUSTOM,
                List.of("Voluntary"),
                "No",
                "Yes",
                List.of("Checking/ECHK"),
                10,
                "Weekly",
                null,
                "C100",
                List.of("SK-002"),
                "EVT-2",
                null,
                "2026-09",
                1,
                true,
                "sel",
                "field",
                "to",
                null,
                null,
                null);
    }

    private static DbmWorkOrderResponse sampleResponse(Long caseId, String caseNumber, boolean pending) {
        return new DbmWorkOrderResponse(
                caseId,
                caseNumber,
                "DBM_WORK_ORDER_REQUEST",
                "Ada Lovelace",
                LocalDate.of(2026, 8, 15),
                "Medium",
                "Subject line",
                "Requested",
                "Notes",
                "C100",
                pending,
                "Acxiom",
                false,
                "Account Update File",
                List.of("Complementary"),
                "Yes",
                "No",
                List.of("Share/ESHAR"),
                100,
                "Once",
                null,
                List.of("SK-001"),
                "EVT-1",
                null,
                "2026-08",
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-08-01T10:00:00Z"),
                Instant.parse("2026-08-01T10:00:00Z"));
    }
}
