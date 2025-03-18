package com.internship.recommendation_service.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
    PLUMBER(1),
    ELECTRICIAN(2),
    CARPENTER(3),
    PAINTER(4),
    MECHANIC(5),
    LOCKSMITH(6),
    HANDYMAN(7),
    CLEANER(8),
    GARDENER(9),
    HAIRDRESSER(10),
    MAKEUP_ARTIST(11),
    MASSAGE_THERAPIST(12),
    DELIVERY_DRIVER(13);

    private final int id;

}
