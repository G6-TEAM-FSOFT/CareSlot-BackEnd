package com.org.care_slot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Files;

@SpringBootApplication
@EnableScheduling
public class CareSlotApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(CareSlotApplication.class, args);
    }

    private static void loadDotenv() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File("CareSlot-BackEnd/.env");
        }
        if (envFile.exists()) {
            try {
                Files.readAllLines(envFile.toPath()).forEach(line -> {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String val = line.substring(idx + 1).trim();
                        System.setProperty(key, val);
                        if ("SPRING_MAIL_USERNAME".equals(key)) {
                            System.setProperty("MAIL_USERNAME", val);
                        }
                        if ("SPRING_MAIL_PASSWORD".equals(key)) {
                            System.setProperty("MAIL_PASSWORD", val);
                        }
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }
}
