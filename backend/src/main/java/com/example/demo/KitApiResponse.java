package com.example.demo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KitApiResponse {
    private List<Subscriber> subscribers;
    private Pagination pagination;

    public static class Subscriber {
        @JsonProperty("email_address")
        private String emailAddress;
        
        public String getEmailAddress() {
            return emailAddress;
        }
    }

    public static class Pagination {
        @JsonProperty("end_cursor")
        private String endCursor;
        
        public String getEndCursor() {
            return endCursor;
        }
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
