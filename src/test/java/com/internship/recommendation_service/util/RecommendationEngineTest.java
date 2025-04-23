package com.internship.recommendation_service.util;

import com.internship.recommendation_service.config.property.RecommendationDefaultsConfig;
import com.internship.recommendation_service.config.property.RecommendationWeightsConfig;
import com.internship.recommendation_service.dto.external.*;
import com.internship.recommendation_service.dto.response.JobScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationEngine Tests")
class RecommendationEngineTest {
    @Mock
    private RecommendationWeightsConfig mockWeights;

    @Mock
    private RecommendationDefaultsConfig mockDefaults;

    @Mock
    private GeoLocationCalculator mockGeoLocationCalculator;

    @InjectMocks
    private RecommendationEngine recommendationEngine;

    // Constants for Test Data
    private static final Long DEFAULT_WORKER_ID = 1L;
    private static final Long DEFAULT_JOB_ID = 100L;
    private static final double SCORE_PRECISION = 0.01;

    // Default Test Data
    private GeoCoordinatesDTO defaultUserCoords;
    private UserPreferencesDTO defaultUserPrefs;
    private ReviewStatsDTO defaultWorkerReviews;
    private ReviewStatsDTO defaultJobReviews;
    private ReportStatsDTO defaultWorkerReports;
    private ReportStatsDTO defaultJobReports;
    private JobDTO defaultJobDetails;
    private List<Long> defaultFavorites;

    // Expected Score Calculation based on default setup for reuse in multiple tests
    private final double baseDistanceScore = 3.0;           // 5.0 * (1 - 10/25)
    private final double baseExperienceScore = 8.0;         // 3yrs >= 2yrs pref
    private final double baseCategoryScore = 15.0;          // "Plumbing" matches
    private final double baseFavoriteScore = 0.0;           // Not favorite by default
    private final double baseWorkerRatingScore = 12.0;      // 4.0 / 5.0 * 15.0
    private final double baseJobRatingScore = 14.0;         // 3.5 / 5.0 * 20.0
    private final double baseHourlyRatePenalty = -3.0;      // -0.1 * 30
    private final double baseWorkerReportsPenalty = 0.0;    // Default reports are zero
    private final double baseJobReportsPenalty = 0.0;       // Default reports are zero

    // Default initial sum (before penalties)
    private final double defaultInitialSum = baseDistanceScore + baseExperienceScore + baseCategoryScore +
                                             baseFavoriteScore + baseWorkerRatingScore + baseJobRatingScore; // 52.0

    // Default final score (penalties applied)
    private final double defaultFinalScore = defaultInitialSum + baseHourlyRatePenalty +
                                             baseWorkerReportsPenalty + baseJobReportsPenalty; // 49.0

    @BeforeEach
    void setUp() {
        // Configure mocks
        lenient().when(mockWeights.getDistance()).thenReturn(5.0);
        lenient().when(mockWeights.getExperienceMatch()).thenReturn(8.0);
        lenient().when(mockWeights.getCategoryMatch()).thenReturn(15.0);
        lenient().when(mockWeights.getFavorite()).thenReturn(10.0);
        lenient().when(mockWeights.getWorkerRating()).thenReturn(15.0);
        lenient().when(mockWeights.getJobRating()).thenReturn(20.0);
        lenient().when(mockWeights.getHourlyRate()).thenReturn(-0.1);
        lenient().when(mockWeights.getUserReportsLow()).thenReturn(-1.0);
        lenient().when(mockWeights.getUserReportsMedium()).thenReturn(-2.0);
        lenient().when(mockWeights.getUserReportsHigh()).thenReturn(-5.5);
        lenient().when(mockWeights.getJobReportsLow()).thenReturn(-2.0);
        lenient().when(mockWeights.getJobReportsMedium()).thenReturn(-5.0);
        lenient().when(mockWeights.getJobReportsHigh()).thenReturn(-7.5);

        lenient().when(mockDefaults.getMaxDistance()).thenReturn(100.0);
        lenient().when(mockDefaults.getMinExperience()).thenReturn(1);

        // Default GeoLocation Calculation
        lenient().when(mockGeoLocationCalculator.calculateDistance(any(GeoCoordinatesDTO.class), any(GeoCoordinatesDTO.class)))
                .thenReturn(10.0); // Default 10km distance

        // Default Input DTOs
        defaultJobDetails = new JobDTO(DEFAULT_JOB_ID,
                DEFAULT_WORKER_ID,
                "Fix leaky faucet",
                "...",
                LocalDate.now(),
                3,
                30,
                "Plumbing",
                "ACTIVE",
                40.7580,
                -73.9855);
        defaultUserCoords = new GeoCoordinatesDTO(40.7128, -74.0060);
        defaultUserPrefs = new UserPreferencesDTO(99L, 25.0, 2, List.of("Plumbing", "Electrical"));
        defaultWorkerReviews = new ReviewStatsDTO(DEFAULT_WORKER_ID, "USER", 4.0, 20);
        defaultJobReviews = new ReviewStatsDTO(DEFAULT_JOB_ID, "JOB", 3.5, 10);
        defaultWorkerReports = ReportStatsDTO.defaultValue(DEFAULT_WORKER_ID, "USER");
        defaultJobReports = ReportStatsDTO.defaultValue(DEFAULT_JOB_ID, "JOB");
        defaultFavorites = Collections.emptyList();
    }

