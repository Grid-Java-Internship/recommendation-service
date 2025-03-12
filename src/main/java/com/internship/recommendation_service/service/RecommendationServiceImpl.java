package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.ReservationDTO;
import com.internship.recommendation_service.dto.status.ReservationStatus;
import com.internship.recommendation_service.exception.JobNotFoundException;
import com.internship.recommendation_service.exception.ReservationsNotFoundException;
import com.internship.recommendation_service.openfeign.clients.JobClient;
import com.internship.recommendation_service.openfeign.clients.ReservationClient;
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

    @Override
    @Cacheable(value = "job-cache", key = "'most-reserved'")
    public JobDTO getJobRecommendation() {
        List<ReservationDTO> reservations = reservationClient.getReservations();

        if(reservations.isEmpty()) {
            throw new ReservationsNotFoundException(ReservationsNotFoundException.MESSAGE);
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
                .orElseThrow(() -> new JobNotFoundException(JobNotFoundException.MESSAGE));

    }
}
