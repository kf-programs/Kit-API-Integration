package com.example.demo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KitApiSubscribersResponse {
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
        // Kit API returns an "end_cursor" for pagination to fetch the next set of results
        // currently, end_cursor is always supplied, even if there are no more results
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
