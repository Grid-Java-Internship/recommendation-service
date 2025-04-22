package com.internship.recommendation_service.util;

import com.internship.recommendation_service.config.property.RecommendationDefaultsConfig;
import com.internship.recommendation_service.config.property.RecommendationWeightsConfig;
import com.internship.recommendation_service.dto.external.*;
import com.internship.recommendation_service.dto.response.JobScoreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
@RequiredArgsConstructor
public class RecommendationEngine {
    private final RecommendationWeightsConfig weights;
    private final RecommendationDefaultsConfig defaults;
    private final GeoLocationCalculator geoLocationCalculator;

    @Valid
    public JobScoreResponse calculateJobScore(
            Long workerId,
            GeoCoordinatesDTO userCoordinates,
            UserPreferencesDTO userPreferences,
            ReviewStatsDTO workerReviewStats,
            ReviewStatsDTO jobReviewStats,
            ReportStatsDTO workerReportStats,
            ReportStatsDTO jobReportStats,
            JobDTO jobDetails,
            List<Long> favoriteWorkerIds
    ) {
        LogUtil.info("Calculating recommendation score for worker [{}] and job [{}]", workerId, jobDetails.id());

        double totalScore = 0.0;
        totalScore += calculateDistanceScore(userCoordinates, userPreferences, jobDetails);
        totalScore += calculateExperienceMatchScore(userPreferences, jobDetails);
        totalScore += calculateFavoriteScore(favoriteWorkerIds, workerId);
        totalScore += calculateWorkerRatingScore(workerReviewStats, workerId);
        totalScore += calculateJobRatingScore(jobReviewStats, workerId);
        totalScore += calculateCategoryMatchScore(userPreferences, jobDetails);

        if (totalScore > 0) {
            totalScore += calculateHourlyRatePenaltyScore(jobDetails, workerId);
            totalScore += calculateWorkerReportsScore(workerReportStats);
            totalScore += calculateJobReportsScore(jobReportStats);
        }

        // Calculate the final score and it can be negative
        totalScore = Math.round(totalScore * 100.0) / 100.0;
        LogUtil.info("Final calculated score for worker {}: {}", workerId, totalScore);
        return JobScoreResponse.builder()
                .jobId(jobDetails.id())
                .workerId(workerId)
                .score(totalScore)
                .build();
    }

    private double calculateDistanceScore(GeoCoordinatesDTO userCoordinates, UserPreferencesDTO userPreferences, JobDTO jobDetails) {
        // If user or job data is missing, return 0.0
        if (userCoordinates == null
            || userPreferences == null
            || jobDetails == null
            || jobDetails.lat() == null
            || jobDetails.lon() == null) {
            LogUtil.warn("User data for distance score. Returning 0.0.");
            return 0.0;
        }

        // Check if latitude and longitude are valid
        if (userCoordinates.latitude() == null
            || userCoordinates.latitude() < -90.0
            || userCoordinates.latitude() > 90.0
            || userCoordinates.longitude() == null
            || userCoordinates.longitude() < -180.0
            || userCoordinates.longitude() > 180.0) {
            LogUtil.warn("User coordinates are invalid. Returning 0.0.");
            return 0.0;
        }

        // Preferred distance must be positive for calculation logic below
        double preferredDistance = (userPreferences.preferredDistanceRadius() != null
                                    && userPreferences.preferredDistanceRadius() > 0)
                ? userPreferences.preferredDistanceRadius()
                : defaults.getMaxDistance();

        // Call the distance calculation
        GeoCoordinatesDTO workerCoordinates = GeoCoordinatesDTO.builder()
                .latitude(jobDetails.lat())
                .longitude(jobDetails.lon())
                .build();
        double distance = geoLocationCalculator.calculateDistance(userCoordinates, workerCoordinates);

        // Once distance is calculated, apply scoring logic
        if (distance > preferredDistance) {
            LogUtil.info("Worker {} distance {}km > preferred {}km. Returning 0.0 distance score.",
                    jobDetails.userId(), distance, preferredDistance);
            return 0.0;
        }

        // Simple linear scaling: max points at 0km, 0 points at preferredDistance
        double distanceScore = weights.getDistance() * (1.0 - (distance / preferredDistance));
        double scoreToReturn = Math.max(distanceScore, 0.0);

        LogUtil.info("Worker {} distance {}km <= preferred {}km. Returning {} distance score.",
                jobDetails.userId(), distance, preferredDistance, scoreToReturn);
        return scoreToReturn;
    }

