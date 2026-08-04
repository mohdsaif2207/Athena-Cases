package com.athena.cases.preference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceImplTest {

    @Mock
    UserPreferenceRepository repository;

    @Mock
    CurrentUserService currentUserService;

    @InjectMocks
    UserPreferenceServiceImpl service;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(
                7L,
                "fm_admin",
                "hash",
                "Admin",
                true,
                List.of("SYSTEM_ADMIN"),
                List.of("CASES_VIEW"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        lenient().when(currentUserService.requirePrincipal()).thenReturn(principal);
    }

    @Test
    void should_returnPreference_when_exists() {
        UserPreferenceEntity entity = new UserPreferenceEntity();
        entity.setPrefKey("CASES_SEARCH_COLUMNS");
        entity.setPrefValue("[{\"key\":\"caseId\",\"visible\":true}]");
        when(repository.findByUserIdAndPrefKey(7L, "CASES_SEARCH_COLUMNS")).thenReturn(Optional.of(entity));

        UserPreferenceResponse actual = service.getMine("CASES_SEARCH_COLUMNS");

        assertThat(actual.key()).isEqualTo("CASES_SEARCH_COLUMNS");
        assertThat(actual.value()).contains("caseId");
    }

    @Test
    void should_throwNotFound_when_missing() {
        when(repository.findByUserIdAndPrefKey(7L, "MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_createPreference_when_upsertNew() {
        when(repository.findByUserIdAndPrefKey(7L, "CASES_SEARCH_COLUMNS")).thenReturn(Optional.empty());
        when(repository.save(any(UserPreferenceEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPreferenceResponse actual = service.upsertMine(
                "CASES_SEARCH_COLUMNS",
                new UpsertUserPreferenceCommand("[]"));

        assertThat(actual.value()).isEqualTo("[]");
        ArgumentCaptor<UserPreferenceEntity> captor = ArgumentCaptor.forClass(UserPreferenceEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("fm_admin");
    }

    @Test
    void should_rejectInvalidKey() {
        assertThatThrownBy(() -> service.getMine("bad key!"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
