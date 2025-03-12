package com.internship.recommendation_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ReservationsNotFoundException extends RuntimeException{
    private final String message;
    public static final String MESSAGE = "There are no reservations";
}
