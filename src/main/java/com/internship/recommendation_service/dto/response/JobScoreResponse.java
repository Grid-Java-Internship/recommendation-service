package com.internship.recommendation_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobScoreResponse(
        @NotNull(message = "Job ID must not be null") Long jobId,
        @NotNull(message = "Worker ID must not be null") Long workerId,
        @NotNull(message = "Score must not be null") Double score
) {
}
