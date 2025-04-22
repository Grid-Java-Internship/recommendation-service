package com.internship.recommendation_service.dto.external;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDTO(
        @NotNull(message = "User ID must not be null") Long id,
        @NotBlank(message = "User address must not be blank") String address,
        @NotBlank(message = "User city must not be blank") String city,
        @NotBlank(message = "User zip code must not be blank") String zipCode,
        @NotBlank(message = "User country must not be blank") String country
) {
}
