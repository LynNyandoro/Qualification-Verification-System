package com.mim736.qvs.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PostgresSchemaAlignerTest {

    @Test
    void skipsWhenDatasourceIsNotPostgres() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:h2:mem:test");

        new PostgresSchemaAligner(jdbcTemplate, environment).run(new DefaultApplicationArguments());

        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void convertsOnlyByteaColumns() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/qvs");

        Map<String, String> columnTypes = Map.of(
                "holder_name", "bytea",
                "verification_code", "character varying",
                "credential_id", "character varying",
                "holder_national_id", "character varying",
                "holder_username", "character varying",
                "title", "character varying",
                "issuing_institution", "character varying"
        );

        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(), any()))
                .thenAnswer(invocation -> columnTypes.get(invocation.getArgument(3)));

        new PostgresSchemaAligner(jdbcTemplate, environment).run(new DefaultApplicationArguments());

        verify(jdbcTemplate).execute(
                "ALTER TABLE qualifications ALTER COLUMN holder_name TYPE VARCHAR(160) "
                        + "USING convert_from(holder_name, 'UTF8')"
        );
        verify(jdbcTemplate, never()).execute(
                "ALTER TABLE qualifications ALTER COLUMN verification_code TYPE VARCHAR(48) "
                        + "USING convert_from(verification_code, 'UTF8')"
        );
    }
}
