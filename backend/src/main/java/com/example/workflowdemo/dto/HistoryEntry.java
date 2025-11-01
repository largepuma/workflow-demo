package com.example.workflowdemo.dto;

import java.time.OffsetDateTime;

public record HistoryEntry(
        String activityId,
        String activityName,
        String activityType,
        String assignee,
        String result,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
