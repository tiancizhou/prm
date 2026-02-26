package com.prm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PrmApplication {
    public static void main(String[] args) {
        ensureDataDirExists();
        SpringApplication.run(PrmApplication.class, args);
    }

    /** 确保 SQLite 数据库所在目录存在，避免从项目根目录启动时 ./data 不存在 */
    private static void ensureDataDirExists() {
        String dbPath = System.getenv("PRM_DB_PATH");
        if (dbPath == null || dbPath.isBlank()) {
            dbPath = System.getProperty("PRM_DB_PATH", "./data/prm.db");
        }
        Path path = Paths.get(dbPath).toAbsolutePath().normalize();
        File parent = path.getParent().toFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
    }
}
