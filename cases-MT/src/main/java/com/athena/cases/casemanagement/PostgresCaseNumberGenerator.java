package com.athena.cases.casemanagement;

import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reads the next value from a PostgreSQL sequence via {@code nextval}.
 * Sequence state lives in the database — survives restarts and is concurrency-safe.
 */
@Component
public class PostgresCaseNumberGenerator implements CaseNumberGenerator {

    private static final Logger log = LoggerFactory.getLogger(PostgresCaseNumberGenerator.class);
    private static final Pattern SAFE_SEQUENCE_NAME = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final JdbcTemplate jdbcTemplate;

    public PostgresCaseNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String next(String sequenceName, String prefix, int padWidth) {
        Objects.requireNonNull(sequenceName, "sequenceName");
        Objects.requireNonNull(prefix, "prefix");
        if (padWidth < 1 || padWidth > 18) {
            throw new IllegalArgumentException("padWidth must be between 1 and 18");
        }
        if (!SAFE_SEQUENCE_NAME.matcher(sequenceName).matches()) {
            throw new IllegalArgumentException("Invalid sequence name: " + sequenceName);
        }

        Long value = jdbcTemplate.queryForObject(
                "SELECT nextval('" + sequenceName + "')",
                Long.class);
        if (value == null) {
            throw new IllegalStateException("Sequence returned null: " + sequenceName);
        }

        String caseNumber = prefix + String.format("%0" + padWidth + "d", value);
        log.debug("generated case number - sequence={}, caseNumber={}", sequenceName, caseNumber);
        return caseNumber;
    }
}
