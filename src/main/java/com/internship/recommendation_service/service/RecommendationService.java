package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.response.JobScoreResponse;
import reactor.core.publisher.Flux;

public interface RecommendationService {
    /**
     * Generates a list of job recommendations for the given user ID and limit.
     *
     * @param userId the ID of the user to generate recommendations for
     * @param limit  the maximum number of recommendations to generate
     * @return a Flux of JobScoreResponse objects in descending order of score
     */
    Flux<JobScoreResponse> getJobRecommendations(Long userId, int limit);
}
