package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.enums.Category;
import org.springframework.http.ResponseEntity;

public interface RecommendationService {

    JobDTO getJobRecommendation();

    JobDTO getJobRecommendationByRating(Category category);
}
