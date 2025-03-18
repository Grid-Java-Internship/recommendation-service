package com.internship.recommendation_service.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    String text;

    String title;

    Integer rating;
}
