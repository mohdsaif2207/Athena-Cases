package com.athena.cases.features.billing.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.athena.cases.features.billing.enums.BillingHoldLevelCode;
import com.athena.cases.lookup.LookupItem;
import java.util.List;
import org.junit.jupiter.api.Test;

class BillingLookupModelTest {

    @Test
    void should_mapLookupItemToBillingAssigneeResponse() {
        // Arrange
        LookupItem item = new LookupItem("user-123", "charan", "Charan KB");

        // Act
        BillingAssigneeLookupResponse response = BillingAssigneeLookupResponse.from(item);

        // Assert
        assertThat(response.username()).isEqualTo("charan");
        assertThat(response.displayName()).isEqualTo("Charan KB");
        assertThat(response.toString()).contains("username=charan", "displayName=Charan KB");
    }

    @Test
    void should_supportAssigneeResponseRecordSemantics() {
        // Arrange
        BillingAssigneeLookupResponse left = new BillingAssigneeLookupResponse("charan", "Charan KB");
        BillingAssigneeLookupResponse right = new BillingAssigneeLookupResponse("charan", "Charan KB");
        BillingAssigneeLookupResponse different = new BillingAssigneeLookupResponse("mtcharan", "MT Charan");

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
    }

    @Test
    void should_storeAndExposeHoldLevelsLookupValues() {
        // Arrange
        List<BillingHoldLevelCode> levels = List.of(BillingHoldLevelCode.ALL, BillingHoldLevelCode.REFUND);

        // Act
        HoldLevelsLookupResponse response = new HoldLevelsLookupResponse(levels);

        // Assert
        assertThat(response.available()).containsExactly(BillingHoldLevelCode.ALL, BillingHoldLevelCode.REFUND);
        assertThat(response.toString()).contains("ALL", "REFUND");
    }

    @Test
    void should_supportHoldLevelsLookupRecordSemantics() {
        // Arrange
        HoldLevelsLookupResponse left = new HoldLevelsLookupResponse(List.of(BillingHoldLevelCode.BILLING));
        HoldLevelsLookupResponse right = new HoldLevelsLookupResponse(List.of(BillingHoldLevelCode.BILLING));
        HoldLevelsLookupResponse different = new HoldLevelsLookupResponse(List.of(BillingHoldLevelCode.ALL));

        // Assert
        assertThat(left).isEqualTo(right);
        assertThat(left).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(different);
    }
}
