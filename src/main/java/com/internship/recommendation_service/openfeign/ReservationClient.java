package com.internship.recommendation_service.openfeign;

import com.internship.recommendation_service.dto.external.ReservationDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Validated
@FeignClient(name = "${feign.client.reservation-service.name}",
        url = "${microserviceUrls.reservation-service}",
        path = "${feign.client.reservation-service.base-path}")
public interface ReservationClient {
    /**
     * Get all reservations.
     *
     * @return a list of all reservations in the system.
     */
    @Valid
    @GetMapping
    List<ReservationDTO> getReservations();
}
