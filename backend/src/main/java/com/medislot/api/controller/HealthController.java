package com.medislot.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "medislot-api");
    }

    @GetMapping("/api/db-check")
    public ResponseEntity<Map<String, String>> databaseCheck() {
        try {
            String database = jdbcTemplate.queryForObject("select current_database()", String.class);
            return ResponseEntity.ok(Map.of(
                    "status", "CONNECTED",
                    "database", database == null ? "unknown" : database
            ));
        } catch (Exception exception) {
            // Do not expose credentials, SQL, or internal exception details to clients.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DISCONNECTED",
                            "message", "Database connection failed. Check server configuration and logs."));
        }
    }
}
