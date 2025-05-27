package com.example.demo;

import java.util.List;

/**
 * The request sent from the frontend to the backend
 * to tag subscribers with a specific tag.
 */
public class TagSubscribersRequest {
    private List<String> emails;
    private String tagId;

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
}
