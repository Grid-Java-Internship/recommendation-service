package com.internship.recommendation_service.config.property.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "web.client.report-service")
public class ReportServiceConfig {
    private String baseUrl;
    private String apiUserReportInfo;
    private String apiJobReportInfo;
}
