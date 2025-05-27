package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.service.ReviewServiceConfig;
import com.internship.recommendation_service.config.property.service.ServiceNamesConfig;
import com.internship.recommendation_service.dto.external.ReviewStatsDTO;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReviewServiceClient {
    private final ServiceClient serviceClient;
    private final ServiceNamesConfig serviceNamesConfig;
    private final ReviewServiceConfig reviewServiceConfig;

    @Value("${security.feign.review-service.api-key}")
    private String reviewApiKey;

    /**
     * Returns a Mono that emits a ReviewStatsDTO representing the rating of the user with the given ID.
     *
     * @param userId the ID of the user to retrieve
     * @return a Mono that emits a ReviewStatsDTO representing the rating of the user with the given ID
     */
    public Mono<ReviewStatsDTO> getUserRating(Long userId) {
        String url = serviceNamesConfig.getReviewService() +
                     reviewServiceConfig.getBaseUrl() +
                     reviewServiceConfig.getApiUserRating() +
                     "/" + userId;

        LogUtil.info("Getting user rating for user {}", userId);
        return serviceClient
                .getMonoObject(url, ReviewStatsDTO.class, reviewApiKey)
                .onErrorResume(error -> {
                    LogUtil.error("Error retrieving rating for user {}", userId, error);
                    return Mono.just(ReviewStatsDTO.defaultValue(userId, "USER"));
                });
    }

    /**
     * Returns a Mono that emits a ReviewStatsDTO representing the rating of the job with the given ID.
     *
     * @param jobId the ID of the job to retrieve
     * @return a Mono that emits a ReviewStatsDTO representing the rating of the job with the given ID
     */
    public Mono<ReviewStatsDTO> getJobRating(Long jobId) {
        String url = serviceNamesConfig.getReviewService() +
                     reviewServiceConfig.getBaseUrl() +
                     reviewServiceConfig.getApiJobRating() +
                     "/" + jobId;

        LogUtil.info("Getting job rating for job {}", jobId);
        return serviceClient
                .getMonoObject(url, ReviewStatsDTO.class, reviewApiKey)
                .onErrorResume(error -> {
                    LogUtil.error("Error retrieving rating for job {}", jobId, error);
                    return Mono.just(ReviewStatsDTO.defaultValue(jobId, "JOB"));
                });
    }
}
