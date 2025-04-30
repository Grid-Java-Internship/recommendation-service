package com.internship.recommendation_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recommendation.defaults")
public class RecommendationDefaultsConfig {
    private int limit;
    private double maxDistance;
    private int minExperience;
}
