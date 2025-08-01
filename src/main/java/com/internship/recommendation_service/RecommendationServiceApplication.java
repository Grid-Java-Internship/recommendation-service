package com.internship.recommendation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.internship"}, exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"com.internship"})
public class RecommendationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
