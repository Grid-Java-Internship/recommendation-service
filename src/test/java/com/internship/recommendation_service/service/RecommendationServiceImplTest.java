package com.internship.recommendation_service.service;

import com.internship.recommendation_service.dto.JobDTO;
import com.internship.recommendation_service.dto.ReservationDTO;
import com.internship.recommendation_service.dto.status.Category;
import com.internship.recommendation_service.dto.status.ReservationStatus;
import com.internship.recommendation_service.exception.JobNotFoundException;
import com.internship.recommendation_service.exception.ReservationsNotFoundException;
import com.internship.recommendation_service.openfeign.clients.JobClient;
import com.internship.recommendation_service.openfeign.clients.ReservationClient;
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

    private List<ReservationDTO> reservations;
    private JobDTO job;


    private ReservationDTO createApprovedReservation(Long id, Long jobId, Long workerId) {
        return  ReservationDTO.builder()
                .id(id)
                .jobId(jobId)
                .workerId(workerId)
                .status(ReservationStatus.APPROVED)
                .build();
    }

    @BeforeEach
    void setUp() {

        reservations = new ArrayList<>();

        reservations.add(createApprovedReservation(1L, 1L, 1L));

        reservations.add(createApprovedReservation(2L, 2L, 2L));

        reservations.add(createApprovedReservation(3L, 2L, 2L));

        job = JobDTO.builder()
                .id(2L)
                .title("Electrician needed for home repairs.")
                .description("We are looking for electrician.")
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
        assertEquals("Electrician needed for home repairs.", jobDTO.getTitle());
        assertEquals("We are looking for electrician.", jobDTO.getDescription());
        assertEquals(Category.PLUMBER, jobDTO.getCategory());

        verify(reservationClient, times(1)).getReservations();
        verify(jobClient, times(1)).getJobById(2L);
        verify(jobClient, times(0)).getJobById(1L);

    }

    @Test
    void getJobRecommendationReservationNotFoundException() {
        reservations.clear();
        when(reservationClient.getReservations()).thenReturn(reservations);

        ReservationsNotFoundException exception = assertThrows(ReservationsNotFoundException.class,
                () -> recommendationService.getJobRecommendation());
        assertEquals(ReservationsNotFoundException.MESSAGE, exception.getMessage());

        verify(reservationClient,times(1)).getReservations();
        verify(jobClient,never()).getJobById(anyLong());
    }

    @Test
    void getJobRecommendationJobNotFoundException() {

        when(reservationClient.getReservations()).thenReturn(reservations);
        when(jobClient.getJobById(2L)).thenReturn(null);

        JobNotFoundException exception = assertThrows(JobNotFoundException.class,
                () -> recommendationService.getJobRecommendation());
        assertEquals(JobNotFoundException.MESSAGE, exception.getMessage());

        verify(reservationClient, times(1)).getReservations();
        verify(jobClient, times(1)).getJobById(2L);
        verify(jobClient, times(0)).getJobById(1L);
    }
}