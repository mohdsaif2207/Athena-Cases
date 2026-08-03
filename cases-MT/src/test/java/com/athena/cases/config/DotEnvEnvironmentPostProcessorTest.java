package com.athena.cases.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class DotEnvEnvironmentPostProcessorTest {

    @TempDir
    Path tempDir;

    private String previousUserDir;

    @BeforeEach
    void saveUserDir() {
        previousUserDir = System.getProperty("user.dir");
    }

    @AfterEach
    void restoreUserDir() {
        if (previousUserDir != null) {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    void should_loadDotEnv_when_filePresentAndKeysNotAlreadySet() throws Exception {
        Files.writeString(tempDir.resolve(".env"), "DB_USER=postgres\nDB_PASSWORD=secret-from-file\n");
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

        MockEnvironment environment = new MockEnvironment();
        new DotEnvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("DB_USER")).isEqualTo("postgres");
        assertThat(environment.getProperty("DB_PASSWORD")).isEqualTo("secret-from-file");
        assertThat(environment.getPropertySources().contains(DotEnvEnvironmentPostProcessor.PROPERTY_SOURCE_NAME))
                .isTrue();
    }

    @Test
    void should_notOverrideExistingEnvironmentProperty() throws Exception {
        Files.writeString(tempDir.resolve(".env"), "DB_PASSWORD=from-file\n");
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("DB_PASSWORD", "from-os");
        new DotEnvEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("DB_PASSWORD")).isEqualTo("from-os");
    }

    @Test
    void should_failFast_when_dbPasswordIsBlank() throws Exception {
        Files.writeString(tempDir.resolve(".env"), "DB_PASSWORD=\n");
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

        MockEnvironment environment = new MockEnvironment();
        assertThatThrownBy(() ->
                        new DotEnvEnvironmentPostProcessor()
                                .postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_PASSWORD is set but blank");
    }
}
