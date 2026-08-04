package com.athena.cases.preference;

public interface UserPreferenceService {

    UserPreferenceResponse getMine(String key);

    UserPreferenceResponse upsertMine(String key, UpsertUserPreferenceCommand command);

    void deleteMine(String key);
}
