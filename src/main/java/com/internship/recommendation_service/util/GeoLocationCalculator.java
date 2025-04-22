package com.internship.recommendation_service.util;

import com.internship.recommendation_service.dto.external.GeoCoordinatesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeoLocationCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate the distance between two points on a sphere (such as the Earth)
     * using the Haversine formula.
     *
     * @param coordinates1 the coordinates of the first point
     * @param coordinates2 the coordinates of the second point
     * @return the distance between the two points in kilometers
     */
    public Double calculateDistance(GeoCoordinatesDTO coordinates1, GeoCoordinatesDTO coordinates2) {
        LogUtil.info("Calculating distance for user coords: {} and worker coords: {}", coordinates1, coordinates2);

        // User coordinates in radians
        double userLatRad = Math.toRadians(coordinates1.latitude());
        double userLonRad = Math.toRadians(coordinates1.longitude());

        // Worker coordinates in radians
        double workerLatRad = Math.toRadians(coordinates2.latitude());
        double workerLonRad = Math.toRadians(coordinates2.longitude());

        // Difference in coordinates
        double deltaLat = workerLatRad - userLatRad;
        double deltaLon = workerLonRad - userLonRad;

        // Calculate the distance in kilometers using the haversine formula
        double distance = EARTH_RADIUS_KM * haversine(deltaLat, userLatRad, workerLatRad, deltaLon);
        LogUtil.info("Calculated distance: {} km", distance);
        return distance;
    }

    /**
     * Haversine formula for calculating the distance between two coordinates on the Earth's surface.
     *
     * @param deltaLat     difference in latitude between the two coordinates in radians
     * @param userLatRad   latitude of the user in radians
     * @param workerLatRad latitude of the worker in radians
     * @param deltaLon     difference in longitude between the two coordinates in radians
     * @return the distance between the two coordinates in kilometers
     */
    private double haversine(double deltaLat, double userLatRad, double workerLatRad, double deltaLon) {
        double a = Math.pow(Math.sin(deltaLat / 2.0), 2) +
                   Math.cos(userLatRad) * Math.cos(workerLatRad) *
                   Math.pow(Math.sin(deltaLon / 2.0), 2);

        return 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }
}
