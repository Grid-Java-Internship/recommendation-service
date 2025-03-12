package com.internship.recommendation_service.controller;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/job")
    public ResponseEntity<JobDTO> getJobRecommendation() {
        return ResponseEntity.ok(recommendationService.getJobRecommendation());
    }

}
