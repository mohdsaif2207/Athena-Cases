package com.athena.cases.features.billing.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BillingRequestHoldLevelTest {

    @Test
    void should_storeAndReturnFieldValues() {
        // Arrange
        BillingDepartmentRequest billingRequest = new BillingDepartmentRequest();
        BillingRequestHoldLevel holdLevel = new BillingRequestHoldLevel();
        Instant createdAt = Instant.parse("2026-08-04T11:00:00Z");

        // Act
        holdLevel.setId(7L);
        holdLevel.setBillingRequest(billingRequest);
        holdLevel.setHoldLevelCode(BillingHoldLevelCode.REFUND);
        holdLevel.setCreatedAt(createdAt);

        // Assert
        assertThat(holdLevel.getId()).isEqualTo(7L);
        assertThat(holdLevel.getBillingRequest()).isSameAs(billingRequest);
        assertThat(holdLevel.getHoldLevelCode()).isEqualTo(BillingHoldLevelCode.REFUND);
        assertThat(holdLevel.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void should_useIdForEqualityAndHashCode() {
        // Arrange
        BillingRequestHoldLevel left = new BillingRequestHoldLevel();
        BillingRequestHoldLevel right = new BillingRequestHoldLevel();
        BillingRequestHoldLevel different = new BillingRequestHoldLevel();

        left.setId(21L);
        right.setId(21L);
        different.setId(22L);

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
        assertThat(left).isNotEqualTo(new Object());
    }
}
