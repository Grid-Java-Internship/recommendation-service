package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "web.client.geolocation-service")
public class GeolocationServiceConfig {
    private String baseUrl;
    private String apiSearch;
}
