package com.internship.recommendation_service.openfeign;

import com.internship.recommendation_service.dto.external.JobDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Validated
@FeignClient(name = "${feign.client.job-service.name}",
        url = "${microserviceUrls.job-service}",
        path = "${feign.client.job-service.base-path}")
public interface JobClient {
    /**
     * Returns a job with given id.
     *
     * @param id of the job
     * @return a job with given id
     */
    @Valid
    @GetMapping("/{id}")
    JobDTO getJobById(@PathVariable Long id);

    /**
     * Returns all jobs.
     *
     * @return a list of all jobs
     */
    @Valid
    @GetMapping
    List<JobDTO> getAllJobs();
}
