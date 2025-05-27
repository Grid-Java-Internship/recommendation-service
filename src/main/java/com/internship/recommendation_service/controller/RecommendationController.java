package com.internship.recommendation_service.controller;

import com.internship.recommendation_service.config.property.RecommendationDefaultsConfig;
import com.internship.recommendation_service.dto.response.JobScoreResponse;
import com.internship.recommendation_service.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final RecommendationDefaultsConfig defaults;

    /**
     * Endpoint to retrieve job recommendations for a specific user.
     *
     * @param limit  optional parameter to specify the maximum number of recommendations to return;
     *               defaults to a pre-configured limit if not provided or invalid
     * @return a Flux stream of job score responses containing job recommendations
     */
    @GetMapping("/jobs")
    public Flux<JobScoreResponse> getJobRecommendations(
            @RequestParam(required = false) Integer limit) {
        int effectiveLimit = (limit != null && limit > 0) ? limit : defaults.getLimit();
        return recommendationService.getJobRecommendations(effectiveLimit);
    }
}
