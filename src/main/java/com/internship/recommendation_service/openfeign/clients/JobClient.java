package com.internship.recommendation_service.openfeign.clients;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.openfeign.clients.constants.JobServiceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = JobServiceInfo.NAME, url = JobServiceInfo.URL + JobServiceInfo.JOB_PATH)
public interface JobClient {

    @GetMapping("{id}")
    JobDTO getJobById(@PathVariable Long id);

    @GetMapping
    List<JobDTO> getAllJobs();

}
