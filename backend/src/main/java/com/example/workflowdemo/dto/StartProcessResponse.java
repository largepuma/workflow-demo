package com.example.workflowdemo.dto;

import com.example.workflowdemo.process.ProcessState;

public record StartProcessResponse(
        String processInstanceId,
        ProcessState state,
        TaskSummary currentTask
) {
}
