package com.athena.cases.features.billing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Intended Billing repository slice tests per architecture
 * ({@code @DataJpaTest} + optional Testcontainers).
 *
 * <p><b>Not runnable yet:</b> {@code cases-MT/pom.xml} has only
 * {@code spring-boot-starter-webmvc} / {@code webmvc-test}. Adding
 * {@code spring-boot-starter-data-jpa} (and a test DB) is Lead-owned and
 * must not be done from the Billing phase. Repository behaviour is covered
 * indirectly via mocked repository interactions in
 * {@code BillingDepartmentRequestServiceImplTest}.
 *
 * <p>Enable and convert to {@code @DataJpaTest} once foundation JPA deps land:
 * <ul>
 *   <li>{@code findByCaseId} loads entity + {@code holdLevels} via {@code @EntityGraph}</li>
 *   <li>{@code existsByCaseId} returns true after save</li>
 *   <li>{@code BillingRequestHoldLevelRepository.deleteByBillingRequest_Id}</li>
 * </ul>
 */
@Disabled("Requires spring-boot-starter-data-jpa — do not add from Billing; Lead foundation")
class BillingDepartmentRequestRepositoryTest {

    @Test
    void should_findByCaseIdWithHoldLevels_when_entityPersisted() {
        assertThat(true)
                .as("Placeholder until @DataJpaTest is available on the classpath")
                .isTrue();
    }

    @Test
    void should_returnTrue_when_existsByCaseIdAfterSave() {
        assertThat(true)
                .as("Placeholder until @DataJpaTest is available on the classpath")
                .isTrue();
    }

    @Test
    void should_deleteHoldLevels_when_deleteByBillingRequestId() {
        assertThat(true)
                .as("Placeholder until @DataJpaTest is available on the classpath")
                .isTrue();
    }
}
