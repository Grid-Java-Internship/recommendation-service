package com.internship.recommendation_service.exception;

import com.internship.recommendation_service.constant.ExceptionMessages;
import com.internship.recommendation_service.dto.response.ExceptionResponse;
import com.internship.recommendation_service.util.LogUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class DefaultExceptionHandler {
    /**
     * Handles exceptions of type {@link MethodArgumentNotValidException} that occur
     * when Spring validation fails for a method argument.
     *
     * @param ex      the {@link MethodArgumentNotValidException} thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} containing an {@link ExceptionResponse} with
     * a status of {@code HttpStatus.BAD_REQUEST} and a message indicating
     * that the request validation failed
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errorMessages = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        LogUtil.error("Validation failed for request [{}]: {}",
                request.getDescription(false), errorMessages);

        return generateExceptionResponse(HttpStatus.BAD_REQUEST, errorMessages.toArray(new String[0]));
    }

    /**
     * Handles exceptions of type {@link ConstraintViolationException} that occur
     * when JSR-303 bean validation fails.
     *
     * @param ex      the {@link ConstraintViolationException} thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} containing an {@link ExceptionResponse} with
     * a status of {@code HttpStatus.BAD_REQUEST} and a message indicating
     * that the request validation failed
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<String> errorMessages = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        LogUtil.error("Validation failed for request [{}]: {}",
                request.getDescription(false), errorMessages);

        return generateExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ExceptionResponse> handleServiceUnavailableException(
            ServiceUnavailableException ex, WebRequest request) {
        LogUtil.error("Service unavailable for request [{}]: {}",
                request.getDescription(false), ex.getMessage());

        return generateExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE, ExceptionMessages.SERVICE_UNAVAILABLE);
    }

    /**
     * Handles exceptions of type {@link NotFoundException} that occur when a resource
     * could not be found.
     *
     * @param ex      the {@link NotFoundException} thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} containing an {@link ExceptionResponse} with
     * a status of {@code HttpStatus.NOT_FOUND} and a message indicating
     * that the requested resource was not found
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(
            NotFoundException ex, WebRequest request) {
        LogUtil.error("Resource not found [{}] on request [{}]: {}",
                ex.getClass().getSimpleName(), request.getDescription(false), ex.getMessage());

        return generateExceptionResponse(HttpStatus.NOT_FOUND, ExceptionMessages.RESOURCE_NOT_FOUND);
    }

    /**
     * Handles any unexpected exceptions that occur and return a generic
     * internal server error response.
     *
     * @param ex      the exception that was thrown
     * @param request the current web request
     * @return a {@link ResponseEntity} containing an {@link ExceptionResponse} with
     * a status of {@code HttpStatus.INTERNAL_SERVER_ERROR} and a message
     * indicating that an internal server error occurred
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
            Exception ex, WebRequest request) {
        LogUtil.error("Unexpected internal server error occurred on request [{}]: ",
                request.getDescription(false), ex);

        return generateExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generates a {@link ResponseEntity} containing an {@link ExceptionResponse}
     * based on the given {@link HttpStatus} and error messages.
     *
     * @param httpStatus the HTTP status of the response
     * @param messages   the error messages to be included in the response
     * @return a {@link ResponseEntity} containing the generated
     * {@link ExceptionResponse}
     */
    private static ResponseEntity<ExceptionResponse> generateExceptionResponse(
            HttpStatus httpStatus, String... messages) {
        var response = ExceptionResponse.of(httpStatus.value(), List.of(messages));
        return ResponseEntity.status(httpStatus).body(response);
    }
}
