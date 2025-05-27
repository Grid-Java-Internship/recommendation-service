package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "service.names")
public class ServiceNamesConfig {
    private String authService;
    private String userService;
    private String jobService;
    private String reservationService;
    private String reviewService;
    private String reportService;
    private String chatService;
    private String notificationService;
    private String recommendationService;
    private String paymentService;
}
