package com.example.demo;

import java.util.List;

public class TagsResponse {
    private List<Tag> tags;

    public static class Tag {
        private String id;
        private String name;
        
        public String getId() { return id; }
        public String getName() { return name; }
    }

    public List<Tag> getTags() { return tags; }
}
