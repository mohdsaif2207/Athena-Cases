package com.athena.cases.preference;

import com.athena.cases.common.exception.ResourceNotFoundException;
import com.athena.cases.security.CurrentUserService;
import com.athena.cases.security.UserPrincipal;
import java.time.Instant;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(UserPreferenceServiceImpl.class);
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,120}$");

    private final UserPreferenceRepository repository;
    private final CurrentUserService currentUserService;

    public UserPreferenceServiceImpl(
            UserPreferenceRepository repository,
            CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getMine(String key) {
        String prefKey = requireValidKey(key);
        UserPrincipal principal = currentUserService.requirePrincipal();
        UserPreferenceEntity entity = repository.findByUserIdAndPrefKey(principal.getUserId(), prefKey)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreference", prefKey));
        return new UserPreferenceResponse(entity.getPrefKey(), entity.getPrefValue());
    }

    @Override
    @Transactional
    public UserPreferenceResponse upsertMine(String key, UpsertUserPreferenceCommand command) {
        String prefKey = requireValidKey(key);
        UserPrincipal principal = currentUserService.requirePrincipal();
        Instant now = Instant.now();
        String actor = principal.getUsername();

        UserPreferenceEntity entity = repository.findByUserIdAndPrefKey(principal.getUserId(), prefKey)
                .orElseGet(() -> {
                    UserPreferenceEntity created = new UserPreferenceEntity();
                    created.setUserId(principal.getUserId());
                    created.setPrefKey(prefKey);
                    created.setCreatedAt(now);
                    created.setCreatedBy(actor);
                    created.setVersion(1);
                    return created;
                });

        entity.setPrefValue(command.value());
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(actor);
        UserPreferenceEntity saved = repository.save(entity);
        log.info("user preference saved - userId={} key={}", principal.getUserId(), prefKey);
        return new UserPreferenceResponse(saved.getPrefKey(), saved.getPrefValue());
    }

    @Override
    @Transactional
    public void deleteMine(String key) {
        String prefKey = requireValidKey(key);
        UserPrincipal principal = currentUserService.requirePrincipal();
        repository.deleteByUserIdAndPrefKey(principal.getUserId(), prefKey);
        log.info("user preference deleted - userId={} key={}", principal.getUserId(), prefKey);
    }

    private static String requireValidKey(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid preference key");
        }
        return key;
    }
}
