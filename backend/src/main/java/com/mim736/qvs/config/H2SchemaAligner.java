package com.mim736.qvs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate {@code ddl-auto=update} does not widen H2 ENUM types. A file database created
 * before STUDENT existed still only allows ADMIN, ISSUER and VERIFIER.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class H2SchemaAligner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(H2SchemaAligner.class);

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public H2SchemaAligner(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (!url.contains(":h2:")) {
            return;
        }
        widenIfEnum("USERS", "ROLE");
        widenIfEnum("USERS", "STUDENT_STAGE");
    }

    private void widenIfEnum(String table, String column) {
        try {
            String dataType = jdbcTemplate.query(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
                            + "WHERE TABLE_SCHEMA = 'PUBLIC' AND UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    table,
                    column
            );
            if (dataType == null || !dataType.toUpperCase().contains("ENUM")) {
                return;
            }
            jdbcTemplate.execute(
                    "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DATA TYPE VARCHAR(32)"
            );
            LOG.info("Converted H2 {}.{} from ENUM to VARCHAR so student accounts can be seeded", table, column);
        } catch (DataAccessException ex) {
            LOG.debug("Skip aligning {}.{}: {}", table, column, ex.getMessage());
        }
    }
}
