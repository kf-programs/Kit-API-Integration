package com.example.kitApp.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.kitApp.context.KitApiKeyContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Filter to extract the Kit API key from incoming HTTP requests and store it in a thread-local context.
 * This ensures that each request is handled with the correct API key without interference from other requests.
 */
public class KitApiKeyFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(KitApiKeyFilter.class);

    private static final String API_KEY_HEADER = "Kit-Api-Key";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        logger.info("KitApiKeyFilter: Processing request to extract API key");
        // 1. Cast to HttpServletRequest to access headers
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 2. Extract the API key from the header
        String apiKey = httpRequest.getHeader(API_KEY_HEADER);

        try {
            // 3. Set the key in the thread-local context
            if (apiKey != null && !apiKey.isEmpty()) {
                KitApiKeyContext.setApiKey(apiKey);
            }

            // 4. Continue the processing chain (Controller will be next)
            chain.doFilter(request, response);
            
        } finally {
            // 5. IMPORTANT: Clear the ThreadLocal to prevent memory leaks 
            //    or contamination of subsequent requests using the same thread.
            KitApiKeyContext.clear();
        }
    }
}