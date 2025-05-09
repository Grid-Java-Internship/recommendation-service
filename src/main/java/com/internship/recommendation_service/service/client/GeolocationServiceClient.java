package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.service.GeolocationServiceConfig;
import com.internship.recommendation_service.dto.external.GeoCoordinatesDTO;
import com.internship.recommendation_service.dto.external.UserDTO;
import com.internship.recommendation_service.exception.ServiceUnavailableException;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GeolocationServiceClient {
    private final ServiceClient serviceClient;
    private final GeolocationServiceConfig geolocationConfig;

    /**
     * Returns a Mono of the user's coordinates based on the given {@link UserDTO}.
     * <p>
     * The coordinates are retrieved by making a GET call to the geolocation service
     * with the user's city, zip, and country as the query. If the response is empty or null,
     * the method returns an error with a {@link ServiceUnavailableException}. If the call
     * fails, the method returns an error with a {@link ServiceUnavailableException}.
     * <p>
     * The coordinates are logged when successfully retrieved.
     *
     * @param userDTO the user to get the coordinates for
     * @return a Mono of the user's coordinates
     */
    public Mono<GeoCoordinatesDTO> getCoordinates(UserDTO userDTO) {
        String query = buildSearchQuery(userDTO);
        String url = buildSearchUrl(query);

        LogUtil.info("Getting coordinates for query: {}", query);

        return serviceClient
                .getMonoList(url, GeoCoordinatesDTO.class, "")
                .map(response -> {
                    if (response.isEmpty()) {
                        LogUtil.error("No coordinates found for query '{}'", query);
                        return GeoCoordinatesDTO.DEFAULT_VALUE;
                    }
                    return response.get(0);
                })
                .doOnSuccess(coordinates -> {
                    if (coordinates != null) {
                        LogUtil.info("Successfully retrieved coordinates for query '{}': {}", query, coordinates);
                    }
                })
                .onErrorResume(error -> {
                    LogUtil.error("Error retrieving coordinates for user {} with query '{}': {}", userDTO.id(), query, error.getMessage(), error);
                    return Mono.empty();
                });
    }

    /**
     * Builds a search query string from the user's address details.
     * <p>
     * This method constructs a query string by concatenating the user's address,
     * city, zip code, and country, replacing spaces with "+" for URL compatibility.
     * The resulting query is logged for informational purposes.
     *
     * @param userDTO the user data transfer object containing address details
     * @return a formatted search query string
     */
    private String buildSearchQuery(UserDTO userDTO) {
        LogUtil.info("Building query for user: {}", userDTO);
        return String.format("%s %s %s %s",
                userDTO.address(),
                userDTO.city(),
                userDTO.zipCode(),
                userDTO.country()
        ).trim().replaceAll("\\s+", "+"); // Trim and replace multiple spaces
    }

    /**
     * Builds a URL for the geolocation service based on the given query.
     * <p>
     * This method constructs a URL by concatenating the base URL of the geolocation
     * service with the API path and query parameters. The query parameter is set
     * to the given query string. The format parameter is set to "jsonv2" and the
     * limit parameter is set to 1.
     * <p>
     * The resulting URL is logged for informational purposes.
     *
     * @param query the query string to use
     * @return a formatted URL for the geolocation service
     */
    private String buildSearchUrl(String query) {
        LogUtil.info("Building url for query: {}", query);
        return UriComponentsBuilder.fromUriString(geolocationConfig.getBaseUrl())
                .path(geolocationConfig.getApiSearch())
                .queryParam("q", query)
                .queryParam("format", "jsonv2")
                .queryParam("limit", 1)
                .encode()
                .toUriString();
    }
}
