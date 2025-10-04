package com.example.kitApp.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.kitApp.model.KitApiSubscribersResponse;
import com.example.kitApp.model.KitApiTagsResponse;
import com.example.kitApp.model.TagSubscribersRequest;
import com.example.kitApp.service.KitApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * KitApiController handles API requests related to Kit subscribers and tags.
 */
@RestController
@RequestMapping("/api")
public class KitApiController {
    private static final Logger logger = LoggerFactory.getLogger(KitApiController.class);
    
    private final KitApiService kitApiService;

    public KitApiController(KitApiService kitApiService) {
        this.kitApiService = kitApiService;
    }

    /**
     * Gets all the subscribers. Will make multiple calls to the Kit API
     * if there are more than 500 subscribers, using pagination with end cursors.
     */
    @PostMapping("/subscribers")
    public ResponseEntity<?> getSubscribers() {
        logger.info("Fetching subscribers from Kit API");
        
        try {
            KitApiSubscribersResponse kitResponse = kitApiService.fetchSubscribers(null);
            logger.info("Response: {}", kitResponse);
            if (kitResponse == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No response from Kit API.");
            }
            List<String> emails = kitResponse.getSubscriberEmailAddresses();

            if (emails == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subscribers found.");
            }
            logger.info("Successfully retrieved subscribers from Kit API");

            // Extract emails and end cursor
            String endCursor = kitResponse.getEndCursor();
            
            // if an end cursor is present, continue fetching more subscribers (at current implementation,
            // end_cursor is always present, even if it hit the final subscriber, but check null/empty to be safe)
            int safetyCount = 8; // Safety count to prevent infinite loops
            while (endCursor != null && !endCursor.isEmpty() && safetyCount > 0) {
                safetyCount--;
                logger.info("End cursor found: {}", endCursor);
                Map<String, Object> result = new HashMap<>();
                result = getNextPageSubscribers(endCursor);
                if (result == null || !result.containsKey("emails") || !result.containsKey("endCursor")) {
                    logger.warn("No more subscribers found or invalid response structure.");
                    break;
                }
                
                logger.info("Safety {} Result {}", safetyCount, result);
                emails.addAll((List<String>) result.get("emails"));
                endCursor = (String) result.get("endCursor");
            }
            if (safetyCount == 0) {
                logger.warn("Safety count reached zero, stopping pagination to prevent infinite loop.");
            }
            logger.info("Total subscribers fetched: {}", emails.size());
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Uses the end cursor to fetch the next page of subscribers.
     * This method is called recursively until there are no more pages to fetch or safety limit is reached.
     * 
     * returns a map containing the list of emails and the new end cursor, or null if no more subscribers are found.
     */
    private Map<String, Object> getNextPageSubscribers(String endCursor) {
        logger.info("Fetching next page of subscribers with end cursor: {}", endCursor);

        KitApiSubscribersResponse kitResponse = kitApiService.fetchSubscribers(endCursor);
        if (kitResponse == null || kitResponse.getSubscribers() == null || kitResponse.getSubscribers().isEmpty()) {
            return null;
        }
        logger.info("Successfully retrieved subscribers from Kit API");

        // Extract emails and end cursor
        List<String> emails = kitResponse.getSubscribers().stream()
        .map(sub -> sub.getEmailAddress())
        .collect(Collectors.toList());
        String newEndCursor = kitResponse.getPagination().getEndCursor();

        Map<String, Object> result = new HashMap<>();
        result.put("emails", emails);
        result.put("endCursor", newEndCursor);
        
        return result;
    }

    /**
     * Fetches all available tags for the account linked to the Kit API key.
     */
    @GetMapping("/tags")
    public ResponseEntity<?> getTags() {
        logger.info("Fetching available tags");
        try {
            KitApiTagsResponse response = kitApiService.fetchTags();
            return ResponseEntity.ok(response.getTags());
        } catch (Exception e) {
            logger.error("Error fetching tags: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Tags subscribers with a specific tag ID.
     * Accepts a list of email addresses and a tag ID, and attempts to tag each subscriber.
     * Makes a separate API call for each email address cause Kit's bulk tagging requires subscriber IDs,
     * which are not provided in the initial subscriber fetch.
     */
    @PostMapping("/tag-subscribers")
    public ResponseEntity<?> tagSubscribers(@RequestBody TagSubscribersRequest request) {

        logger.info("Tagging {} emails with tag: {}", 
            request.getEmails().size(), request.getTagId());

        // Initialize results map to track success, already tagged, and failed counts
        Map<String, Integer> results = new HashMap<>();
        results.put("success", 0);
        results.put("alreadyTagged", 0);
        results.put("failed", 0);

        // Initialize a list to hold details of each email processed. Each detail will include the email address,
        // http status, and result, which is either the response body on success, or error message.
        List<Map<String, Object>> emailDetails = new ArrayList<>();

        // Iterate over each email and attempt to tag it, calling the Kit API for each
        for (String email : request.getEmails()) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("email", email);
            
            try {
                ResponseEntity<String> response = kitApiService.tagSubscriber(request.getTagId(), email);
                detail.put("status", response.getStatusCode().value());
                detail.put("result", response.getBody());
                
                if (response.getStatusCode() == HttpStatus.CREATED) {
                    results.put("success", results.get("success") + 1);
                } else if (response.getStatusCode() == HttpStatus.OK) {
                    results.put("alreadyTagged", results.get("alreadyTagged") + 1);
                }
            } catch (Exception e) {
                logger.error("Error tagging subscriber {}: {}", email, e.getMessage());
                detail.put("status", "ERROR");
                detail.put("result", e.getMessage());
                
                results.put("failed", results.get("failed") + 1);
            }
            
            emailDetails.add(detail);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format(
            "Processing complete. Successfully tagged: %d, Already tagged: %d, Failed: %d",
            results.get("success"),
            results.get("alreadyTagged"),
            results.get("failed")
        ));
        response.put("details", results);
        response.put("emailDetails", emailDetails);

        boolean hasErrors = results.get("failed") > 0;
        return ResponseEntity
            .status(hasErrors ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
            .body(response);
    }
}