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

import java.util.List;

/**
 * Hibernate {@code ddl-auto=update} does not always repair pre-existing PostgreSQL column types.
 * If older qualifications columns were created as BYTEA, case-insensitive search queries fail.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostgresSchemaAligner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresSchemaAligner.class);

    private static final List<ColumnSpec> QUALIFICATION_TEXT_COLUMNS = List.of(
            new ColumnSpec("holder_name", "VARCHAR(160)"),
            new ColumnSpec("verification_code", "VARCHAR(48)"),
            new ColumnSpec("credential_id", "VARCHAR(48)"),
            new ColumnSpec("holder_national_id", "VARCHAR(40)"),
            new ColumnSpec("holder_username", "VARCHAR(80)"),
            new ColumnSpec("title", "VARCHAR(200)"),
            new ColumnSpec("issuing_institution", "VARCHAR(200)")
    );

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public PostgresSchemaAligner(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (!url.contains(":postgresql:")) {
            return;
        }
        for (ColumnSpec column : QUALIFICATION_TEXT_COLUMNS) {
            alignIfBytea("qualifications", column);
        }
    }

    private void alignIfBytea(String table, ColumnSpec column) {
        try {
            String dataType = jdbcTemplate.query(
                    "SELECT data_type FROM information_schema.columns "
                            + "WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    table,
                    column.name()
            );
            if (dataType == null || !"bytea".equalsIgnoreCase(dataType)) {
                return;
            }
            jdbcTemplate.execute(
                    "ALTER TABLE " + table + " ALTER COLUMN " + column.name()
                            + " TYPE " + column.targetType()
                            + " USING convert_from(" + column.name() + ", 'UTF8')"
            );
            LOG.info("Converted PostgreSQL {}.{} from BYTEA to {}", table, column.name(), column.targetType());
        } catch (DataAccessException ex) {
            LOG.debug("Skip aligning {}.{}: {}", table, column.name(), ex.getMessage());
        }
    }

    private record ColumnSpec(String name, String targetType) {
    }
}
