package com.internship.recommendation_service.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserPreferencesDTO(
        @NotNull(message = "User ID must not be null") Long userId,
        @JsonProperty("preferredDistanceRadius")
        Double preferredDistanceRadius,
        @JsonProperty("preferredExperience")
        Integer preferredYearsOfExperience,
        List<String> wantedCategories
) {
    public static UserPreferencesDTO defaultValue(Long userId, Double preferredDistanceRadius, Integer preferredYearsOfExperience) {
        return new UserPreferencesDTO(userId, preferredDistanceRadius, preferredYearsOfExperience, List.of());
    }
}
