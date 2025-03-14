package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.ReservationDTO;
import com.internship.recommendation_service.dto.ReviewDTO;
import com.internship.recommendation_service.dto.enums.Category;
import com.internship.recommendation_service.dto.enums.ReservationStatus;
import com.internship.recommendation_service.dto.enums.Status;
import com.internship.recommendation_service.exception.JobNotFoundException;
import com.internship.recommendation_service.exception.ReservationNotFoundException;
import com.internship.recommendation_service.exception.ReviewNotFoundException;
import com.internship.recommendation_service.openfeign.clients.JobClient;
import com.internship.recommendation_service.openfeign.clients.ReservationClient;
import com.internship.recommendation_service.openfeign.clients.ReviewClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ReservationClient reservationClient;
    private final JobClient jobClient;
    private final ReviewClient reviewClient;

    @Override
    @Cacheable(value = "job-cache", key = "'most-reserved'")
    public JobDTO getJobRecommendation() {
        List<ReservationDTO> reservations = reservationClient.getReservations();

        if(reservations == null || reservations.isEmpty()) {
            throw new ReservationNotFoundException(ReservationNotFoundException.RESERVATIONS_NOT_FOUND);
        }

        Map<Long, List<ReservationDTO>> approvedReservations = reservations
                .stream()
                .filter(reservationDTO -> reservationDTO.getStatus().equals(ReservationStatus.APPROVED))
                .collect(Collectors.groupingBy(ReservationDTO::getJobId));

        Optional<Long> jobId = approvedReservations
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey);

        return jobId
                .map(jobClient::getJobById)
                .orElseThrow(() -> new JobNotFoundException(JobNotFoundException.JOB_WITH_ID_NOT_FOUND));
    }

    @Override
    @Cacheable(value = "job-rating-cache")
    public JobDTO getJobRecommendationByRating(Category category) {

        List<JobDTO> allJobs = jobClient.getAllJobs();

        if(allJobs == null || allJobs.isEmpty()) {
            throw new JobNotFoundException(JobNotFoundException.JOBS_NOT_FOUND);
        }

        List<JobDTO> filteredJobs  = allJobs
                .stream()
                .filter(job -> job.getStatus().equals(Status.ACCEPTED))
                .filter(job -> job.getCategory().equals(category))
                .toList();

        return filteredJobs
                .stream()
                .map(job -> new Object[]{job, reviewClient.getJobReviews(job.getId())})
                .filter(arr -> !((List<ReviewDTO>) arr[1]).isEmpty())
                .max(Comparator.comparingDouble(arr -> ((List<ReviewDTO>) arr[1])
                        .stream()
                        .mapToInt(ReviewDTO::getRating)
                        .average()
                        .orElse(0.0)))
                .map(arr -> (JobDTO) arr[0])
                .orElseThrow(() -> new ReviewNotFoundException(ReviewNotFoundException.REVIEWS_NOT_FOUND));
    }

}
