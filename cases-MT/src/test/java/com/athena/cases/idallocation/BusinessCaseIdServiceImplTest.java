package com.athena.cases.idallocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.athena.cases.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessCaseIdServiceImplTest {

    @Mock
    private BusinessCaseIdSequenceRepository sequenceRepository;

    private BusinessCaseIdServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessCaseIdServiceImpl(sequenceRepository);
    }

    @Test
    void should_returnFormattedId_and_incrementSequence_when_allocate() {
        BusinessCaseIdSequenceEntity row = new BusinessCaseIdSequenceEntity();
        row.setCaseTypeCode("BILLING_DEPARTMENT_REQUEST");
        row.setPrefix("BIL");
        row.setWidth(6);
        row.setNextValue(1L);
        row.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        when(sequenceRepository.findByCaseTypeCodeForUpdate("BILLING_DEPARTMENT_REQUEST"))
                .thenReturn(Optional.of(row));
        when(sequenceRepository.save(any(BusinessCaseIdSequenceEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String id = service.allocate("BILLING_DEPARTMENT_REQUEST");

        assertThat(id).isEqualTo("BIL000001");
        ArgumentCaptor<BusinessCaseIdSequenceEntity> captor =
                ArgumentCaptor.forClass(BusinessCaseIdSequenceEntity.class);
        verify(sequenceRepository).save(captor.capture());
        assertThat(captor.getValue().getNextValue()).isEqualTo(2L);
    }

    @Test
    void should_throwNotFound_when_caseTypeNotRegistered() {
        when(sequenceRepository.findByCaseTypeCodeForUpdate("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.allocate("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void should_formatPrefixAndWidth() {
        assertThat(BusinessCaseIdServiceImpl.format("BIL", 6, 42)).isEqualTo("BIL000042");
        assertThat(BusinessCaseIdServiceImpl.format("DBM", 6, 1)).isEqualTo("DBM000001");
    }
}
