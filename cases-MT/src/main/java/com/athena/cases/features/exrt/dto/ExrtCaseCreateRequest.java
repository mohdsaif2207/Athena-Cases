package com.athena.cases.features.exrt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create payload for ExRT Request. Case owner is forced from CurrentUserService.
 */
public record ExrtCaseCreateRequest(
        @NotBlank @Size(max = 64) String clientId,
        @NotBlank @Size(max = 64) String status,
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[A-Za-z][A-Za-z '\\-]*$", message = "Last name must be text only")
        String lastName,
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[A-Za-z][A-Za-z '\\-]*$", message = "First name must be text only")
        String firstName,
        @Size(max = 64) String tierIiAgentId,
        @Size(max = 1) String mi,
        @Size(max = 64) String state,
        @Size(max = 10) @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be exactly 10 digits")
        String phoneNumber,
        @NotBlank @Size(max = 64) String productId,
        @Size(max = 64) String coverageId,
        @NotBlank @Size(max = 64) String carrierId,
        @Size(max = 255) String customerContactEmail,
        @NotBlank @Size(max = 32) String type,
        @Size(max = 25) String policyNumber,
        @NotBlank @Size(max = 128) String disposition,
        @NotBlank @Size(max = 64) String inquirySource,
        @NotBlank @Size(max = 64) String actionNeeded,
        @Size(max = 128) String requestAssignedTo,
        @NotBlank @Size(max = 128) String reasonForEscalation,
        @NotBlank @Size(max = 128) String reasonCode1,
        @Size(max = 1000) String requestorNotes,
        @Size(max = 1000) String notesIssues,
        @Size(max = 1000) String coachingFeedback,
        Boolean callCenterEducation,
        @NotBlank @Size(max = 64) String caseOrigin,
        @Size(max = 255) String webMail,
        @NotBlank @Size(max = 200) String subject,
        @Size(max = 200) String contactName,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 32) String priority
) {
}
