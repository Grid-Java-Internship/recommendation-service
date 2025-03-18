package com.internship.recommendation_service.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class NotFoundException extends RuntimeException{
    private final String message;
}
