package com.example.kitApp.context;

public class KitApiKeyContext {

    // ThreadLocal ensures that each thread (i.e., each request) 
    // gets its own isolated copy of the apiKey.
    private static final ThreadLocal<String> apiKeyHolder = new ThreadLocal<>();

    // Set the API key at the beginning of the request
    public static void setApiKey(String apiKey) {
        apiKeyHolder.set(apiKey);
    }

    // Get the API key when needed by the service
    public static String getApiKey() {
        return apiKeyHolder.get();
    }

    // IMPORTANT: Clear the ThreadLocal at the end of the request
    public static void clear() {
        apiKeyHolder.remove();
    }
}
