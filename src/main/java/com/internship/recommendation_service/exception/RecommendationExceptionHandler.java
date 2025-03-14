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


    private static ResponseEntity<ExceptionResponse> handleUserDefinedException(Exception ex, HttpStatus httpStatus) {
        String errorMessage = ex.getMessage();
        ExceptionResponse errorResponse = ExceptionResponse.builder()
                .statusCode(httpStatus.value())
                .messages(List.of(errorMessage))
                .success(false)
                .build();
        return ResponseEntity.status(httpStatus.value()).body(errorResponse);
    }

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


    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleReservationNotFoundException(ReservationNotFoundException ex) {
        log.error("ReservationsNotFoundException occurred: {}", ex.getMessage());
        return handleUserDefinedException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleJobNotFoundException(JobNotFoundException ex) {
        log.error("JobNotFoundException occurred: {}", ex.getMessage());
        return handleUserDefinedException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleReviewNotFoundException(ReviewNotFoundException ex) {
        log.error("ReviewNotFoundException occurred: {}", ex.getMessage());
        return handleUserDefinedException(ex, HttpStatus.NOT_FOUND);
    }

}
