package com.levi.controller;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
//import com.levi.model.PubSubPublishRequest;
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
        Publisher publisher = null;

        try {
            // Validate input
            if (request.getTopic() == null || request.getTopic().isEmpty()) {
                return ResponseEntity.badRequest().body("Topic must not be empty");
            }
            if (request.getMessage() == null || request.getMessage().isEmpty()) {
                return ResponseEntity.badRequest().body("Message must not be empty");
            }

            // Build the topic name dynamically
            ProjectTopicName topicName = ProjectTopicName.of(projectId, request.getTopic());

            // Create the publisher
            publisher = Publisher.newBuilder(topicName).build();

            // Create the Pub/Sub message
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(request.getMessage()))
                    .build();

            // Publish the message
            publisher.publish(pubsubMessage).get();
            logger.info("Message published to topic: {}", request.getTopic());

            return ResponseEntity.ok("Message published to topic: " + request.getTopic());
        } catch (Exception e) {
            logger.error("Failed to publish message to topic: {}", request.getTopic(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to publish message: " + e.getMessage());
        } finally {
            // Clean up the publisher
            if (publisher != null) {
                try {
                    publisher.shutdown();
                    publisher.awaitTermination(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.error("Error shutting down publisher for topic: {}", request.getTopic(), e);
                }
            }
        }
    }
}
