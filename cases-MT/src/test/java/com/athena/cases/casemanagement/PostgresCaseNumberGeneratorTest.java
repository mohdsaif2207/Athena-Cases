package com.athena.cases.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class PostgresCaseNumberGeneratorTest {

    @Mock JdbcTemplate jdbcTemplate;
    @InjectMocks PostgresCaseNumberGenerator generator;

    @Test
    void should_returnExRPaddedNumber_when_nextvalReturnsValue() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(3L);

        String caseNumber = generator.next("exrt_case_number_seq", "ExR", 6);

        assertThat(caseNumber).isEqualTo("ExR000003");
        verify(jdbcTemplate).queryForObject("SELECT nextval('exrt_case_number_seq')", Long.class);
    }

    @Test
    void should_rejectUnsafeSequenceName() {
        assertThatThrownBy(() -> generator.next("exrt_case_number_seq;drop", "ExR", 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sequence name");
    }
}
