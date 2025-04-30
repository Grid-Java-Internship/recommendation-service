package com.internship.recommendation_service.util;

import com.internship.recommendation_service.dto.external.GeoCoordinatesDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@DisplayName("GeoLocationCalculator Tests")
class GeoLocationCalculatorTest {
    private GeoLocationCalculator geoLocationCalculator;

    private static final double DISTANCE_TOLERANCE_KM = 0.1; // Allow 0.1 km difference

    @BeforeEach
    void setUp() {
        geoLocationCalculator = new GeoLocationCalculator();
    }

    @Nested
    @DisplayName("calculateDistance Method")
    class CalculateDistanceTests {
        private final GeoCoordinatesDTO london = GeoCoordinatesDTO.builder()
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        private final GeoCoordinatesDTO paris = GeoCoordinatesDTO.builder()
                .latitude(48.8566)
                .longitude(2.3522)
                .build();

        // Approximate distance London to Paris ~ 343.5km
        private static final double EXPECTED_LONDON_PARIS_DISTANCE = 343.5;

        @Test
        @DisplayName("Should return zero distance for identical coordinates")
        void shouldReturnZeroWhenCoordinatesAreIdentical() {
            // Arrange
            GeoCoordinatesDTO point = GeoCoordinatesDTO.builder().latitude(40.7128).longitude(-74.0060).build(); // New York City

            // Act
            Double distance = geoLocationCalculator.calculateDistance(point, point);

            // Assert
            assertThat(distance)
                    .as("Distance between identical points should be zero")
                    .isCloseTo(0.0, within(DISTANCE_TOLERANCE_KM));
        }

        @Test
        @DisplayName("Should calculate correct distance between London and Paris")
        void shouldCalculateCorrectDistanceForKnownPoints() {
            // Act
            Double distance = geoLocationCalculator.calculateDistance(london, paris);

            // Assert
            assertThat(distance)
                    .as("Calculated distance between London and Paris")
                    .isCloseTo(EXPECTED_LONDON_PARIS_DISTANCE, within(DISTANCE_TOLERANCE_KM));
        }

        @Test
        @DisplayName("Should calculate correct distance when order is reversed")
        void shouldCalculateSameDistanceWhenOrderReversed() {
            // Act
            Double distance1 = geoLocationCalculator.calculateDistance(london, paris);
            Double distance2 = geoLocationCalculator.calculateDistance(paris, london);

            // Assert
            assertThat(distance1)
                    .as("Distance calculation should be commutative")
                    .isEqualTo(distance2);
            assertThat(distance1)
                    .isCloseTo(EXPECTED_LONDON_PARIS_DISTANCE, within(DISTANCE_TOLERANCE_KM));
        }
    }
}