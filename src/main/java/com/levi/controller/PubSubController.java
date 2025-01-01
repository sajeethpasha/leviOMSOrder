package com.levi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.levi.service.PubSubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/pubsub")
public class PubSubController {


    private final PubSubService pubSubService;

    private static final Logger logger = LoggerFactory.getLogger(PubSubController.class);

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.pubsub.topic.target}")
    private String targetTopicId;

    private Publisher publisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PubSubController(PubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }

    @PostConstruct
    public void initPublisher() throws IOException {
        logger.info("Initializing Pub/Sub Publisher for project: {} and topic: {}", projectId, targetTopicId);
        ProjectTopicName targetTopicName = ProjectTopicName.of(projectId, targetTopicId);
        publisher = Publisher.newBuilder(targetTopicName).build();
        logger.info("Publisher initialized successfully.");
    }

    @PreDestroy
    public void shutdownPublisher() {
        if (publisher != null) {
            logger.info("Shutting down Pub/Sub Publisher...");
            publisher.shutdown();
            try {
                if (publisher.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.info("Publisher shut down successfully.");
                } else {
                    logger.warn("Publisher shutdown timed out.");
                }
            } catch (InterruptedException e) {
                logger.error("Publisher shutdown interrupted.", e);
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        } else {
            logger.warn("Publisher was already null; no action taken.");
        }
    }

    @PostMapping("/push")
    public ResponseEntity<String> receivePushMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Received push message payload: {}", payload);
        try {
            // Parse the Pub/Sub message
            PubSubPushMessage message = objectMapper.convertValue(payload, PubSubPushMessage.class);
            PubsubMessage pubsubMessage = message.getMessage();

            String data = pubsubMessage.getData().toStringUtf8();
            logger.info("Parsed message data: {}", data);

            // Publish to target topic
            PubsubMessage targetMessage = PubsubMessage.newBuilder()
                    .setData(pubsubMessage.getData())
                    .putAllAttributes(pubsubMessage.getAttributesMap())
                    .build();

            logger.info("Publishing message to target topic: {}", targetTopicId);
            publisher.publish(targetMessage).get();
            logger.info("Message published to target topic successfully.");

            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error processing push message: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Inner class to map the Pub/Sub push message
    public static class PubSubPushMessage {
        private PubsubMessage message;
        private String subscription;

        // Getters and setters

        public PubsubMessage getMessage() {
            return message;
        }

        public void setMessage(PubsubMessage message) {
            this.message = message;
        }

        public String getSubscription() {
            return subscription;
        }

        public void setSubscription(String subscription) {
            this.subscription = subscription;
        }
    }


//    @GetMapping("/start-pull-subscription")
//    public String startPullSubscription() {
//        pubSubService.subscribeToPullSubscription();
//        return "Pull Subscription started: Topic_MAO-sub_1";
//    }
//
//    @GetMapping("/start-target-subscription")
//    public String startTargetSubscription() {
//        pubSubService.subscribeToTargetSubscription();
//        return "Target Subscription started: Topic_MAO_Target-sub";
//    }

}
