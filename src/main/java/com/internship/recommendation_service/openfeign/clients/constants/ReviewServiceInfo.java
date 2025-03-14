package com.internship.recommendation_service.openfeign.clients.constants;

public class ReviewServiceInfo {

    public static final String NAME = "review-service";
    public static final String URL = "${microserviceUrls.review-service}";
    public static final String RESERVATION_PATH = "/v1/review/";
}
