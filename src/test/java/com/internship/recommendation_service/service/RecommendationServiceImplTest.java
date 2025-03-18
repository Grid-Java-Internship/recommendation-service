package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.ReservationDTO;
import com.internship.recommendation_service.dto.enums.Category;
import com.internship.recommendation_service.dto.enums.ReservationStatus;
import com.internship.recommendation_service.exception.JobNotFoundException;
import com.internship.recommendation_service.exception.ReservationNotFoundException;
import com.internship.recommendation_service.openfeign.clients.JobClient;
import com.internship.recommendation_service.openfeign.clients.ReservationClient;
import com.internship.recommendation_service.openfeign.clients.ReviewClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private ReservationClient reservationClient;

    @Mock
    private JobClient jobClient;

    @Mock
    private ReviewClient reviewClient;

    private List<ReservationDTO> reservations;
    private JobDTO job;


    private ReservationDTO createReservation(Long id, Long jobId, Long workerId, ReservationStatus status) {
        return  ReservationDTO.builder()
                .id(id)
                .jobId(jobId)
                .workerId(workerId)
                .status(status)
                .build();
    }

    @BeforeEach
    void setUp() {

        reservations = new ArrayList<>();

        reservations.add(createReservation(1L, 1L,
                1L, ReservationStatus.APPROVED));

        reservations.add(createReservation(2L, 2L,
                2L, ReservationStatus.APPROVED));

        reservations.add(createReservation(3L, 2L,
                2L, ReservationStatus.APPROVED));

        reservations.add(createReservation(4L, 1L,
                1L, ReservationStatus.PENDING_WORKER_APPROVAL));

        reservations.add(createReservation(5L, 1L,
                1L, ReservationStatus.PENDING_WORKER_APPROVAL));

        job = JobDTO.builder()
                .id(2L)
                .title("Skilled electrician.")
                .description("Electrician for home repairs.")
                .category(Category.PLUMBER)
                .build();
    }

    @Test
    void getJobRecommendationSuccessful() {
        when(reservationClient.getReservations()).thenReturn(reservations);
        when(jobClient.getJobById(2L)).thenReturn(job);

        JobDTO jobDTO = recommendationService.getJobRecommendation();

        assertNotNull(jobDTO);
        assertEquals(2L, jobDTO.getId());
        assertEquals("Skilled electrician.", jobDTO.getTitle());
        assertEquals("Electrician for home repairs.", jobDTO.getDescription());
        assertEquals(Category.PLUMBER, jobDTO.getCategory());

        verify(reservationClient, times(1)).getReservations();
        verify(jobClient, times(1)).getJobById(2L);
        verify(jobClient, times(0)).getJobById(1L);

    }

    @Test
    void getJobRecommendationReservationNotFoundException() {
        reservations.clear();
        when(reservationClient.getReservations()).thenReturn(reservations);

        ReservationNotFoundException exception = assertThrows(ReservationNotFoundException.class,
                () -> recommendationService.getJobRecommendation());
        assertEquals(ReservationNotFoundException.RESERVATIONS_NOT_FOUND, exception.getMessage());

        verify(reservationClient,times(1)).getReservations();
        verify(jobClient,never()).getJobById(anyLong());
    }

    @Test
    void getJobRecommendationReservationsNull() {
        when(reservationClient.getReservations()).thenReturn(null);

        ReservationNotFoundException exception = assertThrows(ReservationNotFoundException.class,
                () -> recommendationService.getJobRecommendation());
        assertEquals(ReservationNotFoundException.RESERVATIONS_NOT_FOUND, exception.getMessage());

        verify(reservationClient,times(1)).getReservations();
        verify(jobClient,never()).getJobById(anyLong());
    }

    @Test
    void getJobRecommendationJobNotFoundException() {
        when(reservationClient.getReservations()).thenReturn(reservations);
        when(jobClient.getJobById(2L)).thenReturn(null);

        JobNotFoundException exception = assertThrows(JobNotFoundException.class,
                () -> recommendationService.getJobRecommendation());
        assertEquals(JobNotFoundException.JOB_WITH_ID_NOT_FOUND, exception.getMessage());

        verify(reservationClient, times(1)).getReservations();
        verify(jobClient, times(1)).getJobById(2L);
        verify(jobClient, times(0)).getJobById(1L);
    }


}