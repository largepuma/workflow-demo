package com.example.workflowdemo.dto;

import com.example.workflowdemo.process.ProcessState;

public record TaskOperationResponse(
        String taskId,
        String status,
        ProcessState nextState
) {
}
