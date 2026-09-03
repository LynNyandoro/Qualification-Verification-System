package com.mim736.qvs.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class H2SchemaAlignerTest {

    @Test
    void convertsLegacyRoleEnumSoStudentCanBeInserted() {
        String url = "jdbc:h2:mem:aligner;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE users ("
                + "id INT, "
                + "role ENUM('ADMIN', 'ISSUER', 'VERIFIER'), "
                + "student_stage ENUM('ENROLLED', 'GRADUATING', 'ALUMNI')"
                + ")");

        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", url);
        new H2SchemaAligner(jdbc, environment).run(new DefaultApplicationArguments());

        jdbc.update("INSERT INTO users VALUES (1, 'STUDENT', 'ENROLLED')");
        assertEquals("STUDENT", jdbc.queryForObject("SELECT role FROM users WHERE id = 1", String.class));
    }
}
