package com.internship.recommendation_service.service.impl;

import com.internship.recommendation_service.dto.external.*;
import com.internship.recommendation_service.dto.response.JobScoreResponse;
import com.internship.recommendation_service.exception.ServiceUnavailableException;
import com.internship.recommendation_service.service.client.*;
import com.internship.recommendation_service.util.RecommendationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationServiceImpl Tests")
class RecommendationServiceImplTest {
    @Mock
    private JobServiceClient mockJobServiceClient;

    @Mock
    private UserServiceClient mockUserServiceClient;

    @Mock
    private ReviewServiceClient mockReviewServiceClient;

    @Mock
    private ReportServiceClient mockReportServiceClient;

    @Mock
    private GeolocationServiceClient mockGeoLocationServiceClient;

    @Mock
    private RecommendationEngine mockRecommendationEngine;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    // Constants & Test Data
    private static final Long TEST_USER_ID = 1L;
    private static final int DEFAULT_LIMIT = 5;

    private JobDTO job1, job2, job3, inactiveJob, blockedWorkerJob;
    private JobScoreResponse score1, score2, score3;
    private ReviewStatsDTO worker2Reviews;
    private ReviewStatsDTO worker3Reviews;

    @BeforeEach
    void setUp() {
        // Common test data
        UserDTO testUserDTO = new UserDTO(TEST_USER_ID, "123 Main St", "Anytown", "12345", "USA");
        GeoCoordinatesDTO testUserCoords = new GeoCoordinatesDTO(40.0, -70.0);
        UserPreferencesDTO testUserPrefs = UserPreferencesDTO.defaultValue(TEST_USER_ID, 50.0, 2);
        List<Long> testFavorites = Collections.emptyList();
        List<Long> testBlocked = List.of(15L);

        // Active jobs
        job1 = new JobDTO(101L, 11L, "Job 1", "Desc 1", LocalDate.now(), 3, 25, "CAT1", "ACCEPTED", 40.1, -70.1);
        job2 = new JobDTO(102L, 12L, "Job 2", "Desc 2", LocalDate.now(), 5, 35, "CAT2", "ACCEPTED", 40.2, -70.2);
        job3 = new JobDTO(103L, 13L, "Job 3", "Desc 3", LocalDate.now(), 1, 20, "CAT1", "ACCEPTED", 40.3, -70.3);

        // Inactive job
        inactiveJob = new JobDTO(104L, 14L, "Inactive Job", "Desc 4", LocalDate.now(), 2, 30, "CAT1", "PENDING", 40.4, -70.4);

        // Job by a blocked worker
        blockedWorkerJob = new JobDTO(105L, 15L, "Blocked Worker Job", "Desc 5", LocalDate.now(), 4, 40, "CAT2", "ACCEPTED", 40.5, -70.5);

        // Reviews
        ReviewStatsDTO worker1Reviews = new ReviewStatsDTO(job1.userId(), "USER", 4.5, 10);
        worker2Reviews = new ReviewStatsDTO(job2.userId(), "USER", 4.8, 20);
        worker3Reviews = new ReviewStatsDTO(job3.userId(), "USER", 3.9, 5);

        ReviewStatsDTO job1Reviews = new ReviewStatsDTO(job1.id(), "JOB", 4.2, 8);
        ReviewStatsDTO job2Reviews = new ReviewStatsDTO(job2.id(), "JOB", 4.6, 15);
        ReviewStatsDTO job3Reviews = new ReviewStatsDTO(job3.id(), "JOB", 4.0, 3);

        // Reports
        ReportStatsDTO worker1Reports = ReportStatsDTO.defaultValue(job1.userId(), "USER");
        ReportStatsDTO worker2Reports = ReportStatsDTO.defaultValue(job2.userId(), "USER");
        ReportStatsDTO worker3Reports = ReportStatsDTO.defaultValue(job3.userId(), "USER");
        ReportStatsDTO job1Reports = ReportStatsDTO.defaultValue(job1.id(), "JOB");
        ReportStatsDTO job2Reports = ReportStatsDTO.defaultValue(job2.id(), "JOB");
        ReportStatsDTO job3Reports = ReportStatsDTO.defaultValue(job3.id(), "JOB");

        // Job scores
        score1 = JobScoreResponse.builder().jobId(job1.id()).workerId(job1.userId()).score(85.5).build();
        score2 = JobScoreResponse.builder().jobId(job2.id()).workerId(job2.userId()).score(95.0).build();
        score3 = JobScoreResponse.builder().jobId(job3.id()).workerId(job3.userId()).score(75.2).build();

        // User Service
        lenient().when(mockUserServiceClient.getUserDetails(TEST_USER_ID)).thenReturn(Mono.just(testUserDTO));
        lenient().when(mockUserServiceClient.getUserPreferences(TEST_USER_ID)).thenReturn(Mono.just(testUserPrefs));
        lenient().when(mockUserServiceClient.getFavoriteUserIds(TEST_USER_ID)).thenReturn(Mono.just(testFavorites));
        lenient().when(mockUserServiceClient.getBlockedUserIds(TEST_USER_ID)).thenReturn(Mono.just(testBlocked));

        // Geolocation Service
        lenient().when(mockGeoLocationServiceClient.getCoordinates(any(UserDTO.class))).thenReturn(Mono.just(testUserCoords));

        // Job Service
        lenient().when(mockJobServiceClient.getAllJobs()).thenReturn(Flux.just(job1, job2, job3, inactiveJob, blockedWorkerJob));

        // Review Service (Defaults for active jobs)
        lenient().when(mockReviewServiceClient.getUserRating(job1.userId())).thenReturn(Mono.just(worker1Reviews));
        lenient().when(mockReviewServiceClient.getUserRating(job2.userId())).thenReturn(Mono.just(worker2Reviews));
        lenient().when(mockReviewServiceClient.getUserRating(job3.userId())).thenReturn(Mono.just(worker3Reviews));
        lenient().when(mockReviewServiceClient.getJobRating(job1.id())).thenReturn(Mono.just(job1Reviews));
        lenient().when(mockReviewServiceClient.getJobRating(job2.id())).thenReturn(Mono.just(job2Reviews));
        lenient().when(mockReviewServiceClient.getJobRating(job3.id())).thenReturn(Mono.just(job3Reviews));

        // Report Service (Defaults for active jobs)
        lenient().when(mockReportServiceClient.getUserReportStats(job1.userId())).thenReturn(Mono.just(worker1Reports));
        lenient().when(mockReportServiceClient.getUserReportStats(job2.userId())).thenReturn(Mono.just(worker2Reports));
        lenient().when(mockReportServiceClient.getUserReportStats(job3.userId())).thenReturn(Mono.just(worker3Reports));
        lenient().when(mockReportServiceClient.getJobReportStats(job1.id())).thenReturn(Mono.just(job1Reports));
        lenient().when(mockReportServiceClient.getJobReportStats(job2.id())).thenReturn(Mono.just(job2Reports));
        lenient().when(mockReportServiceClient.getJobReportStats(job3.id())).thenReturn(Mono.just(job3Reports));

        // Recommendation Engine (Defaults for active jobs)
        lenient().when(mockRecommendationEngine.calculateJobScore(eq(job1.userId()), any(), any(), any(), any(), any(), any(), eq(job1), any())).thenReturn(score1);
        lenient().when(mockRecommendationEngine.calculateJobScore(eq(job2.userId()), any(), any(), any(), any(), any(), any(), eq(job2), any())).thenReturn(score2);
        lenient().when(mockRecommendationEngine.calculateJobScore(eq(job3.userId()), any(), any(), any(), any(), any(), any(), eq(job3), any())).thenReturn(score3);
    }

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPathTests {
        @Test
        @DisplayName("Should return sorted and limited recommendations when all services succeed")
        void shouldReturnSortedLimitedRecommendations() {
            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 2);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNext(score2) // Highest score first
                    .expectNext(score1) // Second highest
                    .verifyComplete();  // Limit is 2

            // Verify engine was called for the 3 active, non-blocked jobs
            verify(mockRecommendationEngine).calculateJobScore(eq(job1.userId()), any(), any(), any(), any(), any(), any(), eq(job1), any());
            verify(mockRecommendationEngine).calculateJobScore(eq(job2.userId()), any(), any(), any(), any(), any(), any(), eq(job2), any());
            verify(mockRecommendationEngine).calculateJobScore(eq(job3.userId()), any(), any(), any(), any(), any(), any(), eq(job3), any());
        }

