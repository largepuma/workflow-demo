package com.example.workflowdemo.controller;

import com.example.workflowdemo.dto.ProcessStatusResponse;
import com.example.workflowdemo.dto.StartProcessRequest;
import com.example.workflowdemo.dto.StartProcessResponse;
import com.example.workflowdemo.service.ProcessService;
import com.example.workflowdemo.service.ProcessStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessService processService;
    private final ProcessStatusService statusService;

    public ProcessController(ProcessService processService,
                             ProcessStatusService statusService) {
        this.processService = processService;
        this.statusService = statusService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartProcessResponse> startProcess(@Valid @RequestBody StartProcessRequest request) {
        return ResponseEntity.ok(processService.startProcess(request));
    }

    @GetMapping("/{processInstanceId}")
    public ResponseEntity<ProcessStatusResponse> getStatus(@PathVariable String processInstanceId) {
        return ResponseEntity.ok(statusService.getStatus(processInstanceId));
    }
}
