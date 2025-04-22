package com.internship.recommendation_service.service.impl;

import com.internship.recommendation_service.dto.external.*;
import com.internship.recommendation_service.dto.response.JobScoreResponse;
import com.internship.recommendation_service.exception.ServiceUnavailableException;
import com.internship.recommendation_service.service.RecommendationService;
import com.internship.recommendation_service.service.client.*;
import com.internship.recommendation_service.util.LogUtil;
import com.internship.recommendation_service.util.RecommendationEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final JobServiceClient jobServiceClient;
    private final UserServiceClient userServiceClient;
    private final ReviewServiceClient reviewServiceClient;
    private final ReportServiceClient reportServiceClient;

    private final GeolocationServiceClient geoLocationServiceClient;
    private final RecommendationEngine recommendationEngine;

    @Override
    public Flux<JobScoreResponse> getJobRecommendations(Long userId, int limit) {
        LogUtil.info("Getting job recommendations for user {}", userId);

        // Fetch user-specific data concurrently
        Mono<UserDTO> userDetailsMono = userServiceClient.getUserDetails(userId).cache();
        Mono<GeoCoordinatesDTO> userCoordinatesMono = getUserCoordinates(userId, userDetailsMono);
        Mono<UserPreferencesDTO> userPreferencesMono = userServiceClient.getUserPreferences(userId).cache();
        Mono<List<Long>> favoriteWorkerIdsMono = userServiceClient.getFavoriteUserIds(userId).cache();
        Mono<List<Long>> blockedWorkerIdsMono = userServiceClient.getBlockedUserIds(userId).cache();

        // Get all active jobs
        return jobServiceClient.getAllJobs()
                .filter(this::isJobActive)
                .filterWhen(job -> isNotBlockedByUser(job.userId(), blockedWorkerIdsMono))
                .flatMap(job -> fetchDataAndScoreJob(job,
                        userCoordinatesMono,
                        userPreferencesMono,
                        favoriteWorkerIdsMono))
                .sort(Comparator.comparing(JobScoreResponse::score).reversed())
                .take(limit)
                .doOnComplete(() -> LogUtil.info("Finished generating recommendations for user {}", userId))
                .doOnError(e -> LogUtil.error("Error generating recommendations for user {}: {}",
                        userId,
                        e.getMessage()));
    }

    /**
     * Returns true if the job is active, false otherwise.
     * A job is considered active if its status is "ACCEPTED".
     *
     * @param job the job to check
     * @return true if the job is active, false otherwise
     */
    private boolean isJobActive(@Valid JobDTO job) {
        boolean isActive = job != null && "ACCEPTED".equals(job.status());
        LogUtil.info("Job {} is {} active (Status: {}).",
                job != null ? job.id() : "null",
                isActive ? "" : "not",
                job != null ? job.status() : "N/A");
        return isActive;
    }

    /**
     * Retrieves the geographical coordinates of the specified user.
     *
     * @param userId          the ID of the user whose coordinates are to be retrieved
     * @param userDetailsMono a Mono emitting the UserDTO containing user details
     * @return a Mono emitting the GeoCoordinatesDTO representing user's coordinates
     */
    @Valid
    private Mono<GeoCoordinatesDTO> getUserCoordinates(Long userId, Mono<UserDTO> userDetailsMono) {
        return userDetailsMono.flatMap(userDTO -> {
                    LogUtil.info("Getting user coordinates for user {}", userId);
                    return geoLocationServiceClient.getCoordinates(userDTO);
                })
                .doOnError(e -> LogUtil.warn("Failed to get coordinates for user {}: {}", userId, e.getMessage()))
                .cache();
    }

    /**
     * Checks if the specified worker is not blocked by the user.
     * <p>
     * This method logs the worker ID being checked, retrieves the list of blocked worker IDs,
     * and determines whether the given worker ID is not present in that list.
     *
     * @param workerId             The ID of the worker to check.
     * @param blockedWorkerIdsMono A Mono emitting a list of blocked worker IDs.
     * @return A Mono emitting true if the worker is not blocked, false if blocked, or true if the list is empty.
     */
    private Mono<Boolean> isNotBlockedByUser(Long workerId, Mono<List<Long>> blockedWorkerIdsMono) {
        LogUtil.info("Checking if worker {} is NOT blocked by user", workerId);
        return blockedWorkerIdsMono
                .map(blockedWorkers -> {
                    boolean notBlocked = !blockedWorkers.contains(workerId);
                    LogUtil.info("Checking if worker {} is blocked. Blocked list size: {}. Result: {}",
                            workerId, blockedWorkers.size(), notBlocked ? "Not Blocked" : "Blocked");
                    return notBlocked;
                })
                .defaultIfEmpty(true);
    }

    /**
     * Retrieves all relevant data for a job and calculates its recommendation score.
     *
     * @param jobDetails            the job for which data is to be retrieved
     * @param userCoordinatesMono   a Mono emitting the GeoCoordinatesDTO representing user's coordinates
     * @param userPreferencesMono   a Mono emitting the UserPreferencesDTO containing user preferences
     * @param favoriteWorkerIdsMono a Mono emitting a list of IDs of workers marked as favorite by the user
     * @return a Mono emitting a JobScoreResponse containing the calculated score
     */
    @Valid
    private Mono<JobScoreResponse> fetchDataAndScoreJob(
            JobDTO jobDetails,
            Mono<GeoCoordinatesDTO> userCoordinatesMono,
            Mono<UserPreferencesDTO> userPreferencesMono,
            Mono<List<Long>> favoriteWorkerIdsMono) {
        LogUtil.info("Fetching data for job {}", jobDetails.id());

        // Get worker ID
        Long workerId = jobDetails.userId();

        // Fetch worker and job specific data concurrently
        Mono<ReviewStatsDTO> workerReviewStatsMono = reviewServiceClient.getUserRating(workerId);
        Mono<ReviewStatsDTO> jobReviewStatsMono = reviewServiceClient.getJobRating(jobDetails.id());

        Mono<ReportStatsDTO> workerReportStatsMono = reportServiceClient.getUserReportStats(workerId);
        Mono<ReportStatsDTO> jobReportStatsMono = reportServiceClient.getJobReportStats(jobDetails.id());

        // Combine when all data is ready
        return Mono.zip(userCoordinatesMono,
                        userPreferencesMono,
                        workerReviewStatsMono,
                        jobReviewStatsMono,
                        workerReportStatsMono,
                        jobReportStatsMono,
                        favoriteWorkerIdsMono)
                .map(tuple -> buildJobScoreResponse(
                        jobDetails,
                        workerId,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6(),
                        tuple.getT7()))
                .subscribeOn(Schedulers.boundedElastic()); // Perform blocking calls or CPU-intensive work off the event loop
    }

    /**
     * Builds a JobScoreResponse by calculating the recommendation score for a given job
     * and worker using the provided data.
     *
     * @param jobDetails        the details of the job for which the score is to be calculated
     * @param workerId          the ID of the worker being considered
     * @param userCoordinates   the geographical coordinates of the user
     * @param userPreferences   the preferences of the user
     * @param workerReviewStats review statistics of the worker
     * @param jobReviewStats    review statistics of the job
     * @param workerReportStats report information for the worker
     * @param jobReportStats    report information for the job
     * @param favoriteWorkerIds list of favorite worker IDs
     * @return a JobScoreResponse containing the calculated score, or null if validation fails
     */
    @Valid
    private JobScoreResponse buildJobScoreResponse(
            JobDTO jobDetails,
            Long workerId,
            GeoCoordinatesDTO userCoordinates,
            UserPreferencesDTO userPreferences,
            ReviewStatsDTO workerReviewStats,
            ReviewStatsDTO jobReviewStats,
            ReportStatsDTO workerReportStats,
            ReportStatsDTO jobReportStats,
            List<Long> favoriteWorkerIds) {
        LogUtil.info("Fetched all data. Calculating recommendation score for job {} for worker {}",
                jobDetails.id(),
                workerId);

        // Check if review types are valid
        if (!isValidReviewType(workerReviewStats, "USER", "Worker", workerId) ||
            !isValidReviewType(jobReviewStats, "JOB", "Job", jobDetails.id())) {
            throw new ServiceUnavailableException("Review types are invalid.");
        }

        // Check if report types are valid
        if (!isValidReportType(workerReportStats, "USER", "Worker", workerId) ||
            !isValidReportType(jobReportStats, "JOB", "Job", jobDetails.id())) {
            throw new ServiceUnavailableException("Report types are invalid.");
        }

        // Call the recommendation engine with all fetched data
        return recommendationEngine.calculateJobScore(
                workerId,
                userCoordinates,
                userPreferences,
                workerReviewStats,
                jobReviewStats,
                workerReportStats,
                jobReportStats,
                jobDetails,
                favoriteWorkerIds);
    }

    /**
     * Checks if the given {@code ReviewStatsDTO} has the expected review type and logs an error if not.
     *
     * @param stats        the review statistics to check
     * @param expectedType the expected review type (e.g. "USER" or "JOB")
     * @param entityLabel  the label of the entity (e.g. "worker" or "job") for logging
     * @param entityId     the ID of the entity for logging
     * @return true if the review type matches the expected type, false otherwise
     */
    private boolean isValidReviewType(ReviewStatsDTO stats, String expectedType, String entityLabel, Long entityId) {
        if (!expectedType.equals(stats.reviewType())) {
            LogUtil.error("{} {} has no {} review", entityLabel, entityId, expectedType.toLowerCase());
            return false;
        }
        return true;
    }

    /**
     * Checks if the given {@code ReportStatsDTO} has the expected report type and logs an error if not.
     *
     * @param stats        the report statistics to check
     * @param expectedType the expected report type (e.g. "USER" or "JOB")
     * @param entityLabel  the label of the entity (e.g. "worker" or "job") for logging
     * @param entityId     the ID of the entity for logging
     * @return true if the report type matches the expected type, false otherwise
     */
    private boolean isValidReportType(ReportStatsDTO stats, String expectedType, String entityLabel, Long entityId) {
        if (!expectedType.equals(stats.reportType())) {
            LogUtil.error("{} {} has no {} report", entityLabel, entityId, expectedType.toLowerCase());
            return false;
        }
        return true;
    }
}
