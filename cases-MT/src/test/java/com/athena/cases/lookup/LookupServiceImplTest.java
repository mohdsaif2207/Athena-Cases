package com.athena.cases.lookup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.athena.cases.common.exception.ForbiddenException;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LookupServiceImplTest {

    @Mock
    private CaseTypeRepository caseTypeRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private LookupServiceImpl service;

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
                List.of("CASES_CREATE"),
                List.of(),
                List.of(),
                List.of("DBM_WORK_ORDER_REQUEST", "EXRT_REQUEST", "BILLING_DEPARTMENT_REQUEST"),
                List.of(),
                List.of());
    }

    @Test
    void should_returnAuthorizedCaseTypes_inDisplayOrder() {
        when(currentUserService.hasPermission("CASES_CREATE")).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(principal);

        CaseTypeEntity billing = type(3L, "BILLING_DEPARTMENT_REQUEST", "Billing Department Request");
        CaseTypeEntity dbm = type(1L, "DBM_WORK_ORDER_REQUEST", "DBM Work Order Request");
        CaseTypeEntity exrt = type(2L, "EXRT_REQUEST", "Executive Response Team (ExRT) Request");
        when(caseTypeRepository.findByCodeInAndActiveTrue(
                        List.of("DBM_WORK_ORDER_REQUEST", "EXRT_REQUEST", "BILLING_DEPARTMENT_REQUEST")))
                .thenReturn(List.of(billing, exrt, dbm));

        List<LookupItem> rows = service.listAuthorizedCaseTypes();

        assertThat(rows).extracting(LookupItem::label)
                .containsExactly(
                        "DBM Work Order Request",
                        "Executive Response Team (ExRT) Request",
                        "Billing Department Request");
    }

    @Test
    void should_returnEmpty_when_noCaseTypesAssigned() {
        UserPrincipal emptyScope = new UserPrincipal(
                2L, "user", "hash", "User", true,
                List.of("AGENT"), List.of("CASES_CREATE"), List.of(), List.of(),
                List.of(), List.of(), List.of());
        when(currentUserService.hasPermission("CASES_CREATE")).thenReturn(true);
        when(currentUserService.requirePrincipal()).thenReturn(emptyScope);

        assertThat(service.listAuthorizedCaseTypes()).isEmpty();
        verifyNoInteractions(caseTypeRepository);
    }

    @Test
    void should_throwForbidden_when_missingCreatePermission() {
        when(currentUserService.hasPermission("CASES_CREATE")).thenReturn(false);
        when(currentUserService.hasPermission("CASES_ACCESS")).thenReturn(false);

        assertThatThrownBy(() -> service.listAuthorizedCaseTypes())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("CASES_CREATE");
    }

    private static CaseTypeEntity type(Long id, String code, String name) {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setActive(true);
        return entity;
    }
}