    // Helper for Assertions
    private void assertResponse(JobScoreResponse response, double expectedScore) {
        assertThat(response).isNotNull();
        assertThat(response.jobId()).isEqualTo(DEFAULT_JOB_ID);
        assertThat(response.workerId()).isEqualTo(DEFAULT_WORKER_ID);
        assertThat(response.score()).isCloseTo(expectedScore, within(SCORE_PRECISION));
    }

    @Nested
    @DisplayName("Distance Score Tests (calculateDistanceScore)")
    class DistanceScoreTests {
        @Test
        @DisplayName("Should return 0.0 distance score when actual distance exceeds preferred")
        void shouldReturnZeroDistanceScoreWhenDistanceExceedsPreferred() {
            when(mockGeoLocationCalculator.calculateDistance(any(), any())).thenReturn(30.0); // 30km > 25km preferred
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should use default max distance when user preference is null or zero")
        void shouldUseDefaultMaxDistanceWhenPreferenceMissing() {
            UserPreferencesDTO prefsNoDistance = new UserPreferencesDTO(99L,
                    0.0,
                    2,
                    List.of("Plumbing"));

            double specificDistanceScore = 5.0 * (1 - 10.0 / 100.0); // 4.5
            double expectedScore = defaultFinalScore - baseDistanceScore + specificDistanceScore; // 49.0 - 3.0 + 4.5 = 50.5

            when(mockGeoLocationCalculator.calculateDistance(any(), any())).thenReturn(10.0); // 10km < 100km

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    prefsNoDistance,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return 0.0 distance score for invalid user latitude (>90)")
        void shouldReturnZeroDistanceScoreForInvalidCoords() {
            GeoCoordinatesDTO invalidCoords = new GeoCoordinatesDTO(95.0, -74.0);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    invalidCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero scores when UserPreferences is null (Affects D, E, C)")
        void shouldReturnZeroScoresWhenUserPreferencesAreNull() {
            // Expected: Distance = 0, Experience = 0, Category = 0
            double expectedScore = defaultFinalScore - baseDistanceScore - baseExperienceScore - baseCategoryScore; // 23.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    null,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero distance score when userCoordinates is null")
        void shouldReturnZeroDistanceScoreWhenUserCoordinatesAreNull() {
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    null,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when jobDetails lat is null")
        void shouldReturnZeroDistanceScoreWhenJobLatIsNull() {
            JobDTO jobNullLat = new JobDTO(DEFAULT_JOB_ID,
                    DEFAULT_WORKER_ID,
                    "Fix leaky faucet",
                    "...",
                    LocalDate.now(),
                    3,
                    30,
                    "Plumbing",
                    "ACTIVE",
                    null,
                    -73.9855);

            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    jobNullLat,
                    defaultFavorites);

            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when jobDetails lon is null")
        void shouldReturnZeroDistanceScoreWhenJobLonIsNull() {
            JobDTO jobNullLon = new JobDTO(DEFAULT_JOB_ID,
                    DEFAULT_WORKER_ID,
                    "Fix leaky faucet",
                    "...",
                    LocalDate.now(),
                    3,
                    30,
                    "Plumbing",
                    "ACTIVE",
                    40.7580,
                    null);

            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    jobNullLon,
                    defaultFavorites);

            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when user lat is null")
        void shouldReturnZeroDistanceScoreWhenUserLatIsNull() {
            GeoCoordinatesDTO coordsNullLat = new GeoCoordinatesDTO(null, -74.0);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    coordsNullLat,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);

            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when user lat is below -90")
        void shouldReturnZeroDistanceScoreWhenUserLatIsBelowMinus90() {
            GeoCoordinatesDTO coordsLowLat = new GeoCoordinatesDTO(-91.0, -74.0);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    coordsLowLat,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when user lon is null")
        void shouldReturnZeroDistanceScoreWhenUserLonIsNull() {
            GeoCoordinatesDTO coordsNullLon = new GeoCoordinatesDTO(40.0, null);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    coordsNullLon,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when user lon is below -180")
        void shouldReturnZeroDistanceScoreWhenUserLonIsBelowMinus180() {
            GeoCoordinatesDTO coordsLowLon = new GeoCoordinatesDTO(40.0, -181.0);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    coordsLowLon,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }

        @Test
        @DisplayName("Should return zero distance score when user lon is above 180")
        void shouldReturnZeroDistanceScoreWhenUserLonIsAbove180() {
            GeoCoordinatesDTO coordsHighLon = new GeoCoordinatesDTO(40.0, 181.0);
            double expectedScore = defaultFinalScore - baseDistanceScore; // 49.0 - 3.0 = 46.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    coordsHighLon,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
            verify(mockGeoLocationCalculator, never()).calculateDistance(any(), any());
        }
    }

    @Nested
    @DisplayName("Experience Score Tests (calculateExperienceMatchScore)")
    class ExperienceScoreTests {
        @Test
        @DisplayName("Should return 0.0 experience score when job experience is less than preferred")
        void shouldReturnZeroExperienceScoreWhenBelowPreference() {
            // Prefers 5 years or experience, job has 3 years
            UserPreferencesDTO prefsHighExp = new UserPreferencesDTO(99L,
                    25.0,
                    5,
                    List.of("Plumbing"));
            double expectedScore = defaultFinalScore - baseExperienceScore; // 49.0 - 8.0 = 41.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    prefsHighExp,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero experience score when Job experience is null")
        void shouldReturnZeroExperienceScoreWhenJobExperienceIsNull() {
            JobDTO jobNullExp = new JobDTO(DEFAULT_JOB_ID,
                    DEFAULT_WORKER_ID,
                    "Fix leaky faucet",
                    "...",
                    LocalDate.now(),
                    null,
                    30,
                    "Plumbing",
                    "ACTIVE",
                    40.7580,
                    -73.9855);

            double expectedScore = defaultFinalScore - baseExperienceScore; // 49.0 - 8.0 = 41.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    jobNullExp,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should use default min experience when preference is null or negative")
        void shouldUseDefaultMinExperienceWhenPreferenceIsNullOrNegative() {
            // Job has 3 years, default min is 1, i.e. 3 >= 1, so should get points
            UserPreferencesDTO prefsNullExp = new UserPreferencesDTO(99L, 25.0, null, List.of("Plumbing"));
            UserPreferencesDTO prefsNegExp = new UserPreferencesDTO(99L, 25.0, -1, List.of("Plumbing"));

            JobScoreResponse responseNull = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    prefsNullExp,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            JobScoreResponse responseNeg = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    prefsNegExp,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(responseNull, defaultFinalScore);
            assertResponse(responseNeg, defaultFinalScore);
        }
    }

    @Nested
    @DisplayName("Favorite Score Tests (calculateFavoriteScore)")
    class FavoriteScoreTests {
        @Test
        @DisplayName("Should add favorite score when worker is in favorite list")
        void shouldAddFavoriteScoreWhenWorkerIsFavorite() {
            List<Long> favoritesList = List.of(DEFAULT_WORKER_ID, 5L, 6L);
            double specificFavoriteScore = 10.0; // From mockWeights
            double expectedScore = defaultFinalScore - baseFavoriteScore + specificFavoriteScore; // 49.0 - 0.0 + 10.0 = 59.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    favoritesList);
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero favorite score when worker is not favorite")
        void shouldReturnZeroFavoriteScoreWhenWorkerIsNotFavorite() {
            // Worker 1L is not in list
            List<Long> favoritesList = List.of(2L, 3L);

            // Favorite score remains 0.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    favoritesList);
            assertResponse(response, defaultFinalScore);
        }

        @Test
        @DisplayName("Should return zero favorite score when list is null")
        void shouldReturnZeroFavoriteScoreWhenListIsNull() {
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    null);

            assertResponse(response, defaultFinalScore);
        }
    }

    @Nested
    @DisplayName("Worker Rating Score Tests (calculateWorkerRatingScore)")
    class WorkerRatingScoreTests {
        @Test
        @DisplayName("Should calculate zero worker rating score when stats are missing")
        void shouldCalculateZeroWorkerRatingScoreWhenStatsMissing() {
            double expectedScore = defaultFinalScore - baseWorkerRatingScore; // 49.0 - 12.0 = 37.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    null,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should calculate zero worker rating score when average rating is null")
        void shouldCalculateZeroWorkerRatingScoreWhenAverageRatingIsNull() {
            ReviewStatsDTO workerReviewsNullRating = new ReviewStatsDTO(DEFAULT_WORKER_ID, "USER", null, 20);
            double expectedScore = defaultFinalScore - baseWorkerRatingScore; // 49.0 - 12.0 = 37.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    workerReviewsNullRating,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }
    }

    @Nested
    @DisplayName("Job Rating Score Tests (calculateJobRatingScore)")
    class JobRatingScoreTests {
        @Test
        @DisplayName("Should calculate zero job rating score when stats are missing")
        void shouldCalculateZeroJobRatingScoreWhenStatsMissing() {
            double expectedScore = defaultFinalScore - baseJobRatingScore; // 49.0 - 14.0 = 35.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    null,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should calculate zero job rating score when average rating is null")
        void shouldCalculateZeroJobRatingScoreWhenAverageRatingIsNull() {
            ReviewStatsDTO jobReviewsNullRating = new ReviewStatsDTO(DEFAULT_JOB_ID, "JOB", null, 10);
            double expectedScore = defaultFinalScore - baseJobRatingScore; // 49.0 - 14.0 = 35.0
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    jobReviewsNullRating,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, expectedScore);
        }
    }

    @Nested
    @DisplayName("Category Match Score Tests (calculateCategoryMatchScore)")
    class CategoryMatchScoreTests {
        @Test
        @DisplayName("Should return 0.0 category score when job category does not match preferences")
        void shouldReturnZeroCategoryScoreWhenNoMatch() {
            JobDTO jobGardening = new JobDTO(DEFAULT_JOB_ID,
                    DEFAULT_WORKER_ID,
                    "Mow lawn",
                    "...",
                    LocalDate.now(),
                    3,
                    30,
                    "Gardening",
                    "ACTIVE"
                    , 40.7580,
                    -73.9855);

            double expectedScore = defaultFinalScore - baseCategoryScore; // 49.0 - 15.0 = 34.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    jobGardening,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero category score when wantedCategories is null")
        void shouldReturnZeroCategoryScoreWhenWantedCategoriesIsNull() {
            UserPreferencesDTO prefsNullCategories = new UserPreferencesDTO(99L,
                    25.0,
                    2,
                    null);

            double expectedScore = defaultFinalScore - baseCategoryScore; // 49.0 - 15.0 = 34.0

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    prefsNullCategories,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, expectedScore);
        }
    }

    @Nested
    @DisplayName("Hourly Rate Penalty Score Tests (calculateHourlyRatePenaltyScore)")
    class HourlyRatePenaltyScoreTests {
        @Test
        @DisplayName("Should apply hourly rate penalty when initial score is positive")
        void shouldApplyHourlyRatePenaltyWhenConditionsMet() {
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);
            assertResponse(response, defaultFinalScore);
        }

        @Test
        @DisplayName("Should return zero hourly rate score when job hourly rate is null")
        void shouldReturnZeroHourlyRateScoreWhenJobHourlyRateIsNull() {
            JobDTO jobNullRate = new JobDTO(DEFAULT_JOB_ID,
                    DEFAULT_WORKER_ID,
                    "Fix leaky faucet",
                    "...",
                    LocalDate.now(),
                    3,
                    null,
                    "Plumbing",
                    "ACTIVE",
                    40.7580,
                    -73.9855);

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    jobNullRate,
                    defaultFavorites);

            assertResponse(response, defaultInitialSum);
        }
    }

    @Nested
    @DisplayName("Worker Reports Score Tests (calculateWorkerReportsScore)")
    class WorkerReportsScoreTests {
        @Test
        @DisplayName("Should apply worker reports penalty when initial score positive and reports exist")
        void shouldApplyWorkerReportsPenaltyWhenConditionsMet() {
            // Arrange: Add reports, ensure initial score is positive
            ReportStatsDTO workerReportsWithIssues = new ReportStatsDTO(DEFAULT_WORKER_ID,
                    "USER",
                    1L,
                    1L,
                    0L);

            // Expected Penalty: (-2.0 * 1) + (-5.0 * 1) + (-7.5 * 0) = -7.0
            double expectedWorkerPenalty = -7.0;
            double expectedScore = defaultFinalScore - baseWorkerReportsPenalty + expectedWorkerPenalty; // 49.0 - 0.0 + (-7.0) = 42.0

            // Act
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    workerReportsWithIssues,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            // Assert
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero worker reports score when stats are missing")
        void shouldReturnZeroWorkerReportsScoreWhenStatsMissing() {
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    null,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, defaultFinalScore);
        }

        @Test
        @DisplayName("Should return zero worker reports score when type is not USER")
        void shouldReturnZeroWorkerReportsScoreWhenTypeIsNotUser() {
            ReportStatsDTO workerReportsWrongType = new ReportStatsDTO(DEFAULT_WORKER_ID,
                    "JOB",
                    1L,
                    1L,
                    1L);

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    workerReportsWrongType,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, defaultFinalScore);
        }
    }

    @Nested
    @DisplayName("Job Reports Score Tests (calculateJobReportsScore)")
    class JobReportsScoreTests {
        @Test
        @DisplayName("Should apply job reports penalty when initial score positive and reports exist")
        void shouldApplyJobReportsPenaltyWhenConditionsMet() {
            // Arrange: Add reports, ensure initial score is positive
            ReportStatsDTO jobReportsWithIssues = new ReportStatsDTO(DEFAULT_JOB_ID,
                    "JOB",
                    0L,
                    0L,
                    1L);

            // Expected Penalty (uses User weights): (-1.0 * 0) + (-2.0 * 0) + (-5.5 * 1) = -5.5
            double expectedJobPenalty = -5.5;
            double expectedScore = defaultFinalScore - baseJobReportsPenalty + expectedJobPenalty; // 49.0 - 0.0 + (-5.5) = 43.5

            // Act
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    jobReportsWithIssues,
                    defaultJobDetails,
                    defaultFavorites);

            // Assert
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should return zero job reports score when stats are missing")
        void shouldReturnZeroJobReportsScoreWhenStatsMissing() {
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    null,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, defaultFinalScore);
        }

        @Test
        @DisplayName("Should return zero job reports score when type is not JOB")
        void shouldReturnZeroJobReportsScoreWhenTypeIsNotJob() {
            ReportStatsDTO jobReportsWrongType = new ReportStatsDTO(DEFAULT_JOB_ID, "USER", 1L, 1L, 1L);

            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    jobReportsWrongType,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, defaultFinalScore);
        }
    }

    @Nested
    @DisplayName("Overall Score and Penalty Logic Tests")
    class OverallScoreAndPenaltyTests {
        @Test
        @DisplayName("Should calculate positive score with default favorable conditions (Overall Integration)")
        void shouldCalculatePositiveScoreWithDefaults() {
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            assertResponse(response, defaultFinalScore);
            verify(mockGeoLocationCalculator).calculateDistance(eq(defaultUserCoords), any(GeoCoordinatesDTO.class));
        }

        @Test
        @DisplayName("Should apply ALL penalty scores when initial score is positive")
        void shouldApplyAllPenaltyScoresWhenInitialScorePositive() {
            // Arrange: Add reports
            ReportStatsDTO workerReportsWithIssues = new ReportStatsDTO(DEFAULT_WORKER_ID, "USER", 1L, 1L, 0L);
            ReportStatsDTO jobReportsWithIssues = new ReportStatsDTO(DEFAULT_JOB_ID, "JOB", 0L, 0L, 1L);

            // Expected: Default initial 52.0
            // Hourly Rate: -3.0
            // Worker Reports: -7.0
            // Job Reports: -5.5
            // Final = 52.0 - 3.0 - 7.0 - 5.5 = 36.5
            double expectedScore = 36.5;

            // Act
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    workerReportsWithIssues,
                    jobReportsWithIssues,
                    defaultJobDetails,
                    defaultFavorites);

            // Assert
            assertResponse(response, expectedScore);
        }

        @Test
        @DisplayName("Should NOT apply ANY penalty scores when initial score is zero or negative")
        void shouldNotApplyAnyPenaltyScoresWhenInitialScoreNotPositive() {
            // Arrange: Force initial score = 0
            when(mockWeights.getDistance()).thenReturn(0.0);
            when(mockWeights.getExperienceMatch()).thenReturn(0.0);
            when(mockWeights.getCategoryMatch()).thenReturn(0.0);
            when(mockWeights.getWorkerRating()).thenReturn(0.0);
            when(mockWeights.getJobRating()).thenReturn(0.0);

            // Add reports/rate which WOULD cause penalties if initial score were positive
            ReportStatsDTO workerReportsWithIssues = new ReportStatsDTO(DEFAULT_WORKER_ID,
                    "USER",
                    1L,
                    1L,
                    0L);
            ReportStatsDTO jobReportsWithIssues = new ReportStatsDTO(DEFAULT_JOB_ID,
                    "JOB",
                    0L,
                    0L,
                    1L);

            // Act
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    defaultWorkerReviews,
                    defaultJobReviews,
                    workerReportsWithIssues,
                    jobReportsWithIssues,
                    defaultJobDetails,
                    defaultFavorites);

            // Assert
            assertResponse(response, 0.0);

            // Verify penalty weights were not accessed
            verify(mockWeights, never()).getHourlyRate();
            verify(mockWeights, never()).getJobReportsLow();
            verify(mockWeights, never()).getJobReportsMedium();
            verify(mockWeights, never()).getJobReportsHigh();
            verify(mockWeights, never()).getUserReportsLow();
            verify(mockWeights, never()).getUserReportsMedium();
            verify(mockWeights, never()).getUserReportsHigh();
        }

        @Test
        @DisplayName("Should round final score correctly to two decimal places")
        void shouldRoundFinalScore() {
            // Arrange: Use worker rating = 4.1 to introduce decimals
            ReviewStatsDTO preciseWorkerReviews = new ReviewStatsDTO(DEFAULT_WORKER_ID,
                    "USER",
                    4.1,
                    20);

            // Initial Sum = 3.0 + 8.0 + 15.0 + 0.0 + 12.3 + 14.0 = 52.3
            // Final Score = 52.3 - 3.0 = 49.3
            double expectedScore = 49.30;

            // Act
            JobScoreResponse response = recommendationEngine.calculateJobScore(DEFAULT_WORKER_ID,
                    defaultUserCoords,
                    defaultUserPrefs,
                    preciseWorkerReviews,
                    defaultJobReviews,
                    defaultWorkerReports,
                    defaultJobReports,
                    defaultJobDetails,
                    defaultFavorites);

            // Assert
            assertThat(response.score()).isEqualTo(expectedScore);
        }
    }
}