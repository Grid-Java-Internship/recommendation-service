package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.service.ReportServiceConfig;
import com.internship.recommendation_service.config.property.service.ServiceUrlsConfig;
import com.internship.recommendation_service.dto.external.ReportStatsDTO;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReportServiceClient {
    private final ServiceClient serviceClient;
    private final ServiceUrlsConfig serviceUrlsConfig;
    private final ReportServiceConfig reportServiceConfig;

    @Value("${security.feign.report-service.api-key}")
    private String reportApiKey;

    /**
     * Retrieves report information for a specific user by their ID.
     *
     * @param userId the ID of the user for whom the report information is being retrieved
     * @return a Mono that emits a ReportStatsDTO containing the report data for the specified user
     */
    public Mono<ReportStatsDTO> getUserReportStats(Long userId) {
        String url = serviceUrlsConfig.getReportService() +
                     reportServiceConfig.getBaseUrl() +
                     reportServiceConfig.getApiUserReportInfo() +
                     "/" + userId;

        return serviceClient
                .getMonoObject(url, ReportStatsDTO.class, reportApiKey)
                .onErrorResume(e -> {
                    LogUtil.error("Error retrieving report info for user {}", userId, e);
                    return Mono.just(ReportStatsDTO.defaultValue(userId, "USER"));
                });
    }

    /**
     * Retrieves report information for a specific job by its ID.
     *
     * @param jobId the ID of the job for which the report information is being retrieved
     * @return a Mono that emits a ReportStatsDTO containing the report data for the specified job
     */
    public Mono<ReportStatsDTO> getJobReportStats(Long jobId) {
        String url = serviceUrlsConfig.getReportService() +
                     reportServiceConfig.getBaseUrl() +
                     reportServiceConfig.getApiJobReportInfo() +
                     "/" + jobId;

        return serviceClient
                .getMonoObject(url, ReportStatsDTO.class, reportApiKey)
                .onErrorResume(e -> {
                    LogUtil.error("Error retrieving report info for job {}", jobId, e);
                    return Mono.just(ReportStatsDTO.defaultValue(jobId, "JOB"));
                });
    }
}
