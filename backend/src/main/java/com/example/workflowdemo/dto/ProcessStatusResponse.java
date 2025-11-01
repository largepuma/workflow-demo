package com.example.workflowdemo.dto;

import com.example.workflowdemo.process.ProcessState;

import java.util.List;
import java.util.Map;

public record ProcessStatusResponse(
        String processInstanceId,
        ProcessState state,
        TaskSummary currentTask,
        List<HistoryEntry> history,
        Map<String, Object> variables
) {
}
