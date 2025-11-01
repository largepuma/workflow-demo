package com.example.workflowdemo.controller;

import com.example.workflowdemo.dto.TaskActionRequest;
import com.example.workflowdemo.dto.TaskDecisionRequest;
import com.example.workflowdemo.dto.TaskListItem;
import com.example.workflowdemo.dto.TaskOperationResponse;
import com.example.workflowdemo.service.TaskApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<TaskListItem>> findTasks(@RequestParam(required = false) String role,
                                                        @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(taskApplicationService.findTasks(role, userId));
    }

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<TaskOperationResponse> approve(@PathVariable String taskId,
                                                         @Valid @RequestBody TaskDecisionRequest request) {
        return ResponseEntity.ok(taskApplicationService.approveTask(taskId, request));
    }

    @PostMapping("/{taskId}/reject")
    public ResponseEntity<TaskOperationResponse> reject(@PathVariable String taskId,
                                                        @Valid @RequestBody TaskDecisionRequest request) {
        return ResponseEntity.ok(taskApplicationService.rejectTask(taskId, request));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<TaskOperationResponse> complete(@PathVariable String taskId,
                                                          @Valid @RequestBody TaskActionRequest request) {
        return ResponseEntity.ok(taskApplicationService.completeTask(taskId, request));
    }
}
