package com.internship.recommendation_service.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ReportStatsDTO(
        @NotNull(message = "Report ID must not be null")
        @JsonProperty("id")
        Long reportedEntityId,
        @NotBlank(message = "Report type must not be blank")
        @JsonProperty("type")
        String reportType,
        @NotNull(message = "Low severity reports count must not be null")
        @PositiveOrZero(message = "Low severity reports count must be non-negative")
        Long lowSeverityCount,
        @NotNull(message = "Medium severity reports count must not be null")
        @PositiveOrZero(message = "Medium severity reports count must be non-negative")
        Long mediumSeverityCount,
        @NotNull(message = "High severity reports count must not be null")
        @PositiveOrZero(message = "High severity reports count must be non-negative")
        Long highSeverityCount
) {
    public static ReportStatsDTO defaultValue(Long reportedEntityId, String reportType) {
        return new ReportStatsDTO(reportedEntityId, reportType, 0L, 0L, 0L);
    }
}
