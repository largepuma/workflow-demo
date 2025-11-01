package com.example.workflowdemo.dto;

import java.util.Map;

public record StartProcessRequest(
        String initiator,
        String approverId,
        String executorId,
        Map<String, Object> payload
) {
}
