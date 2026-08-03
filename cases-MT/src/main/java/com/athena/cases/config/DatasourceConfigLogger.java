package com.athena.cases.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Logs resolved datasource URL/username (never the password) as soon as the Environment is ready.
 * Runs before Flyway so operators can see which credentials Spring will use.
 */
public class DatasourceConfigLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(DatasourceConfigLogger.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        var env = event.getEnvironment();
        String url = env.getProperty("spring.datasource.url", env.getProperty("DB_URL", "(unset)"));
        String user = env.getProperty("spring.datasource.username", env.getProperty("DB_USER", "(unset)"));
        boolean passwordSet = hasText(env.getProperty("spring.datasource.password"))
                || hasText(env.getProperty("DB_PASSWORD"));
        log.info(
                "Resolved datasource (pre-Flyway) - url={}, username={}, passwordSet={}, dotEnvLoaded={}",
                url,
                user,
                passwordSet,
                env.getPropertySources().contains(DotEnvEnvironmentPostProcessor.PROPERTY_SOURCE_NAME));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
