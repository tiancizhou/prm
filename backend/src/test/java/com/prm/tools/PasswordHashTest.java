package com.prm.tools;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

class PasswordHashTest {

    @Test
    void generateAndVerifyHash() {
        String password = "Admin@123";
        String hash = BCrypt.hashpw(password);
        System.out.println("=== BCrypt Hash for Admin@123 ===");
        System.out.println(hash);
        System.out.println("Verify: " + BCrypt.checkpw(password, hash));

        // Test the existing hash in SQL
        String existingHash = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
        System.out.println("Existing hash valid for Admin@123: " + BCrypt.checkpw(password, existingHash));
        System.out.println("Existing hash valid for 123456: " + BCrypt.checkpw("123456", existingHash));
    }
}
