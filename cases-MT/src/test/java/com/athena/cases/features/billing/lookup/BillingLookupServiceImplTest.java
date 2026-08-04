package com.athena.cases.features.billing.lookup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.athena.cases.casemanagement.CaseEntity;
import com.athena.cases.casemanagement.CaseRepository;
import com.athena.cases.identity.entity.CaseTypeEntity;
import com.athena.cases.identity.entity.UserEntity;
import com.athena.cases.identity.repository.CaseTypeRepository;
import com.athena.cases.identity.repository.UserRepository;
import com.athena.cases.lookup.LookupItem;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillingLookupServiceImplTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private CaseTypeRepository caseTypeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private BillingLookupServiceImpl service;

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
                List.of("CASES_VIEW", "CASES_CREATE"),
                List.of(),
                List.of(),
                List.of("BILLING_DEPARTMENT_REQUEST"),
                List.of(),
                List.of("BILLING_OPS_TEAM"));
    }

    @Test
    void should_returnBillingCampaignsFromMockData() {
        assertThat(service.listCampaigns()).extracting(LookupItem::code)
                .contains("CMP001", "CMP002", "CMP003");
    }

    @Test
    void should_returnProductsAndSegmentsFromMockData() {
        assertThat(service.listProducts(null)).extracting(LookupItem::id).contains("101");
        assertThat(service.listSegments("CLIENT001")).isNotEmpty();
    }

    @Test
    void should_returnOnlyBillingCaseIds_when_findParentCases() {
        when(currentUserService.hasPermission("CASES_VIEW")).thenReturn(true);

        CaseTypeEntity billing = new CaseTypeEntity();
        billing.setId(3L);
        billing.setCode("BILLING_DEPARTMENT_REQUEST");
        when(caseTypeRepository.findByCode("BILLING_DEPARTMENT_REQUEST"))
                .thenReturn(Optional.of(billing));

        CaseEntity existing = new CaseEntity();
        existing.setId(11L);
        existing.setCaseNumber("BIL000001");
        existing.setSubject("Billing Department Request");
        when(caseRepository.findAuthorized(eq(List.of(3L)), eq(false))).thenReturn(List.of(existing));

        List<LookupItem> rows = service.findParentCases(null);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().id()).isEqualTo("11");
        assertThat(rows.getFirst().code()).isEqualTo("BIL000001");
    }

    @Test
    void should_returnEmptyParents_when_billingCaseTypeMissing() {
        when(currentUserService.hasPermission("CASES_VIEW")).thenReturn(true);
        when(caseTypeRepository.findByCode("BILLING_DEPARTMENT_REQUEST")).thenReturn(Optional.empty());

        assertThat(service.findParentCases(null)).isEmpty();
        verifyNoInteractions(caseRepository);
    }

    @Test
    void should_returnBillingTeamAssigneesOnly() {
        when(currentUserService.hasPermission("CASES_VIEW")).thenReturn(true);

        UserEntity opsUser = new UserEntity();
        opsUser.setUsername("billing.ops");
        opsUser.setDisplayName("Billing Ops");
        opsUser.setStatus("ACTIVE");
        when(userRepository.findActiveByTeamCode("BILLING_OPS_TEAM")).thenReturn(List.of(opsUser));

        List<LookupItem> rows = service.listAssignees();

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().code()).isEqualTo("billing.ops");
        assertThat(rows.getFirst().label()).isEqualTo("Billing Ops");
    }

    @Test
    void should_detectBillingCaseByType() {
        CaseTypeEntity billing = new CaseTypeEntity();
        billing.setId(3L);
        billing.setCode("BILLING_DEPARTMENT_REQUEST");
        when(caseTypeRepository.findByCode("BILLING_DEPARTMENT_REQUEST"))
                .thenReturn(Optional.of(billing));

        CaseEntity billingCase = new CaseEntity();
        billingCase.setId(11L);
        billingCase.setCaseTypeId(3L);
        when(caseRepository.findById(11L)).thenReturn(Optional.of(billingCase));

        CaseEntity other = new CaseEntity();
        other.setId(22L);
        other.setCaseTypeId(1L);
        when(caseRepository.findById(22L)).thenReturn(Optional.of(other));

        assertThat(service.isBillingCase(11L)).isTrue();
        assertThat(service.isBillingCase(22L)).isFalse();
        assertThat(service.isBillingCase(null)).isFalse();
    }
}
