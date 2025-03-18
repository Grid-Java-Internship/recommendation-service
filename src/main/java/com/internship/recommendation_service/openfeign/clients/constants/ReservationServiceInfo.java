package com.internship.recommendation_service.openfeign.clients.constants;

public class ReservationServiceInfo {

    public static final String NAME = "reservation-service";
    public static final String URL = "${microserviceUrls.reservation-service}";
    public static final String RESERVATION_PATH = "/v1/reservation/";

    private ReservationServiceInfo() {
    }
}
