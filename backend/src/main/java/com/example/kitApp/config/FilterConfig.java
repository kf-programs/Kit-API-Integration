package com.example.kitApp.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.kitApp.filter.KitApiKeyFilter;

@Configuration
public class FilterConfig {
    
    /**
     * Register the KitApiKeyFilter to apply to all requests under /api/*
     * @return
     */
    @Bean
    public FilterRegistrationBean<KitApiKeyFilter> kitApiKeyFilter() {
        FilterRegistrationBean<KitApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new KitApiKeyFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
