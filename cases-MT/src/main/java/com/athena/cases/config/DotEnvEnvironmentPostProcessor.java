package com.athena.cases.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads gitignored {@code .env} into the Spring Environment for local Maven/IDE runs.
 *
 * <p>Search order (first existing file wins). Existing OS/Docker/IDE env vars are never overwritten:
 *
 * <ol>
 *   <li>{@code <user.dir>/.env}
 *   <li>{@code <user.dir>/../.env} (monorepo root when cwd is {@code cases-MT})
 * </ol>
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "athenaDotEnv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = resolveEnvFile();
        if (envFile != null) {
            Map<String, Object> fromFile = parseDotEnv(envFile);
            Map<String, Object> toAdd = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : fromFile.entrySet()) {
                if (!environment.containsProperty(e.getKey())) {
                    toAdd.put(e.getKey(), e.getValue());
                }
            }
            if (!toAdd.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, toAdd));
            }
        }

        rejectBlankDbPassword(environment);
    }

    private static void rejectBlankDbPassword(ConfigurableEnvironment environment) {
        String password = environment.getProperty("DB_PASSWORD");
        if (password != null && password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD is set but blank. PostgreSQL rejects this with: "
                            + "FATAL: password authentication failed for user \"postgres\". "
                            + "Set a non-empty DB_PASSWORD in Athena-Cases/.env (do not leave DB_PASSWORD= empty).");
        }
    }

    private static Path resolveEnvFile() {
        Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        List<Path> candidates = List.of(
                cwd.resolve(".env"),
                cwd.getParent() != null ? cwd.getParent().resolve(".env") : null);
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static Map<String, Object> parseDotEnv(Path file) {
        Map<String, Object> values = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read .env file: " + file, ex);
        }
        return values;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
