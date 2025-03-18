package com.internship.recommendation_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class JobNotFoundException extends RuntimeException{
    private final String message;
    public static final String JOB_WITH_ID_NOT_FOUND = "Job not found.";
    public static final String JOBS_NOT_FOUND = "Jobs not found.";
}
