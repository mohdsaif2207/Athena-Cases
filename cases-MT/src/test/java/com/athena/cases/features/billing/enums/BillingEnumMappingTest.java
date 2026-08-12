package com.athena.cases.features.billing.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BillingEnumMappingTest {

    @Test
    void should_roundTripBillingRequestTypeDbValues() {
        // Arrange / Act / Assert
        for (BillingRequestType value : BillingRequestType.values()) {
            assertThat(BillingRequestType.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(BillingRequestType.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> BillingRequestType.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown BillingRequestType");
    }

    @Test
    void should_roundTripBillingExtractTypeDbValues() {
        for (BillingExtractType value : BillingExtractType.values()) {
            assertThat(BillingExtractType.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(BillingExtractType.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> BillingExtractType.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown BillingExtractType");
    }

    @Test
    void should_roundTripBillingHoldLevelCodeDbValues() {
        for (BillingHoldLevelCode value : BillingHoldLevelCode.values()) {
            assertThat(BillingHoldLevelCode.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(BillingHoldLevelCode.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> BillingHoldLevelCode.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown BillingHoldLevelCode");
    }

    @Test
    void should_roundTripBillingHoldTypeDbValues() {
        for (BillingHoldType value : BillingHoldType.values()) {
            assertThat(BillingHoldType.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(BillingHoldType.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> BillingHoldType.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown BillingHoldType");
    }

    @Test
    void should_roundTripPreNoteRequestTypeDbValues() {
        for (PreNoteRequestType value : PreNoteRequestType.values()) {
            assertThat(PreNoteRequestType.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(PreNoteRequestType.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> PreNoteRequestType.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown PreNoteRequestType");
    }

    @Test
    void should_roundTripPriorHardDeclinesDbValues() {
        for (PriorHardDeclines value : PriorHardDeclines.values()) {
            assertThat(PriorHardDeclines.fromDbValue(value.getDbValue())).isSameAs(value);
        }
        assertThat(PriorHardDeclines.fromDbValue(null)).isNull();
        assertThatThrownBy(() -> PriorHardDeclines.fromDbValue("Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown PriorHardDeclines");
    }
}
