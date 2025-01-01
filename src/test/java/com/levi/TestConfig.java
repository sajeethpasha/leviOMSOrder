package com.levi;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public PubSubTemplate pubSubTemplate() {
        return Mockito.mock(PubSubTemplate.class);
    }
}
