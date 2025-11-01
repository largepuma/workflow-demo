package com.example.workflowdemo.service;

import com.example.workflowdemo.dto.TaskActionRequest;
import com.example.workflowdemo.dto.TaskDecisionRequest;
import com.example.workflowdemo.dto.TaskListItem;
import com.example.workflowdemo.dto.TaskOperationResponse;
import com.example.workflowdemo.identity.IdentityContextHolder;
import com.example.workflowdemo.logging.WorkflowAuditLogger;
import com.example.workflowdemo.process.ProcessState;
import com.example.workflowdemo.process.ProcessVariables;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskApplicationService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final IdentityService identityService;
    private final WorkflowAuditLogger auditLogger;

    public TaskApplicationService(TaskService taskService,
                                  RuntimeService runtimeService,
                                  IdentityService identityService,
                                  WorkflowAuditLogger auditLogger) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.identityService = identityService;
        this.auditLogger = auditLogger;
    }

    public List<TaskListItem> findTasks(String role, String userId) {
        String resolvedUserId = IdentityContextHolder.requireUserId(userId);
        String resolvedRole = IdentityContextHolder.requireRole(role);

        var query = taskService.createTaskQuery();

        query.taskAssignee(resolvedUserId);

        if ("approver".equalsIgnoreCase(resolvedRole)) {
            query.taskDefinitionKey("approvalTask");
        } else if ("executor".equalsIgnoreCase(resolvedRole)) {
            query.taskDefinitionKey("manualTask");
        }

        return query.orderByTaskCreateTime().desc()
                .list()
                .stream()
                .map(this::toTaskListItem)
                .collect(Collectors.toList());
    }

    public TaskOperationResponse approveTask(String taskId, TaskDecisionRequest request) {
        Task task = retrieveTask(taskId);
        String operator = IdentityContextHolder.requireUserId(request.userId());
        ensureAssignee(task, operator);

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessVariables.APPROVAL_RESULT, "APPROVED");
        variables.put(ProcessVariables.PROCESS_STATUS, ProcessState.MANUAL_PENDING.name());
        variables.put(ProcessVariables.LAST_COMMENT, request.comment());
        variables.put(ProcessVariables.LAST_OPERATOR, operator);

        applyComment(task, operator, request.comment());

        taskService.complete(task.getId(), variables);

        auditLogger.logOperation("TASK_APPROVED", task.getProcessInstanceId(), taskId, operator, "APPROVED");

        return new TaskOperationResponse(taskId, "APPROVED", ProcessState.MANUAL_PENDING);
    }

    public TaskOperationResponse rejectTask(String taskId, TaskDecisionRequest request) {
        Task task = retrieveTask(taskId);
        String operator = IdentityContextHolder.requireUserId(request.userId());
        ensureAssignee(task, operator);

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessVariables.APPROVAL_RESULT, "REJECTED");
        variables.put(ProcessVariables.PROCESS_STATUS, ProcessState.REJECTED.name());
        variables.put(ProcessVariables.LAST_COMMENT, request.reason() != null ? request.reason() : request.comment());
        variables.put(ProcessVariables.LAST_OPERATOR, operator);

        applyComment(task, operator, mergeReasonAndComment(request));

        taskService.complete(task.getId(), variables);

        auditLogger.logOperation("TASK_REJECTED", task.getProcessInstanceId(), taskId, operator, "REJECTED");

        return new TaskOperationResponse(taskId, "REJECTED", ProcessState.REJECTED);
    }

    public TaskOperationResponse completeTask(String taskId, TaskActionRequest request) {
        Task task = retrieveTask(taskId);
        String operator = IdentityContextHolder.requireUserId(request.userId());
        ensureAssignee(task, operator);

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessVariables.PROCESS_STATUS, ProcessState.COMPLETED.name());
        variables.put(ProcessVariables.LAST_COMMENT, request.comment());
        variables.put(ProcessVariables.LAST_OPERATOR, operator);

        applyComment(task, operator, request.comment());

        taskService.complete(task.getId(), variables);

        auditLogger.logOperation("TASK_COMPLETED", task.getProcessInstanceId(), taskId, operator, "COMPLETED");

        return new TaskOperationResponse(taskId, "COMPLETED", ProcessState.COMPLETED);
    }

    private Task retrieveTask(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        return task;
    }

    private void ensureAssignee(Task task, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User identity is required to operate on the task");
        }
        if (!userId.equals(task.getAssignee())) {
            throw new IllegalArgumentException("User " + userId + " is not assigned to task " + task.getId());
        }
    }

    private void applyComment(Task task, String userId, String comment) {
        if (!StringUtils.hasText(comment)) {
            return;
        }
        identityService.setAuthenticatedUserId(userId);
        try {
            taskService.createComment(task.getId(), task.getProcessInstanceId(), comment);
        } finally {
            identityService.clearAuthentication();
        }
    }

    private TaskListItem toTaskListItem(Task task) {
        Map<String, Object> payload = Map.of();
        Object rawPayload = runtimeService.getVariable(task.getExecutionId(), "payload");
        if (rawPayload instanceof Map<?, ?> map) {
            Map<String, Object> converted = new HashMap<>();
            map.forEach((k, v) -> {
                if (k != null) {
                    converted.put(k.toString(), v);
                }
            });
            payload = converted;
        }

        return new TaskListItem(
                task.getId(),
                task.getName(),
                task.getProcessInstanceId(),
                task.getCreateTime() != null
                        ? OffsetDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneOffset.UTC)
                        : null,
                payload
        );
    }

    private String mergeReasonAndComment(TaskDecisionRequest request) {
        if (StringUtils.hasText(request.reason()) && StringUtils.hasText(request.comment())) {
            return request.reason() + " | " + request.comment();
        }
        return StringUtils.hasText(request.reason()) ? request.reason() : request.comment();
    }
}
