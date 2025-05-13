package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.response.JobScoreResponse;
import reactor.core.publisher.Flux;

public interface RecommendationService {
    /**
     * Generates a list of job recommendations for the user and given limit.
     *
     * @param limit  the maximum number of recommendations to generate
     * @return a Flux of JobScoreResponse objects in descending order of score
     */
    Flux<JobScoreResponse> getJobRecommendations(int limit);
}
