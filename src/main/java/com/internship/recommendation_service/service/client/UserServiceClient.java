package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.RecommendationDefaultsConfig;
import com.internship.recommendation_service.config.property.service.ServiceUrlsConfig;
import com.internship.recommendation_service.config.property.service.UserServiceConfig;
import com.internship.recommendation_service.dto.external.UserDTO;
import com.internship.recommendation_service.dto.external.UserPreferencesDTO;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserServiceClient {
    private final ServiceClient serviceClient;
    private final ServiceUrlsConfig serviceUrlsConfig;
    private final UserServiceConfig userServiceConfig;
    private final RecommendationDefaultsConfig defaults;

    /**
     * Retrieves the details of a user given their user ID.
     *
     * @param userId the ID of the user whose details are to be retrieved
     * @return a Mono that emits a UserDTO containing the user's details
     */
    public Mono<UserDTO> getUserDetails(Long userId) {
        String url = serviceUrlsConfig.getUserService() +
                     userServiceConfig.getBaseUrlUsers() +
                     "/" + userId;

        LogUtil.info("Getting user details for user {}", userId);

        return serviceClient.getMonoObject(url, UserDTO.class);
    }

    /**
     * Retrieves the preferences of a user given their user ID.
     *
     * @param userId the ID of the user whose preferences are to be retrieved
     * @return a Mono that emits a UserPreferencesDTO containing the user's preferences
     */
    public Mono<UserPreferencesDTO> getUserPreferences(Long userId) {
        String url = serviceUrlsConfig.getUserService() +
                     userServiceConfig.getBaseUrlPreferences() +
                     "/" + userId;

        LogUtil.info("Getting user preferences for user {}", userId);
        return serviceClient
                .getMonoObject(url, UserPreferencesDTO.class)
                .onErrorResume(e -> {
                    LogUtil.warn("Failed to get user preferences for user {}: {}", userId, e.getMessage());
                    return Mono.just(UserPreferencesDTO.defaultValue(userId, defaults.getMaxDistance(), defaults.getMinExperience()));
                });
    }

    /**
     * Retrieves the IDs of all the users that the given user has favorited.
     *
     * @param userId the ID of the user whose favorited users are to be retrieved
     * @return a Mono that emits a list of user IDs
     */
    public Mono<List<Long>> getFavoriteUserIds(Long userId) {
        String url = serviceUrlsConfig.getUserService() +
                     userServiceConfig.getBaseUrlFavorites() +
                     "?userId=" + userId;

        LogUtil.info("Getting favorite users for user {}", userId);
        return serviceClient.getMonoList(url, Long.class);
    }

    /**
     * Retrieves the IDs of all the users that the given user has blocked.
     *
     * @param userId the ID of the user whose blocked users are to be retrieved
     * @return a Mono that emits a list of user IDs
     */
    public Mono<List<Long>> getBlockedUserIds(Long userId) {
        String url = serviceUrlsConfig.getUserService() +
                     userServiceConfig.getBaseUrlBlocks() +
                     "/" + userId;

        LogUtil.info("Getting blocked users for user {}", userId);
        return serviceClient.getMonoList(url, Long.class);
    }
}
