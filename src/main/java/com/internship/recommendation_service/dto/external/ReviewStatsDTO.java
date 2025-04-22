package com.internship.recommendation_service.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record ReviewStatsDTO(
        @NotNull(message = "Reviewed ID must not be null")
        @JsonProperty("id")
        Long reviewedId,
        @NotBlank(message = "Review type must not be blank") String reviewType,
        @NotNull(message = "Rating must not be null")
        @Min(value = 0, message = "Rating must be non-negative")
        @Max(value = 5, message = "Rating must be less than or equal to 5")
        @JsonProperty("rating")
        Double averageRating,
        @NotNull(message = "Review count must not be null")
        @PositiveOrZero(message = "Review count must be non-negative")
        Integer reviewCount
) {
    public static ReviewStatsDTO defaultValue(Long reviewedId, String reviewType) {
        return new ReviewStatsDTO(reviewedId, reviewType, 0.0, 0);
    }
}
