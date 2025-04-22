package com.internship.recommendation_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {
    private final Integer statusCode;
    private final Boolean success;
    private final List<String> messages;
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Static factory method to create an {@link ExceptionResponse} with a status
     * code and a list of error messages.
     *
     * @param statusCode the HTTP status code of the exception
     * @param messages   the error messages associated with the exception
     * @return an {@link ExceptionResponse} with the given status code and error
     * messages
     */
    public static ExceptionResponse of(Integer statusCode, List<String> messages) {
        return ExceptionResponse.builder()
                .statusCode(statusCode)
                .success(false)
                .messages(messages)
                .build();
    }
}
