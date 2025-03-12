package com.internship.recommendation_service.dto.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    PENDING_WORKER_APPROVAL(1),
    APPROVED(2),
    REJECTED(3),
    PENDING_CUSTOMER_APPROVAL(4),
    CANCELED_BY_WORKER(5),
    CANCELED_BY_CUSTOMER(6);

    private final int id;


}
