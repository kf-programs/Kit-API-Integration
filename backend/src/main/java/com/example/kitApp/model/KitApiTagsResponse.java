package com.example.kitApp.model;

import java.util.List;

public class KitApiTagsResponse {
    private List<Tag> tags;

    public static class Tag {
        private String id;
        private String name;
        
        public String getId() { return id; }
        public String getName() { return name; }
    }

    public List<Tag> getTags() { return tags; }
}
