package com.example.workflowdemo.service;

import com.example.workflowdemo.dto.HistoryEntry;
import com.example.workflowdemo.dto.ProcessStatusResponse;
import com.example.workflowdemo.dto.TaskSummary;
import com.example.workflowdemo.process.ProcessState;
import com.example.workflowdemo.process.ProcessVariables;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessStatusService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public ProcessStatusService(RuntimeService runtimeService,
                                TaskService taskService,
                                HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    public ProcessStatusResponse getStatus(String processInstanceId) {
        ProcessState state = determineState(processInstanceId);
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        TaskSummary summary = currentTask != null ? toSummary(currentTask) : null;

        List<HistoryEntry> history = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list()
                .stream()
                .map(this::toHistoryEntry)
                .collect(Collectors.toList());

        Map<String, Object> variables = resolveVariables(processInstanceId);

        return new ProcessStatusResponse(processInstanceId, state, summary, history, variables);
    }

    private ProcessState determineState(String processInstanceId) {
        Object value = null;
        try {
            value = runtimeService.getVariable(processInstanceId, ProcessVariables.PROCESS_STATUS);
        } catch (Exception ignored) {
        }
        if (value == null) {
            HistoricVariableInstance historic = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .variableName(ProcessVariables.PROCESS_STATUS)
                    .singleResult();
            if (historic != null) {
                value = historic.getValue();
            }
        }
        return ProcessState.fromValue(value);
    }

    private Map<String, Object> resolveVariables(String processInstanceId) {
        try {
            return runtimeService.getVariables(processInstanceId);
        } catch (Exception ignored) {
            return historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue,
                            (left, right) -> right));
        }
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

    private HistoryEntry toHistoryEntry(HistoricActivityInstance instance) {
        return new HistoryEntry(
                instance.getActivityId(),
                instance.getActivityName(),
                instance.getActivityType(),
                instance.getAssignee(),
                resolveResult(instance),
                toOffset(instance.getStartTime()),
                toOffset(instance.getEndTime())
        );
    }

    private String resolveResult(HistoricActivityInstance instance) {
        if ("userTask".equals(instance.getActivityType())) {
            if ("manualTask".equals(instance.getActivityId())) {
                return ProcessState.COMPLETED.name();
            }
            HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(instance.getProcessInstanceId())
                    .variableName(ProcessVariables.APPROVAL_RESULT)
                    .singleResult();
            return variable != null && variable.getValue() != null ? variable.getValue().toString() : null;
        }
        return null;
    }

    private OffsetDateTime toOffset(Date date) {
        return date == null ? null : OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }
}
