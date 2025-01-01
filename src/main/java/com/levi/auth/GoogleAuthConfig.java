//package com.levi.auth;
//
//import com.google.auth.oauth2.AccessToken;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.levi.controller.PubSubController;
//import jakarta.servlet.http.HttpServletRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Collections;
//import java.util.Date;
//
//
//@Configuration
//public class GoogleAuthConfig {
//
//    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthConfig.class);
//    @Bean
//    public GoogleCredentials googleCredentials(HttpServletRequest request) {
//        logger.info("Attempting to create GoogleCredentials...");
//
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            logger.error("Authorization header is missing or invalid");
//            throw new IllegalStateException("Authorization header is missing or invalid");
//        }
//
//        // Extract the token from the header
//        String token = authHeader.substring(7);
//        logger.debug("Extracted token: {}", token);
//
//        // Create GoogleCredentials from the token
//        try {
//            AccessToken accessToken = new AccessToken(token, new Date(System.currentTimeMillis() + 3600 * 1000));
//            logger.info("Access token created successfully");
//
//            GoogleCredentials credentials = GoogleCredentials.create(accessToken)
//                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
//            logger.info("GoogleCredentials created and scoped successfully");
//
//            return credentials;
//        } catch (Exception e) {
//            logger.error("Failed to create GoogleCredentials: {}", e.getMessage(), e);
//            throw new IllegalStateException("Error creating GoogleCredentials", e);
//        }
//    }
//}
