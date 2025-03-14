package com.internship.recommendation_service.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {

    PENDING(1),
    REJECTED(2),
    ACCEPTED(3),
    BLOCKED(4);


    private final int id;
}
