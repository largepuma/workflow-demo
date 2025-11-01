package com.example.workflowdemo.dto;

public record TaskDecisionRequest(
        String userId,
        String comment,
        String reason
) {
}
