package com.internship.recommendation_service.openfeign.clients;

import com.internship.recommendation_service.dto.ReservationDTO;
import com.internship.recommendation_service.openfeign.clients.constants.ReservationServiceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = ReservationServiceInfo.NAME, url = ReservationServiceInfo.URL + ReservationServiceInfo.RESERVATION_PATH)
public interface ReservationClient {

    @GetMapping("/get-all")
    List<ReservationDTO> getReservations();

}
