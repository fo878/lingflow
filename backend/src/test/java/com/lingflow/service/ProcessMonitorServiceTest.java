package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProcessMonitorService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessMonitorServiceTest {

    @Mock
    private ExtendedRuntimeService extendedRuntimeService;

    @Mock
    private ExtendedHistoryService extendedHistoryService;

    @Mock
    private ExtendedTaskService extendedTaskService;

    @InjectMocks
    private ProcessMonitorService processMonitorService;

    @Test
    void testGetProcessInstanceMonitor_Success() {
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instance.setProcessDefinitionId("def1");
        instance.setStartTime(new Date());

        when(extendedRuntimeService.getProcessInstance("process1")).thenReturn(instance);
        when(extendedRuntimeService.getActiveActivityIds("process1")).thenReturn(List.of("task1"));

        ProcessMonitorService.ProcessInstanceMonitor result =
                processMonitorService.getProcessInstanceMonitor("process1");

        assertNotNull(result);
        assertEquals("process1", result.getProcessInstanceId());
        verify(extendedRuntimeService, times(1)).getProcessInstance("process1");
    }

    @Test
    void testGetProcessInstanceMonitor_NotFound() {
        when(extendedRuntimeService.getProcessInstance("process1")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            processMonitorService.getProcessInstanceMonitor("process1");
        });
    }

    @Test
    void testDetectTimeoutProcesses_Success() {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instance.setStartTime(new Date(System.currentTimeMillis() - (30 * 3600 * 1000)));
        instances.add(instance);

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(instances);

        List<String> result = processMonitorService.detectTimeoutProcesses(24L);

        assertNotNull(result);
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
    }

    @Test
    void testGetAllRunningProcessMonitors_Success() {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instances.add(instance);

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(instances);
        when(extendedRuntimeService.getActiveActivityIds("process1")).thenReturn(List.of("task1"));

        List<ProcessMonitorService.ProcessInstanceMonitor> result =
                processMonitorService.getAllRunningProcessMonitors();

        assertNotNull(result);
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
    }
}
