package com.example.workflowdemo.dto;

import java.time.OffsetDateTime;

public record TaskSummary(
        String taskId,
        String taskName,
        String processInstanceId,
        OffsetDateTime createdAt
) {
}
