package com.levi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/push")
public class PushPubSubController {

    private static final Logger logger = LoggerFactory.getLogger(PushPubSubController.class);

    @PostMapping("/receive")
    public String receiveMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Received a push message payload: {}", payload);

        try {
            if (payload.containsKey("message")) {
                logger.info("Processing 'message' key in payload.");
                Map<String, Object> message = (Map<String, Object>) payload.get("message");

                if (message != null && message.containsKey("data")) {
                    String data = (String) message.get("data");
                    logger.info("Decoded message data: {}", data);
                } else {
                    logger.warn("'message' key does not contain 'data'.");
                }
            } else {
                logger.warn("Payload does not contain 'message' key.");
            }
        } catch (Exception e) {
            logger.error("Error while processing the push message payload.", e);
        }

        logger.info("Message processing completed successfully.");
        return "Message received successfully";
    }
}
