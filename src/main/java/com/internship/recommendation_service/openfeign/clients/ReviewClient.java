package com.internship.recommendation_service.openfeign.clients;

import com.internship.recommendation_service.dto.ReviewDTO;
import com.internship.recommendation_service.openfeign.clients.constants.ReviewServiceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = ReviewServiceInfo.NAME, url = ReviewServiceInfo.URL + ReviewServiceInfo.RESERVATION_PATH)
public interface ReviewClient {

    @GetMapping("job/getr/{jobid}")
    List<ReviewDTO> getJobReviews(@PathVariable("jobid") Long jobid);

}
