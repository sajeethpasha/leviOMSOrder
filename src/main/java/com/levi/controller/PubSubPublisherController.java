package com.levi.controller;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.levi.models.PubSubPublishRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/publisher")
public class PubSubPublisherController {

    private static final Logger logger = LoggerFactory.getLogger(PubSubPublisherController.class);

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    /**
     * Endpoint to publish a message to a specified Pub/Sub topic.
     *
     * @param request The request object containing the topic and message.
     * @return A response indicating success or failure.
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestBody PubSubPublishRequest request) {
        logger.info("Starting publishMessage method.");
        Publisher publisher = null;

        logger.info("Received request to publish message. Request: {}", request.toString());

        try {
            // Validate input
            if (request.getTopic() == null || request.getTopic().isEmpty()) {
                logger.warn("Validation failed: Topic must not be empty");
                return ResponseEntity.badRequest().body("Topic must not be empty");
            }
            logger.debug("Topic validation passed.");

            if (request.getMessage() == null || request.getMessage().isEmpty()) {
                logger.warn("Validation failed: Message must not be empty");
                return ResponseEntity.badRequest().body("Message must not be empty");
            }
            logger.debug("Message validation passed.");

            logger.debug("Input validation passed. Topic: {}, Message: {}", request.getTopic(), request.getMessage());

            // Build the topic name dynamically
            logger.debug("Constructing topic name using projectId: {} and topic: {}", projectId, request.getTopic());
            ProjectTopicName topicName = ProjectTopicName.of(projectId, request.getTopic());
            logger.debug("Constructed topic name: {}", topicName);

            // Create the publisher
            logger.debug("Creating Publisher for topic: {}", topicName);
            publisher = Publisher.newBuilder(topicName).build();
            logger.debug("Publisher created successfully for topic: {}", topicName);

            // Create the Pub/Sub message
            logger.debug("Building Pub/Sub message with data: {}", request.getMessage());
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(request.getMessage()))
                    .build();
            logger.debug("Pub/Sub message created: {}", pubsubMessage);

            // Publish the message
            logger.debug("Publishing message to topic: {}", request.getTopic());
            publisher.publish(pubsubMessage).get();
            logger.info("Message successfully published to topic: {}", request.getTopic());

            return ResponseEntity.ok("Message published to topic: " + request.getTopic());
        } catch (Exception e) {
            logger.error("Exception encountered while publishing message to topic: {}", request.getTopic(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to publish message: " + e.getMessage());

        } finally {
            // Clean up the publisher
            if (publisher != null) {
                try {
                    logger.debug("Shutting down publisher for topic: {}", request.getTopic());
                    publisher.shutdown();
                    publisher.awaitTermination(10, TimeUnit.SECONDS);
                    logger.debug("Publisher shut down successfully for topic: {}", request.getTopic());
                } catch (Exception e) {
                    logger.error("Error shutting down publisher for topic: {}", request.getTopic(), e);
                }
            }
            logger.info("Finished publishMessage method.");
        }
    }
}
