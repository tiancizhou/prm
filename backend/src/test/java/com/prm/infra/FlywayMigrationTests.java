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
    void flywayShouldCreateDepartmentTables() throws Exception {
        try (var conn = dataSource.getConnection()) {
            try (var rs = conn.getMetaData().getTables(null, null, "sys_company", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getTables(null, null, "sys_department", null)) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getColumns(null, null, "sys_department", "company_id")) {
                assertThat(rs.next()).isTrue();
            }
            try (var rs = conn.getMetaData().getColumns(null, null, "sys_user", "department_id")) {
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

    @Test
    void flywayShouldKeepOnlyCoreRolesActive() throws Exception {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement("SELECT code FROM sys_role WHERE deleted = 0 ORDER BY id");
             var rs = stmt.executeQuery()) {
            var codes = new java.util.ArrayList<String>();
            while (rs.next()) {
                codes.add(rs.getString(1));
            }

            assertThat(codes).contains("SUPER_ADMIN", "PROJECT_ADMIN", "DEV");
            assertThat(codes).doesNotContain("PM", "QA", "GUEST");
        }
    }
}
