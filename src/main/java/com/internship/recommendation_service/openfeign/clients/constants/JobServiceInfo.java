package com.internship.recommendation_service.openfeign.clients.constants;

public class JobServiceInfo {

    public static final String NAME = "job-service";
    public static final String URL = "${microserviceUrls.job-service}";

    public static final String JOB_PATH = "/v1/jobs/";

    private JobServiceInfo() {
    }
}
