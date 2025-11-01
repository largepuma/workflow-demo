package com.example.workflowdemo.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WorkflowAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("workflow-audit");

    public void logExecutionEvent(String eventType, DelegateExecution execution) {
        LOGGER.info("Process event {}",
                eventType,
                StructuredArguments.keyValue("eventType", eventType),
                StructuredArguments.keyValue("processInstanceId", execution.getProcessInstanceId()),
                StructuredArguments.keyValue("executionId", execution.getId()),
                StructuredArguments.keyValue("activityId", execution.getCurrentActivityId()),
                StructuredArguments.keyValue("activityName", execution.getCurrentActivityName()),
                StructuredArguments.keyValue("timestamp", Instant.now().toString()));
    }

    public void logTaskEvent(String eventType, DelegateTask task) {
        LOGGER.info("Task event {}",
                eventType,
                StructuredArguments.keyValue("eventType", eventType),
                StructuredArguments.keyValue("processInstanceId", task.getProcessInstanceId()),
                StructuredArguments.keyValue("taskId", task.getId()),
                StructuredArguments.keyValue("taskDefinitionKey", task.getTaskDefinitionKey()),
                StructuredArguments.keyValue("assignee", task.getAssignee()),
                StructuredArguments.keyValue("timestamp", Instant.now().toString()));
    }

    public void logOperation(String eventType, String processInstanceId, String taskId, String operator, String result) {
        LOGGER.info("Operation {}",
                eventType,
                StructuredArguments.keyValue("eventType", eventType),
                StructuredArguments.keyValue("processInstanceId", processInstanceId),
                StructuredArguments.keyValue("taskId", taskId),
                StructuredArguments.keyValue("operator", operator),
                StructuredArguments.keyValue("result", result),
                StructuredArguments.keyValue("timestamp", Instant.now().toString()));
    }
}
