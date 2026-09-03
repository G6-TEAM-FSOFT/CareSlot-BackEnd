package com.org.care_slot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CareSlotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareSlotApplication.class, args);
    }

}
