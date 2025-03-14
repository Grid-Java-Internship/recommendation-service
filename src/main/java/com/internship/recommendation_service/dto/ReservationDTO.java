package com.internship.recommendation_service.dto;

import com.internship.recommendation_service.dto.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private Long id;

    private Long workerId;

    private Long jobId;

    private ReservationStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
