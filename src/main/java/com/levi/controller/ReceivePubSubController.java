package com.levi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/push")
public class ReceivePubSubController {

    private static final Logger logger = LoggerFactory.getLogger(ReceivePubSubController.class);

    /**
     * Endpoint to receive push subscription messages from Pub/Sub.
     *
     * @param payload The Pub/Sub message payload.
     * @return A response indicating success or failure.
     */
    @PostMapping("/receive")
    public String receiveMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Received push message payload: {}", payload);

        try {
            if (payload.containsKey("message")) {
                logger.info("Processing 'message' key in payload.");
                Map<String, Object> message = (Map<String, Object>) payload.get("message");

                if (message != null) {
                    if (message.containsKey("data")) {
                        String data = (String) message.get("data");
                        String decodedData = new String(Base64.getDecoder().decode(data));
                        logger.info("Decoded message data: {}", decodedData);
                    } else {
                        logger.warn("'message' key found but does not contain 'data'.");
                    }

                    if (message.containsKey("messageId")) {
                        String messageId = (String) message.get("messageId");
                        logger.info("Message ID: {}", messageId);
                    }

                    if (message.containsKey("attributes")) {
                        Map<String, String> attributes = (Map<String, String>) message.get("attributes");
                        logger.info("Message attributes: {}", attributes);
                    }
                } else {
                    logger.warn("The 'message' key exists but its value is null.");
                }
            } else {
                logger.warn("Payload does not contain 'message' key.");
            }
        } catch (Exception e) {
            logger.error("Error while processing the push message payload.", e);
        }

        logger.info("Push message processing completed.");
        return "Message received successfully";
    }
}
