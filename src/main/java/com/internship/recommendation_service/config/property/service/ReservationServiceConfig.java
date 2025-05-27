package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "web.client.reservation-service")
public class ReservationServiceConfig {
    private String baseUrl;
    private String apiJobReservationCount;
}
