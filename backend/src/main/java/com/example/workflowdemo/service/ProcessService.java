package com.example.workflowdemo.service;

import com.example.workflowdemo.dto.StartProcessRequest;
import com.example.workflowdemo.dto.StartProcessResponse;
import com.example.workflowdemo.dto.TaskSummary;
import com.example.workflowdemo.identity.IdentityContextHolder;
import com.example.workflowdemo.logging.WorkflowAuditLogger;
import com.example.workflowdemo.process.ProcessState;
import com.example.workflowdemo.process.ProcessVariables;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessService {

    private static final String PROCESS_DEFINITION_KEY = "workflowDemoProcess";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final WorkflowAuditLogger auditLogger;

    public ProcessService(RuntimeService runtimeService,
                          TaskService taskService,
                          WorkflowAuditLogger auditLogger) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.auditLogger = auditLogger;
    }

    public StartProcessResponse startProcess(StartProcessRequest request) {
        String initiator = IdentityContextHolder.requireUserId(request.initiator());
        String approverId = normalizeParticipant(request.approverId(), "approverId");
        String executorId = normalizeParticipant(request.executorId(), "executorId");

        Map<String, Object> variables = buildInitialVariables(request, initiator, approverId, executorId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
        Task task = taskService.createTaskQuery()
                .processInstanceId(instance.getProcessInstanceId())
                .singleResult();

        TaskSummary summary = task != null ? toSummary(task) : null;
        auditLogger.logOperation("PROCESS_START", instance.getProcessInstanceId(),
                task != null ? task.getId() : null, initiator, ProcessState.APPROVAL_PENDING.name());

        return new StartProcessResponse(instance.getProcessInstanceId(), ProcessState.APPROVAL_PENDING, summary);
    }

    private Map<String, Object> buildInitialVariables(StartProcessRequest request,
                                                      String initiator,
                                                      String approverId,
                                                      String executorId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessVariables.INITIATOR, initiator);
        variables.put(ProcessVariables.APPROVER_ID, approverId);
        variables.put(ProcessVariables.EXECUTOR_ID, executorId);
        variables.put(ProcessVariables.PROCESS_STATUS, ProcessState.APPROVAL_PENDING.name());
        variables.put(ProcessVariables.APPROVAL_RESULT, null);
        variables.put(ProcessVariables.LAST_COMMENT, null);
        variables.put(ProcessVariables.LAST_OPERATOR, initiator);

        if (!CollectionUtils.isEmpty(request.payload())) {
            variables.put("payload", new HashMap<>(request.payload()));
        }
        return variables;
    }

    private String normalizeParticipant(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Field '" + field + "' cannot be blank");
        }
        return value.trim();
    }

    private TaskSummary toSummary(Task task) {
        return new TaskSummary(
                task.getId(),
                task.getName(),
                task.getProcessInstanceId(),
                task.getCreateTime() != null
                        ? OffsetDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneOffset.UTC)
                        : null
        );
    }
}
