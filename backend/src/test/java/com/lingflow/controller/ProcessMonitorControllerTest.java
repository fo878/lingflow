package com.lingflow.controller;

import com.lingflow.service.ProcessMonitorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * ProcessMonitorController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessMonitorControllerTest {

    @Mock
    private ProcessMonitorService monitorService;

    @InjectMocks
    private ProcessMonitorController controller;

    private MockMvc mockMvc;

    @Test
    void testGetProcessInstanceMonitor_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ProcessMonitorService.ProcessInstanceMonitor monitor =
                new ProcessMonitorService.ProcessInstanceMonitor();
        monitor.setProcessInstanceId("process1");

        when(monitorService.getProcessInstanceMonitor("process1")).thenReturn(monitor);

        mockMvc.perform(get("/api/monitor/process/process1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.processInstanceId").value("process1"));

        verify(monitorService, times(1)).getProcessInstanceMonitor("process1");
    }

    @Test
    void testDetectTimeoutProcesses_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        List<String> timeoutProcesses = List.of("process1", "process2");

        when(monitorService.detectTimeoutProcesses(24L)).thenReturn(timeoutProcesses);

        mockMvc.perform(get("/api/monitor/timeout/24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("process1"));

        verify(monitorService, times(1)).detectTimeoutProcesses(24L);
    }

    @Test
    void testGetAllRunningProcessMonitors_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        List<ProcessMonitorService.ProcessInstanceMonitor> monitors = new ArrayList<>();

        when(monitorService.getAllRunningProcessMonitors()).thenReturn(monitors);

        mockMvc.perform(get("/api/monitor/process/running"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(monitorService, times(1)).getAllRunningProcessMonitors();
    }
}
