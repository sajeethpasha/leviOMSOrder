package com.levi.controller;

import com.levi.service.PubSubPullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pull")
public class PullPubSubController {

    private static final Logger logger = LoggerFactory.getLogger(PullPubSubController.class);

    private final PubSubPullService pullService;

    public PullPubSubController(PubSubPullService pullService) {
        this.pullService = pullService;
    }

    @GetMapping("/messages")
    public String pullMessages(@RequestParam String subscriptionName) {
        logger.info("Received request to pull messages for subscription: {}", subscriptionName);
        try {
            pullService.pullMessages(subscriptionName);
            logger.info("Successfully pulled messages for subscription: {}", subscriptionName);
            return "Messages pulled for subscription: " + subscriptionName;
        } catch (Exception e) {
            logger.error("Failed to pull messages for subscription: {}", subscriptionName, e);
            return "Error occurred while pulling messages for subscription: " + subscriptionName;
        }
    }
}
