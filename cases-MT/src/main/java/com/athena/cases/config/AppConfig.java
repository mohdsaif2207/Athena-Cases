package com.athena.cases.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers application configuration properties (Spring configuration only).
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, AdminSeedProperties.class})
public class AppConfig {
}
