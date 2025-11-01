package com.example.workflowdemo.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record TaskListItem(
        String taskId,
        String taskName,
        String processInstanceId,
        OffsetDateTime createdAt,
        Map<String, Object> payload
) {
}
