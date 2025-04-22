package com.internship.recommendation_service.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record GeoCoordinatesDTO(
        @NotNull(message = "Latitude must not be null")
        @JsonProperty("lat")
        Double latitude,
        @NotNull(message = "Longitude must not be null")
        @JsonProperty("lon")
        Double longitude
) {
    public static final GeoCoordinatesDTO DEFAULT_VALUE = GeoCoordinatesDTO.builder()
            .latitude(99.0)
            .longitude(99.0)
            .build();
}
