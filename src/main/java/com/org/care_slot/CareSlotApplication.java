package com.org.care_slot;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Files;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class CareSlotApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        loadDotenv();
        SpringApplication.run(CareSlotApplication.class, args);
    }

    private static void loadDotenv() {
        String[] possiblePaths = {
            ".env",
            "BE/care-slot/.env",
            "care-slot/.env",
            "CareSlot-BackEnd/.env",
            "../.env",
            "../BE/care-slot/.env"
        };

        File envFile = null;
        for (String path : possiblePaths) {
            File candidate = new File(path);
            if (candidate.exists() && candidate.isFile()) {
                envFile = candidate;
                break;
            }
        }

        if (envFile != null && envFile.exists()) {
            try {
                Files.readAllLines(envFile.toPath()).forEach(line -> {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String val = line.substring(idx + 1).trim();
                        val = val.replaceAll("^[\"']|[\"']$", "");

                        if (System.getProperty(key) == null) {
                            System.setProperty(key, val);
                        }
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
