package com.example.workflowdemo.listener;

import com.example.workflowdemo.logging.WorkflowAuditLogger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component("workflowEventListener")
public class WorkflowEventListener implements ExecutionListener, TaskListener {

    private final WorkflowAuditLogger auditLogger;

    public WorkflowEventListener(WorkflowAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public void notify(DelegateExecution execution) {
        auditLogger.logExecutionEvent(execution.getEventName(), execution);
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        auditLogger.logTaskEvent(delegateTask.getEventName(), delegateTask);
    }
}
