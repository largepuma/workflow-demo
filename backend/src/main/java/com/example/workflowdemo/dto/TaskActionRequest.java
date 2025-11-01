package com.example.workflowdemo.dto;

public record TaskActionRequest(
        String userId,
        String comment
) {
}
