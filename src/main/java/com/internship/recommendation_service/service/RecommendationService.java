package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.enums.Category;
import com.internship.recommendation_service.exception.JobNotFoundException;
import com.internship.recommendation_service.exception.ReservationNotFoundException;

public interface RecommendationService {

    /**
     * Retrieves a job recommendation based on the most reserved job.
     *
     * @return a JobDTO representing the job with the highest number of approved reservations.
     * @throws ReservationNotFoundException if no reservations are found.
     * @throws JobNotFoundException if the job associated with the highest reservations is not found.
     */
    JobDTO getJobRecommendation();

    /**
     * Retrieves a job recommendation based on the job with the highest average rating for the given category.
     *
     * @param category the category of the job to recommend.
     * @return a JobDTO representing the job with the highest average rating for the given category.
     * @throws JobNotFoundException if the job associated with the highest average rating is not found.
     */
    JobDTO getJobRecommendationByRating(Category category);
}
