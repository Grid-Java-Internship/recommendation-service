package com.internship.recommendation_service.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.internship.recommendation_service.dto.enums.Category;
import com.internship.recommendation_service.dto.enums.Status;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO implements Serializable {

    private Long id;

    private String title;

    private String description;

    private Category category;

    private Status status;
}
