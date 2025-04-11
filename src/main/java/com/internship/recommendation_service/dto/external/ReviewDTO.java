package com.internship.recommendation_service.dto.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;

public record ReviewDTO(
        @NotNull(message = "Review ID must not be null") Long id,
        @NotNull(message = "User ID must not be null") Long userId,
        @NotNull(message = "Reviewed ID must not be null") Long reviewedId,
        @NotBlank(message = "Review type must not be null") String reviewType,
        @NotBlank(message = "Review status must not be null") String status,
        @NotNull(message = "Review rating must not be null")
        @PositiveOrZero(message = "Review rating must be greater than or equal to zero")
        Integer rating,
        @NotBlank(message = "Review text must not be empty") String text,
        @NotBlank(message = "Review date must not be empty") String reviewDate
) implements Serializable {
}
