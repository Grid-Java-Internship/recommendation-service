package com.internship.recommendation_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recommendation.weights")
public class RecommendationWeightsConfig {
    private double distance;
    private double experienceMatch;
    private double categoryMatch;
    private double favorite;
    private double workerRating;
    private double jobRating;
    private double hourlyRate;
    private double userReportsLow;
    private double userReportsMedium;
    private double userReportsHigh;
    private double jobReportsLow;
    private double jobReportsMedium;
    private double jobReportsHigh;
    private double jobReservationsCount;
}
