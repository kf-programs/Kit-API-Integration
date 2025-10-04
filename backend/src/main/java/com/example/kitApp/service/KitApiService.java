package com.example.kitApp.service;

import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.example.kitApp.context.KitApiKeyContext;
import com.example.kitApp.model.KitApiSubscribersResponse;
import com.example.kitApp.model.KitApiTagsResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class KitApiService {
    private static final String KIT_API_BASE_URL = "https://api.kit.com/v4";
    
    private final RestTemplate restTemplate;

    public KitApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public KitApiSubscribersResponse fetchSubscribers(String endCursor) {
        String url = KIT_API_BASE_URL + "/subscribers" + (endCursor != null ? "?after=" + endCursor : "");
        return invokeKitAPIGet(url, KitApiSubscribersResponse.class);
    }

    public KitApiTagsResponse fetchTags() {
        String url = KIT_API_BASE_URL + "/tags";
        return invokeKitAPIGet(url, KitApiTagsResponse.class);
    }

    public ResponseEntity<String> tagSubscriber(String tagId, String email) {
        String url = KIT_API_BASE_URL + "/tags/" + tagId + "/subscribers";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email_address", email);

        return invokeKitAPIPost(url, requestBody, String.class);
    }

    private <T> T invokeKitAPIGet(String url, Class<T> responseType) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        ResponseEntity<T> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            responseType
        );
        return response.getBody();
    }

    private <T> ResponseEntity<T> invokeKitAPIPost(String url, Map<String, String> requestBody, Class<T> responseType) {
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, createHeaders());
        ResponseEntity<T> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            responseType
        );
        return response;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Kit-Api-Key", obtainKitApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String obtainKitApiKey() {
        String apiKey = KitApiKeyContext.getApiKey(); 
        
        if (apiKey == null) {
            // Handle error or missing key
            throw new IllegalStateException("API Key not available for this request.");
        }

        return apiKey;
    }
}
