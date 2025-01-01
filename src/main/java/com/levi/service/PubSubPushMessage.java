package com.levi.service;

import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubSubPushMessage {

    private static final Logger logger = LoggerFactory.getLogger(PubSubPushMessage.class);

    private PubsubMessage message;
    private String subscription;

    // Getters and setters
    public PubsubMessage getMessage() {
        return message;
    }

    public void setMessage(PubsubMessage message) {
        if (message == null) {
            logger.warn("Attempting to set a null PubsubMessage.");
        }
        this.message = message;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        if (subscription == null || subscription.isEmpty()) {
            logger.warn("Setting an empty or null subscription.");
        }
        this.subscription = subscription;
    }

    // Method to validate the message
    public boolean isValid() {
        if (message == null) {
            logger.error("Invalid PubSubPushMessage: 'message' field is null.");
            return false;
        }
        if (subscription == null || subscription.isEmpty()) {
            logger.error("Invalid PubSubPushMessage: 'subscription' field is null or empty.");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PubSubPushMessage{" +
                "message=" + (message != null ? message.getData().toStringUtf8() : "null") +
                ", subscription='" + subscription + '\'' +
                '}';
    }
}
