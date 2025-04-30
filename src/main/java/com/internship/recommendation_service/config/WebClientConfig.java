package com.internship.recommendation_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    /**
     * Create a default web client that can be used for making HTTP requests
     * to other services.
     *
     * @return a web client
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
