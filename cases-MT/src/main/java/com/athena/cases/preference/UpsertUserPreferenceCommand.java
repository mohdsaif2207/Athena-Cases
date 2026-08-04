package com.athena.cases.preference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Upsert body for a user preference value (JSON or plain text).
 */
public record UpsertUserPreferenceCommand(
        @NotBlank @Size(max = 50_000) String value
) {
}
