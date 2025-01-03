package com.levi.auth;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

@Configuration
public class GoogleAuthConfig {

    private static final Logger LOGGER = Logger.getLogger(GoogleAuthConfig.class.getName());

    @Bean
    public GoogleCredentials googleCredentials(HttpServletRequest request) {
        LOGGER.info("Starting to process the Authorization header for Google Credentials.");

        // Extract the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.severe("Authorization header is missing or invalid. Throwing an exception.");
            throw new IllegalStateException("Authorization header is missing or invalid");
        }

        LOGGER.info("Authorization header successfully extracted.");

        // Extract the token
        String token = authHeader.substring(7);
        LOGGER.info("Token extracted successfully. Token: " + token);

        // Create AccessToken
        AccessToken accessToken = new AccessToken(token, new Date(System.currentTimeMillis() + 3600 * 1000));
        LOGGER.info("Access token created successfully with expiration time: " + accessToken.getExpirationTime());

        // Return GoogleCredentials
        GoogleCredentials credentials = GoogleCredentials.create(accessToken)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
        LOGGER.info("GoogleCredentials object created successfully.");

        return credentials;
    }
}
