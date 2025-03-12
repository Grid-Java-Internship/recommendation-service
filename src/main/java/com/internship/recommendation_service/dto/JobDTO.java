package com.internship.recommendation_service.dto;


import com.internship.recommendation_service.dto.status.Category;
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
}
