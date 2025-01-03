package com.levi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HealthCheckController provides an endpoint for health checks.
 */
@RestController
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Endpoint to check the health of the application.
     * @return HTTP 200 OK if the application is running.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String timestamp = LocalDateTime.now().format(formatter);
        logger.info("Health check endpoint accessed at {}", timestamp);

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/")
    public String baseCheck() {
        return "Application is running!";
    }
}
