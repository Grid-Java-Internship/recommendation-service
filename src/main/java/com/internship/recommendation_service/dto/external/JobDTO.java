package com.internship.recommendation_service.dto.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.time.LocalDate;

public record JobDTO(
        @NotNull(message = "Job ID must not be null") Long id,
        @NotNull(message = "User ID must not be null") Long userId,
        @NotBlank(message = "Job title must not be empty") String title,
        @NotBlank(message = "Job description must not be empty") String description,
        @NotNull(message = "Job date of posting must not be null") LocalDate dateOfPosting,
        @NotNull(message = "Job experience must not be null")
        @PositiveOrZero(message = "Job experience must be non-negative")
        Integer experience,
        @NotNull(message = "Job hourly rate must not be null")
        @PositiveOrZero(message = "Job hourly rate must be non-negative")
        Integer hourlyRate,
        @NotBlank(message = "Job category must not be null") String category,
        @NotBlank(message = "Job status must not be null") String status
) implements Serializable {
}
