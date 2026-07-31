package com.athena.cases.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaseManagementServiceImplTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private CaseTypeRepository caseTypeRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CaseManagementServiceImpl service;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(
                1L,
                "fm_admin",
                "hash",
                "ADMKL",
                true,
                List.of("SYSTEM_ADMINISTRATOR"),
                List.of("CASES_VIEW", "CASES_ACCESS"),
                List.of(),
                List.of(),
                List.of("BILLING_DEPARTMENT_REQUEST"),
                List.of(),
                List.of("BILLING_OPS_TEAM"));
    }

    @Test
    void should_returnAuthorizedCases_when_userHasCaseTypeScope() {
        when(currentUserService.hasPermission("CASES_VIEW")).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(principal);

        CaseTypeEntity type = new CaseTypeEntity();
        type.setId(10L);
        type.setCode("BILLING_DEPARTMENT_REQUEST");
        type.setName("Billing Department Request");
        when(caseTypeRepository.findByCodeIn(List.of("BILLING_DEPARTMENT_REQUEST"))).thenReturn(List.of(type));

        CaseEntity entity = new CaseEntity();
        entity.setId(1L);
        entity.setCaseNumber("CASE-1001");
        entity.setCaseTypeId(10L);
        entity.setSubject("Sample");
        entity.setCaseOwner("ADMKL");
        entity.setCaseStatus("Requested");
        entity.setPriority("Medium");
        entity.setCreatedAt(Instant.parse("2026-07-31T10:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-07-31T11:00:00Z"));
        when(caseRepository.findAuthorized(anyList(), eq(false))).thenReturn(List.of(entity));

        List<CaseListItem> rows = service.listAuthorizedCases();

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().caseId()).isEqualTo("CASE-1001");
        assertThat(rows.getFirst().caseType()).isEqualTo("Billing Department Request");
    }

    @Test
    void should_throwForbidden_when_missingCasesView() {
        when(currentUserService.hasPermission("CASES_VIEW")).thenReturn(false);
        when(currentUserService.hasPermission("CASES_ACCESS")).thenReturn(false);

        assertThatThrownBy(() -> service.listAuthorizedCases())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("CASES_VIEW");
        verifyNoInteractions(caseRepository);
    }
}