        @Test
        @DisplayName("Should return fewer recommendations than limit if fewer jobs qualify")
        void shouldReturnFewerThanLimitIfFewerJobsQualify() {
            // Arrange: Only job1 and job2 are active and non-blocked
            when(mockJobServiceClient.getAllJobs()).thenReturn(Flux.just(job1, job2, inactiveJob)); // Only 2 active jobs
            when(mockRecommendationEngine.calculateJobScore(eq(job1.userId()), any(), any(), any(), any(), any(), any(), eq(job1), any())).thenReturn(score1);
            when(mockRecommendationEngine.calculateJobScore(eq(job2.userId()), any(), any(), any(), any(), any(), any(), eq(job2), any())).thenReturn(score2);

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 5);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNext(score2)
                    .expectNext(score1)
                    .verifyComplete(); // Emits only 2
        }

        @Test
        @DisplayName("Should return empty flux when no jobs are available")
        void shouldReturnEmptyWhenNoJobsAvailable() {
            // Arrange: Job service returns no jobs
            when(mockJobServiceClient.getAllJobs()).thenReturn(Flux.empty());

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 5);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty flux when all jobs are filtered out")
        void shouldReturnEmptyWhenAllJobsFiltered() {
            // Arrange: All jobs are inactive or blocked
            when(mockJobServiceClient.getAllJobs()).thenReturn(Flux.just(inactiveJob, blockedWorkerJob));
            when(mockUserServiceClient.getBlockedUserIds(TEST_USER_ID)).thenReturn(Mono.just(List.of(blockedWorkerJob.userId())));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 5);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNextCount(0)
                    .verifyComplete();

            // Verify engine is never called
            verify(mockRecommendationEngine, never()).calculateJobScore(anyLong(), any(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Job Filtering Scenarios")
    class JobFilteringTests {
        @Test
        @DisplayName("Should filter out inactive jobs")
        void shouldFilterInactiveJobs() {
            // Act: Default setup includes job1, job2, job3 (active) and inactiveJob
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 5);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNext(score2, score1, score3) // Only scores for active jobs
                    .verifyComplete();

            // Verify engine not called for inactive job
            verify(mockRecommendationEngine, never())
                    .calculateJobScore(eq(inactiveJob.userId()), any(), any(), any(), any(), any(), any(), eq(inactiveJob), any());
        }

        @Test
        @DisplayName("Should filter out jobs from blocked workers")
        void shouldFilterBlockedWorkerJobs() {
            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, 5);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNext(score2, score1, score3) // Only scores for non-blocked active workers
                    .verifyComplete();

            // Verify engine not called for blocked worker job
            verify(mockRecommendationEngine, never())
                    .calculateJobScore(eq(blockedWorkerJob.userId()), any(), any(), any(), any(), any(), any(), eq(blockedWorkerJob), any());
        }
    }

    @Nested
    @DisplayName("Client Error Handling Scenarios")
    class ClientErrorHandlingTests {

        @Test
        @DisplayName("Should propagate error when JobServiceClient fails")
        void shouldPropagateErrorWhenJobServiceFails() {
            // Arrange
            when(mockJobServiceClient.getAllJobs()).thenReturn(Flux.error(new ServiceUnavailableException("Jobs service down")));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    .expectError(ServiceUnavailableException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should propagate error when initial UserServiceClient call fails (e.g., getUserDetails)")
        void shouldPropagateErrorWhenUserServiceInitialFails() {
            // Arrange: getUserDetails fails, impacting getUserCoordinates directly
            when(mockUserServiceClient.getUserDetails(TEST_USER_ID)).thenReturn(Mono.error(new ServiceUnavailableException("User service down")));

            // Geolocation client will not be called or fail as well
            lenient().when(mockGeoLocationServiceClient.getCoordinates(any())).thenReturn(Mono.error(new ServiceUnavailableException("User service down")));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert: The error from getUserDetails or getCoordinates propagates through flatMap
            StepVerifier.create(recommendations)
                    .expectError(ServiceUnavailableException.class)
                    .verify();
            verify(mockJobServiceClient).getAllJobs(); // It attempts to get jobs
            verify(mockUserServiceClient).getUserDetails(TEST_USER_ID); // It attempts user details

            // Engine should not be called if setup fails
            verify(mockRecommendationEngine, never()).calculateJobScore(anyLong(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should use default preferences when UserServiceClient getUserPreferences fails")
        void shouldUseDefaultPreferencesOnError() {
            // Arrange: getUserPreferences fails, but client provides default
            UserPreferencesDTO defaultPrefsFromClient = UserPreferencesDTO.defaultValue(TEST_USER_ID, 100.0, 1); // Match client default logic
            when(mockUserServiceClient.getUserPreferences(TEST_USER_ID))
                    .thenReturn(Mono.just(defaultPrefsFromClient));

            // Mock RecommendationEngine to expect the default prefs provided by the client's onErrorResume
            when(mockRecommendationEngine.calculateJobScore(anyLong(), any(), any(UserPreferencesDTO.class), any(), any(), any(), any(), any(), any()))
                    .thenReturn(score1, score2, score3); // Return scores normally

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert: Should still complete and return results based on default prefs
            StepVerifier.create(recommendations)
                    .expectNextCount(3) // Expecting job2, job1, job3
                    .verifyComplete();

            // Verify engine was called with default UserPreferencesDTO
            verify(mockRecommendationEngine, times(3)).calculateJobScore(anyLong(), any(), isA(UserPreferencesDTO.class), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should use default stats when ReviewServiceClient fails")
        void shouldUseDefaultReviewStatsOnError() {
            // Arrange: getUserRating fails for worker1, client returns default
            ReviewStatsDTO defaultWorker1Reviews = ReviewStatsDTO.defaultValue(job1.userId(), "USER");
            when(mockReviewServiceClient.getUserRating(job1.userId()))
                    .thenReturn(Mono.just(defaultWorker1Reviews));

            // Mock engine to expect the default review stats for job1
            when(mockRecommendationEngine.calculateJobScore(eq(job1.userId()), any(), any(), eq(defaultWorker1Reviews), any(), any(), any(), eq(job1), any()))
                    .thenReturn(score1); // Still return score1 for simplicity, actual score might differ

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert: Stream should complete successfully
            StepVerifier.create(recommendations)
                    .expectNextCount(3) // job2, job1, job3
                    .verifyComplete();

            // Verify engine was called for job1 with the default ReviewStatsDTO
            verify(mockRecommendationEngine)
                    .calculateJobScore(eq(job1.userId()), any(), any(), eq(defaultWorker1Reviews), any(), any(), any(), eq(job1), any());
            verify(mockRecommendationEngine)
                    .calculateJobScore(eq(job2.userId()), any(), any(), eq(worker2Reviews), any(), any(), any(), eq(job2), any());
            verify(mockRecommendationEngine)
                    .calculateJobScore(eq(job3.userId()), any(), any(), eq(worker3Reviews), any(), any(), any(), eq(job3), any());
        }

        @Test
        @DisplayName("Should use default stats when ReportServiceClient fails")
        void shouldUseDefaultReportStatsOnError() {
            // Arrange: getUserReportStats fails for worker1, client returns default
            ReportStatsDTO defaultWorker1Reports = ReportStatsDTO.defaultValue(job1.userId(), "USER");
            when(mockReportServiceClient.getUserReportStats(job1.userId()))
                    .thenReturn(Mono.just(defaultWorker1Reports));

            // Mock engine to expect the default report stats for job1
            when(mockRecommendationEngine
                    .calculateJobScore(eq(job1.userId()), any(), any(), any(), any(), eq(defaultWorker1Reports), any(), eq(job1), any()))
                    .thenReturn(score1);

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert: Stream should complete successfully
            StepVerifier.create(recommendations)
                    .expectNextCount(3)
                    .verifyComplete();

            // Verify engine was called for job1 with the default ReportStatsDTO
            verify(mockRecommendationEngine)
                    .calculateJobScore(eq(job1.userId()), any(), any(), any(), any(), eq(defaultWorker1Reports), any(), eq(job1), any());
        }

        @Test
        @DisplayName("Should proceed without coordinates if GeolocationServiceClient fails (returns empty)")
        void shouldProceedWithoutCoordsIfGeoFails() {
            // Arrange: getCoordinates fails and client returns default coords
            when(mockGeoLocationServiceClient.getCoordinates(any(UserDTO.class)))
                    .thenReturn(Mono.just(GeoCoordinatesDTO.DEFAULT_VALUE));

            // Mock engine to be called with default coordinates
            when(mockRecommendationEngine
                    .calculateJobScore(anyLong(), eq(GeoCoordinatesDTO.DEFAULT_VALUE), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(score1, score2, score3); // Return scores even with null coords

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    .expectNextCount(3) // job2, job1, job3
                    .verifyComplete();

            // Verify engine was called with null GeoCoordinatesDTO
            verify(mockRecommendationEngine, times(3))
                    .calculateJobScore(anyLong(), eq(GeoCoordinatesDTO.DEFAULT_VALUE), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Data Validation Scenarios")
    class DataValidationTests {

        @Test
        @DisplayName("Should throw ServiceUnavailableException if worker review type is invalid")
        void shouldThrowExceptionOnInvalidWorkerReviewType() {
            // Arrange: Return a worker review DTO with type "JOB" instead of "USER"
            ReviewStatsDTO invalidWorkerReview = new ReviewStatsDTO(job1.userId(), "JOB", 4.5, 10); // Wrong type
            when(mockReviewServiceClient.getUserRating(job1.userId())).thenReturn(Mono.just(invalidWorkerReview));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    // Expect error originating from job1 processing
                    .expectErrorSatisfies(throwable -> assertThat(throwable).isInstanceOf(ServiceUnavailableException.class)
                            .hasMessageContaining("Review types are invalid"))
                    .verify();
        }

        @Test
        @DisplayName("Should throw ServiceUnavailableException if job review type is invalid")
        void shouldThrowExceptionOnInvalidJobReviewType() {
            // Arrange: Return a job review DTO with type "USER" instead of "JOB"
            ReviewStatsDTO invalidJobReview = new ReviewStatsDTO(job1.id(), "USER", 4.2, 8);
            when(mockReviewServiceClient.getJobRating(job1.id())).thenReturn(Mono.just(invalidJobReview));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    .expectErrorSatisfies(throwable -> assertThat(throwable).isInstanceOf(ServiceUnavailableException.class)
                            .hasMessageContaining("Review types are invalid"))
                    .verify();
        }

        @Test
        @DisplayName("Should throw ServiceUnavailableException if worker report type is invalid")
        void shouldThrowExceptionOnInvalidWorkerReportType() {
            // Arrange: Return a worker report DTO with type "JOB" instead of "USER"
            ReportStatsDTO invalidWorkerReport = new ReportStatsDTO(job1.userId(), "JOB", 0L, 0L, 0L);
            when(mockReportServiceClient.getUserReportStats(job1.userId())).thenReturn(Mono.just(invalidWorkerReport));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    .expectErrorSatisfies(throwable -> assertThat(throwable).isInstanceOf(ServiceUnavailableException.class)
                            .hasMessageContaining("Report types are invalid"))
                    .verify();
        }

        @Test
        @DisplayName("Should throw ServiceUnavailableException if job report type is invalid")
        void shouldThrowExceptionOnInvalidJobReportType() {
            // Arrange: Return a job report DTO with type "USER" instead of "JOB"
            ReportStatsDTO invalidJobReport = new ReportStatsDTO(job1.id(), "USER", 0L, 0L, 0L);
            when(mockReportServiceClient.getJobReportStats(job1.id())).thenReturn(Mono.just(invalidJobReport));

            // Act
            Flux<JobScoreResponse> recommendations = recommendationService.getJobRecommendations(TEST_USER_ID, DEFAULT_LIMIT);

            // Assert
            StepVerifier.create(recommendations)
                    .expectErrorSatisfies(throwable -> assertThat(throwable).isInstanceOf(ServiceUnavailableException.class)
                            .hasMessageContaining("Report types are invalid"))
                    .verify();
        }
    }
}