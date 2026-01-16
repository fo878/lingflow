package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.service.ProcessVariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessVariableController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessVariableControllerTest {

    @Mock
    private ProcessVariableService variableService;

    @InjectMocks
    private ProcessVariableController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetProcessVariables_Success() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comment", "测试");

        when(variableService.getProcessVariables("process1")).thenReturn(variables);

        mockMvc.perform(get("/api/variables/process/process1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.approved").value(true))
                .andExpect(jsonPath("$.data.comment").value("测试"));

        verify(variableService, times(1)).getProcessVariables("process1");
    }

    @Test
    void testGetProcessVariables_Empty() throws Exception {
        when(variableService.getProcessVariables("process1")).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/variables/process/process1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(variableService, times(1)).getProcessVariables("process1");
    }

    @Test
    void testGetProcessVariable_Success() throws Exception {
        when(variableService.getProcessVariable("process1", "approved")).thenReturn(true);

        mockMvc.perform(get("/api/variables/process/process1/variable/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(variableService, times(1)).getProcessVariable("process1", "approved");
    }

    @Test
    void testSetProcessVariables_Success() throws Exception {
        doNothing().when(variableService).setProcessVariables(anyString(), anyMap());

        ProcessVariableController.SetVariablesRequest request =
                new ProcessVariableController.SetVariablesRequest();
        request.setProcessInstanceId("process1");
        request.setVariables(Map.of("key", "value"));

        mockMvc.perform(post("/api/variables/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).setProcessVariables(eq("process1"), anyMap());
    }

    @Test
    void testUpdateProcessVariable_Success() throws Exception {
        doNothing().when(variableService).updateProcessVariable(anyString(), anyString(), any());

        ProcessVariableController.UpdateVariableRequest request =
                new ProcessVariableController.UpdateVariableRequest();
        request.setProcessInstanceId("process1");
        request.setVariableName("approved");
        request.setValue(true);

        mockMvc.perform(put("/api/variables/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).updateProcessVariable("process1", "approved", true);
    }

    @Test
    void testRemoveProcessVariable_Success() throws Exception {
        doNothing().when(variableService).removeProcessVariable(anyString(), anyString());

        mockMvc.perform(delete("/api/variables/process/process1/variable/oldVar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).removeProcessVariable("process1", "oldVar");
    }

    @Test
    void testGetTaskVariables_Success() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("localVar", "value");

        when(variableService.getTaskVariables("task1")).thenReturn(variables);

        mockMvc.perform(get("/api/variables/task/task1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.localVar").value("value"));

        verify(variableService, times(1)).getTaskVariables("task1");
    }

    @Test
    void testGetTaskVariable_Success() throws Exception {
        when(variableService.getTaskVariable("task1", "localVar")).thenReturn("value");

        mockMvc.perform(get("/api/variables/task/task1/variable/localVar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("value"));

        verify(variableService, times(1)).getTaskVariable("task1", "localVar");
    }

    @Test
    void testSetTaskVariables_Success() throws Exception {
        doNothing().when(variableService).setTaskVariables(anyString(), anyMap());

        ProcessVariableController.SetTaskVariablesRequest request =
                new ProcessVariableController.SetTaskVariablesRequest();
        request.setTaskId("task1");
        request.setVariables(Map.of("key", "value"));

        mockMvc.perform(post("/api/variables/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).setTaskVariables(eq("task1"), anyMap());
    }

    @Test
    void testUpdateTaskVariable_Success() throws Exception {
        doNothing().when(variableService).updateTaskVariable(anyString(), anyString(), any());

        ProcessVariableController.UpdateTaskVariableRequest request =
                new ProcessVariableController.UpdateTaskVariableRequest();
        request.setTaskId("task1");
        request.setVariableName("localVar");
        request.setValue("newValue");

        mockMvc.perform(put("/api/variables/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).updateTaskVariable("task1", "localVar", "newValue");
    }

    @Test
    void testGetHistoricVariables_Success() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        when(variableService.getHistoricVariables("process1")).thenReturn(variables);

        mockMvc.perform(get("/api/variables/history/process1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).getHistoricVariables("process1");
    }

    @Test
    void testGetVariableHistory_Success() throws Exception {
        List<org.flowable.variable.api.history.HistoricVariableInstance> history = new ArrayList<>();
        when(variableService.getVariableHistory("process1", "var1")).thenReturn(history);

        mockMvc.perform(get("/api/variables/history/process1/variable/var1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(variableService, times(1)).getVariableHistory("process1", "var1");
    }

    @Test
    void testCopyVariables_Success() throws Exception {
        doNothing().when(variableService).copyVariables(anyString(), anyString(), anyList());

        ProcessVariableController.CopyVariablesRequest request =
                new ProcessVariableController.CopyVariablesRequest();
        request.setSourceProcessInstanceId("source1");
        request.setTargetProcessInstanceId("target1");
        request.setVariableNames(List.of("var1", "var2"));

        mockMvc.perform(post("/api/variables/copy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(variableService, times(1)).copyVariables("source1", "target1", List.of("var1", "var2"));
    }

    @Test
    void testGetProcessVariables_Exception() throws Exception {
        when(variableService.getProcessVariables(anyString()))
                .thenThrow(new RuntimeException("流程实例不存在"));

        mockMvc.perform(get("/api/variables/process/invalid-process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("流程实例不存在"));

        verify(variableService, times(1)).getProcessVariables("invalid-process");
    }
}
