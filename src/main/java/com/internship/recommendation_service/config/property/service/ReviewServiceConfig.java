package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "web.client.review-service")
public class ReviewServiceConfig {
    private String baseUrl;
    private String apiUserRating;
    private String apiJobRating;
}