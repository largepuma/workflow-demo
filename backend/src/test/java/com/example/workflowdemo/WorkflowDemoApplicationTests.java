package com.example.workflowdemo;

import com.example.workflowdemo.dto.ProcessStatusResponse;
import com.example.workflowdemo.dto.StartProcessResponse;
import com.example.workflowdemo.process.ProcessState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processHappyPathCompletes() throws Exception {
        StartProcessResponse startResponse = startProcess("initiator-1", "approver-1", "executor-1");
        assertThat(startResponse.state()).isEqualTo(ProcessState.APPROVAL_PENDING);
        assertThat(startResponse.currentTask()).isNotNull();

        List<Map<String, Object>> approverTasks = fetchTasks("approver", "approver-1");
        assertThat(approverTasks).hasSize(1);
        String approvalTaskId = (String) approverTasks.get(0).get("taskId");

        approveTask(approvalTaskId, "approver-1");

        List<Map<String, Object>> executorTasks = fetchTasks("executor", "executor-1");
        assertThat(executorTasks).hasSize(1);
        String manualTaskId = (String) executorTasks.get(0).get("taskId");

        completeTask(manualTaskId, "executor-1");

        ProcessStatusResponse status = fetchStatus(startResponse.processInstanceId());
        assertThat(status.state()).isEqualTo(ProcessState.COMPLETED);
        assertThat(status.currentTask()).isNull();
    }

    @Test
    void processRejectedEnds() throws Exception {
        StartProcessResponse startResponse = startProcess("initiator-2", "approver-2", "executor-2");

        List<Map<String, Object>> approverTasks = fetchTasks("approver", "approver-2");
        assertThat(approverTasks).hasSize(1);
        String approvalTaskId = (String) approverTasks.get(0).get("taskId");

        rejectTask(approvalTaskId, "approver-2");

        ProcessStatusResponse status = fetchStatus(startResponse.processInstanceId());
        assertThat(status.state()).isEqualTo(ProcessState.REJECTED);
    }

    private StartProcessResponse startProcess(String initiator, String approver, String executor) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "initiator", initiator,
                "approverId", approver,
                "executorId", executor,
                "payload", Map.of("amount", 100)
        ));

        MvcResult result = mockMvc.perform(post("/api/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), StartProcessResponse.class);
    }

    private List<Map<String, Object>> fetchTasks(String role, String userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tasks")
                        .param("role", role)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {
        });
    }

    private void approveTask(String taskId, String userId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "comment", "Looks good"
        ));
        mockMvc.perform(post("/api/tasks/{taskId}/approve", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void rejectTask(String taskId, String userId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "reason", "Insufficient data"
        ));
        mockMvc.perform(post("/api/tasks/{taskId}/reject", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void completeTask(String taskId, String userId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "userId", userId,
                "comment", "Manual step done"
        ));
        mockMvc.perform(post("/api/tasks/{taskId}/complete", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private ProcessStatusResponse fetchStatus(String processInstanceId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/process/{processInstanceId}", processInstanceId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), ProcessStatusResponse.class);
    }
}
