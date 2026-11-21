package com.medislot.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.medislot.api.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MediSlotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediSlotApplication.class, args);
    }
}
