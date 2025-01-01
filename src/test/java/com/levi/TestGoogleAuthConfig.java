package com.levi;

import com.google.auth.oauth2.GoogleCredentials;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestGoogleAuthConfig {
    @Bean
    public GoogleCredentials googleCredentials() {
        return Mockito.mock(GoogleCredentials.class);
    }
}
