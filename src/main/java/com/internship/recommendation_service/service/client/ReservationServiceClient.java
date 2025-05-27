package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.service.ReservationServiceConfig;
import com.internship.recommendation_service.config.property.service.ServiceNamesConfig;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservationServiceClient {

    @Value("${security.feign.reservation-service.api-key}")
    private String reservationApiKey;

    private final ServiceClient serviceClient;
    private final ServiceNamesConfig serviceNamesConfig;
    private final ReservationServiceConfig reservationServiceConfig;

    public Mono<Long> getJobCount(Long jobId) {
        String url = serviceNamesConfig.getReservationService() +
                     reservationServiceConfig.getBaseUrl() +
                     reservationServiceConfig.getApiJobReservationCount() +
                     "/" + jobId + "/FINISHED";

        return serviceClient
                .getMonoObject(url, Long.class, reservationApiKey)
                .onErrorResume(e -> {
                    LogUtil.error("Error retrieving reservation count for job {}", jobId, e);
                    return Mono.just(0L);
                });
    }
}
