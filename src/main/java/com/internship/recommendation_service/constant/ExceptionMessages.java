package com.internship.recommendation_service.constant;

public class ExceptionMessages {
    private ExceptionMessages() {
    }

    /* Generic exception messages */
    public static final String RESOURCE_NOT_FOUND = "Requested resource was not found.";
    public static final String INTERNAL_SERVER_ERROR = "Request failed due to an internal server error. " +
                                                       "Please contact support.";
    public static final String SERVICE_UNAVAILABLE = "The service is temporarily unavailable. " +
                                                     "Please try again later.";
}
