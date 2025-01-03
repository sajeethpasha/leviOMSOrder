package com.levi.service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PubSubPullService {

    private static final Logger logger = LoggerFactory.getLogger(PubSubPullService.class);

    private final PubSubTemplate pubSubTemplate;

    public PubSubPullService(PubSubTemplate pubSubTemplate) {
        this.pubSubTemplate = pubSubTemplate;
    }

    public void pullMessages(String subscriptionName) {
        logger.info("Attempting to pull messages from subscription: {}", subscriptionName);

        try {
            List<AcknowledgeablePubsubMessage> messages = pubSubTemplate.pull(subscriptionName, 10, false);
            logger.info("Pulled {} messages from subscription: {}", messages.size(), subscriptionName);

            if (!messages.isEmpty()) {
                for (AcknowledgeablePubsubMessage message : messages) {
                    String payload = message.getPubsubMessage().getData().toStringUtf8();
                    logger.info("Received message with payload: {}", payload);

                    try {
                        message.ack(); // Acknowledge the message
                        logger.info("Acknowledged message: {}", message.getPubsubMessage().getMessageId());
                    } catch (Exception e) {
                        logger.error("Failed to acknowledge message: {}", message.getPubsubMessage().getMessageId(), e);
                    }
                }
            } else {
                logger.info("No messages available in subscription: {}", subscriptionName);
            }
        } catch (Exception e) {
            logger.error("Error occurred while pulling messages from subscription: {}", subscriptionName, e);
        }
    }
}
