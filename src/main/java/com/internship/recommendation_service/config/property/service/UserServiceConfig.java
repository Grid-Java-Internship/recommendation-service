package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "web.client.user-service")
public class UserServiceConfig {
    private String baseUrlUsers;
    private String baseUrlFavorites;
    private String baseUrlBlocks;
    private String baseUrlPreferences;
}
