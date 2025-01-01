package com.levi.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
//import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class PubSubService {

    private static final Logger logger = LoggerFactory.getLogger(PubSubService.class);

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.pubsub.subscription.pull}")
    private String pullSubscriptionName;

    @Value("${spring.cloud.gcp.pubsub.topic.target}")
    private String targetTopicName;

    private Subscriber pullSubscriber;
    private Publisher publisher;
    private final HttpServletRequest request;

    public PubSubService(HttpServletRequest request) {
        this.request = request;
    }

    @PostConstruct
    public void startPullSubscriber() {
        try {
            logger.info("Initializing Pub/Sub Publisher for project: {} and topic: {}", projectId, targetTopicName);

            // Create GoogleCredentials from the Authorization header
            GoogleCredentials credentials = getGoogleCredentialsFromHeader();

            // Initialize Publisher for target topic
            ProjectTopicName topicName = ProjectTopicName.of(projectId, targetTopicName);
            publisher = Publisher.newBuilder(topicName).setCredentialsProvider(() -> credentials).build();
            logger.info("Publisher initialized successfully for topic: {}", targetTopicName);

            // Initialize Pull Subscriber
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, pullSubscriptionName);
            MessageReceiver receiver = this::handleMessage;

            pullSubscriber = Subscriber.newBuilder(subscriptionName, receiver)
                    .setCredentialsProvider(() -> credentials)
                    .build();
            pullSubscriber.startAsync().awaitRunning();
            logger.info("Pull Subscriber started for subscription: {}", pullSubscriptionName);

        } catch (Exception e) {
            logger.error("Failed to initialize Pub/Sub service: {}", e.getMessage(), e);
        }
    }

    private GoogleCredentials getGoogleCredentialsFromHeader() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Authorization header is missing or invalid");
            throw new IllegalStateException("Authorization header is missing or invalid");
        }
        String token = authHeader.substring(7);
        AccessToken accessToken = new AccessToken(token, null);
        logger.info("Authorization header processed successfully.");
        return GoogleCredentials.create(accessToken)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
    }

    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        String data = message.getData().toStringUtf8();
        logger.info("Received message (Pull): {}", data);

        // Publish to target topic
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(message.getData())
                .putAllAttributes(message.getAttributesMap())
                .build();

        try {
            logger.info("Publishing message to target topic: {}", targetTopicName);
            publisher.publish(pubsubMessage).get();
            logger.info("Message published to target topic successfully.");
            consumer.ack();
        } catch (Exception e) {
            logger.error("Failed to publish message: {}", e.getMessage(), e);
            consumer.nack();
        }
    }

    @PreDestroy
    public void stopPullSubscriber() {
        if (pullSubscriber != null) {
            logger.info("Stopping Pull Subscriber...");
            try {
                pullSubscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
                logger.info("Pull Subscriber stopped successfully.");
            } catch (Exception e) {
                logger.error("Failed to stop Pull Subscriber: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Pull Subscriber is already null.");
        }
        if (publisher != null) {
            try {
                publisher.shutdown();
                if (publisher.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.info("Publisher shut down successfully.");
                } else {
                    logger.warn("Publisher did not terminate within the timeout.");
                }
            } catch (Exception e) {
                logger.error("Failed to shut down Publisher: {}", e.getMessage(), e);
            }
        }
    }
}
