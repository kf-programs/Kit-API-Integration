package com.example.kitApp.model;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.stream.Collectors;

public class KitApiSubscribersResponse {
    private List<Subscriber> subscribers;
    private Pagination pagination;

    
    public KitApiSubscribersResponse(List<String> emailAddresses, String endCursor) {
        this.subscribers = emailAddresses.stream()
            .map(email -> {
                Subscriber subscriber = new Subscriber();
                subscriber.emailAddress = email;
                return subscriber;
            })
            .collect(Collectors.toList());
            
        Pagination pagination = new Pagination();
        pagination.endCursor = endCursor;
        this.pagination = pagination;
    }

    // Add default constructor to maintain existing functionality
    public KitApiSubscribersResponse() {}

    public static class Subscriber {
        @JsonProperty("email_address")
        private String emailAddress;
        
        public String getEmailAddress() {
            return emailAddress;
        }
    }

    public static class Pagination {
        // Kit API returns an "end_cursor" for pagination to fetch the next set of results
        // currently, end_cursor is always supplied, even if there are no more results
        @JsonProperty("end_cursor")
        private String endCursor;
        
        public String getEndCursor() {
            return endCursor;
        }
    }

    public List<String> getSubscriberEmailAddresses() {
        if (subscribers == null || subscribers.isEmpty()) {
            return Collections.emptyList();
        }
        
        return subscribers.stream()
            .map(Subscriber::getEmailAddress)
            .collect(Collectors.toList());
    }

    public String getEndCursor() {
        return pagination.getEndCursor();
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setSubscribers(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }
    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

}
