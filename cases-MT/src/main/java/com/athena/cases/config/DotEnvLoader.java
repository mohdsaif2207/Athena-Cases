package com.athena.cases.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads {@code .env} into JVM system properties for local development.
 * Does not override values already present in the OS environment or system properties
 * (production / CI remain authoritative).
 */
public final class DotEnvLoader {

    private DotEnvLoader() {
    }

    /**
     * Loads the first readable {@code .env} found next to the process working directory.
     */
    public static void loadIfPresent() {
        Path envFile = resolveEnvFile();
        if (envFile == null) {
            return;
        }
        try {
            for (String raw : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = stripQuotes(line.substring(eq + 1).trim());
                if (key.isEmpty()) {
                    continue;
                }
                setIfAbsent(key, value);
                // Spring Boot binds env-style names from the OS environment, but system
                // properties need canonical spring.* keys (e.g. SPRING_FLYWAY_SCHEMAS → spring.flyway.schemas).
                String springKey = toSpringPropertyKey(key);
                if (springKey != null) {
                    setIfAbsent(springKey, value);
                }
            }
        } catch (IOException ignored) {
            // Local convenience only
        }
    }

    private static void setIfAbsent(String key, String value) {
        if (isBlank(System.getenv(key)) && isBlank(System.getProperty(key))) {
            System.setProperty(key, value);
        }
    }

    /**
     * Converts {@code SPRING_FOO_BAR} to {@code spring.foo.bar} for system-property binding.
     * Returns null when the key is not a SPRING_* setting.
     * <p>
     * Special-case: {@code HIBERNATE_DEFAULT_SCHEMA} must remain {@code hibernate.default_schema}
     * (underscore), not {@code hibernate.default.schema}.
     */
    static String toSpringPropertyKey(String envKey) {
        if (envKey == null || !envKey.startsWith("SPRING_") || envKey.length() <= "SPRING_".length()) {
            return null;
        }
        String remainder = envKey.substring("SPRING_".length());
        if (remainder.endsWith("HIBERNATE_DEFAULT_SCHEMA")) {
            String prefix = remainder.substring(0, remainder.length() - "HIBERNATE_DEFAULT_SCHEMA".length());
            String dottedPrefix = prefix.isEmpty()
                    ? ""
                    : prefix.toLowerCase().replace('_', '.');
            return "spring." + dottedPrefix + "hibernate.default_schema";
        }
        return "spring." + remainder.toLowerCase().replace('_', '.');
    }

    private static Path resolveEnvFile() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        Path[] candidates = {
                cwd.resolve(".env"),
                cwd.resolve("cases-MT").resolve(".env"),
                cwd.getParent() != null ? cwd.getParent().resolve("cases-MT").resolve(".env") : null
        };
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
