package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("postgres-test")
class PostgresConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldConnectToPostgres() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();

            assertEquals("PostgreSQL", metadata.getDatabaseProductName());
            System.out.println("Base de datos: " + metadata.getDatabaseProductName());
            System.out.println("Versión: " + metadata.getDatabaseProductVersion());
        }
    }
}
