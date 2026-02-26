package com.prm.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void flywayShouldCreateSystemTables() throws Exception {
        try (var conn = dataSource.getConnection()) {
            try (var rs = conn.getMetaData().getTables(null, null, "sys_user", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getTables(null, null, "sys_role", null)) {
                assertThat(rs.next()).isTrue();
            }
        }
    }

    @Test
    void flywayShouldCreateProjectTables() throws Exception {
        try (var conn = dataSource.getConnection()) {
            try (var rs = conn.getMetaData().getTables(null, null, "pm_project", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getTables(null, null, "pm_bug", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getTables(null, null, "pm_sprint", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getTables(null, null, "pm_dashboard_snapshot", null)) {
                assertThat(rs.next()).isTrue();
            }
        }
    }

    @Test
    void adminUserShouldExist() throws Exception {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM sys_user WHERE username = 'admin'");
             var rs = stmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }
    }
}
