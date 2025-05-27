package com.internship.recommendation_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    /**
     * Create a load-balanced WebClient builder.
     * Any WebClient built from this builder will be able to resolve
     * service names using the configured discovery client (Eureka).
     *
     * @return a load-balanced web client builder
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Create a default WebClient using the load-balanced builder.
     * This WebClient instance can now make requests using service names.
     *
     * @param builder The load-balanced WebClient.Builder
     * @return a web client
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
