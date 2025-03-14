package com.internship.recommendation_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ReviewNotFoundException extends RuntimeException{
    private final String message;
    public static final String REVIEWS_NOT_FOUND = "Reviews not found.";
}
