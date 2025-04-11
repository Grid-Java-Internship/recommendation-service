package com.internship.recommendation_service.dto.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationDTO(
        @NotNull(message = "Reservation ID must not be null") Long id,
        @NotNull(message = "Customer ID must not be null") Long customerId,
        @NotNull(message = "Worker ID must not be null") Long workerId,
        @NotNull(message = "Job ID must not be null") Long jobId,
        @NotBlank(message = "Reservation status must not be null") String status,
        @NotBlank(message = "Reservation title must not be null") String title,
        @NotBlank(message = "Reservation description must not be null") String description,
        @NotNull(message = "Reservation final price must not be null")
        @PositiveOrZero(message = "Reservation final price must be non-negative")
        Double finalPrice,
        @NotNull(message = "Reservation date must not be null") LocalDate date,
        @NotNull(message = "Reservation start time must not be null") LocalDateTime startTime,
        @NotNull(message = "Reservation end time must not be null") LocalDateTime endTime
) implements Serializable {
}