    private double calculateExperienceMatchScore(UserPreferencesDTO userPreferences, JobDTO jobDetails) {
        if (userPreferences == null
            || jobDetails == null
            || jobDetails.experience() == null) {
            LogUtil.warn("Missing data for experience score. Returning 0.0");
            return 0.0;
        }

        int preferredExperience = (userPreferences.preferredYearsOfExperience() != null
                                   && userPreferences.preferredYearsOfExperience() > 0)
                ? userPreferences.preferredYearsOfExperience()
                : defaults.getMinExperience();

        if (jobDetails.experience() < preferredExperience) {
            LogUtil.info("Worker {} experience {} < preferred {} experience. No points.",
                    jobDetails.userId(),
                    jobDetails.experience(),
                    userPreferences.preferredYearsOfExperience());
            return 0.0;
        }

        LogUtil.info("Worker {} experience {} >= preferred {} experience. Adding {} points.",
                jobDetails.userId(),
                jobDetails.experience(),
                userPreferences.preferredYearsOfExperience(),
                weights.getExperienceMatch());
        return weights.getExperienceMatch();
    }

    private double calculateHourlyRatePenaltyScore(JobDTO jobDetails, Long workerId) {
        if (jobDetails == null || jobDetails.hourlyRate() == null) {
            LogUtil.info("Missing hourly rate for worker {}. Returning 0.0", workerId);
            return 0.0;
        }

        double score = weights.getHourlyRate() * jobDetails.hourlyRate();
        LogUtil.info("Worker {} hourly rate {}. Adding {} points.", workerId, jobDetails.hourlyRate(), score);
        return score;
    }

    private double calculateFavoriteScore(List<Long> favoriteWorkerIds, Long workerId) {
        if (favoriteWorkerIds == null || !favoriteWorkerIds.contains(workerId)) {
            LogUtil.info("Worker {} is not favorite. Returning 0.0.", workerId);
            return 0.0;
        }

        LogUtil.info("Worker {} is a favorite worker. Adding {} points.", workerId, weights.getFavorite());
        return weights.getFavorite();
    }

    private double calculateWorkerRatingScore(ReviewStatsDTO workerReviewStats, Long workerId) {
        if (workerReviewStats == null || workerReviewStats.averageRating() == null) {
            LogUtil.info("Worker {} rating missing. Returning 0.0.", workerId);
            return 0.0;
        }

        double score = workerReviewStats.averageRating() * (weights.getWorkerRating() / 5.0);
        LogUtil.info("Worker {} average worker rating {}. Adding {} worker rating points.",
                workerId,
                workerReviewStats.averageRating(), score);
        return score;
    }

    private double calculateJobRatingScore(ReviewStatsDTO jobReviewStats, Long workerId) {
        if (jobReviewStats == null || jobReviewStats.averageRating() == null) {
            LogUtil.info("Job rating for worker {} missing. Returning 0.0", workerId);
            return 0.0;
        }

        double score = jobReviewStats.averageRating() * (weights.getJobRating() / 5.0);
        LogUtil.info("Worker {}'s job rating {}. Adding {} job rating points.",
                workerId,
                jobReviewStats.averageRating(), score);
        return score;
    }

    private double calculateWorkerReportsScore(ReportStatsDTO workerReportStats) {
        if (workerReportStats == null || !"USER".equals(workerReportStats.reportType())) {
            LogUtil.info("Invalid or missing worker report type. Returning 0.0");
            return 0.0;
        }

        double total = weights.getJobReportsLow() * workerReportStats.lowSeverityCount()
                       + weights.getJobReportsMedium() * workerReportStats.mediumSeverityCount()
                       + weights.getJobReportsHigh() * workerReportStats.highSeverityCount();

        LogUtil.info("Worker {} has {} low severity reports, {} medium severity reports, and {} high severity reports. " +
                      "Adding {} points.",
                workerReportStats.reportedEntityId(),
                workerReportStats.lowSeverityCount(),
                workerReportStats.mediumSeverityCount(),
                workerReportStats.highSeverityCount(), total);
        return total;
    }

    private double calculateJobReportsScore(ReportStatsDTO jobReportStats) {
        if (jobReportStats == null || !"JOB".equals(jobReportStats.reportType())) {
            LogUtil.info("Invalid or missing job report type. Returning 0.0");
            return 0.0;
        }

        double total = weights.getUserReportsLow() * jobReportStats.lowSeverityCount()
                       + weights.getUserReportsMedium() * jobReportStats.mediumSeverityCount()
                       + weights.getUserReportsHigh() * jobReportStats.highSeverityCount();

        LogUtil.info("Job {} has {} low severity reports, {} medium severity reports, and {} high severity reports. " +
                      "Adding {} points.",
                jobReportStats.reportedEntityId(),
                jobReportStats.lowSeverityCount(),
                jobReportStats.mediumSeverityCount(),
                jobReportStats.highSeverityCount(), total);
        return total;
    }

    private double calculateCategoryMatchScore(UserPreferencesDTO userPreferences, JobDTO jobDetails) {
        if (userPreferences == null || jobDetails == null || userPreferences.wantedCategories() == null) {
            LogUtil.info("Missing data for category match. Returning 0.0");
            return 0.0;
        }

        boolean match = userPreferences.wantedCategories().contains(jobDetails.category());
        LogUtil.info("Category match for worker {}: {}", jobDetails.userId(), match);
        return match ? weights.getCategoryMatch() : 0.0;
    }
}