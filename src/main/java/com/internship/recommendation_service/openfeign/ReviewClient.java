package com.internship.recommendation_service.openfeign;

import com.internship.recommendation_service.dto.external.ReviewDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Validated
@FeignClient(name = "${feign.client.review-service.name}",
        url = "${microserviceUrls.review-service}",
        path = "${feign.client.review-service.base-path}")
public interface ReviewClient {
    /**
     * Returns a list of all reviews for a job with the given id.
     *
     * @param id the id of the job whose reviews are to be retrieved
     * @return a list of all reviews for the given job
     */
    @Valid
    @GetMapping("${feign.client.review-service.api.jobs}/{id}")
    List<ReviewDTO> getJobReviews(@PathVariable("id") Long id);
}
