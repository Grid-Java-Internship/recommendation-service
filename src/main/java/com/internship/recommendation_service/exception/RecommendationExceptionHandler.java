package com.internship.recommendation_service.exception;

import com.internship.recommendation_service.dto.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@Slf4j
@ControllerAdvice
public class RecommendationExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
        String errorMessage = "Request failed because of an internal problem. " +
                "Please contact support or your administrator. Error: " + ex.getMessage();
        log.error("Internal server error occurred: {}", errorMessage);

        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .messages(List.of(errorMessage))
                .success(false)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
