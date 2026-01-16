package com.lingflow.service;

import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProcessVariableService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessVariableServiceTest {

    @Mock
    private org.flowable.engine.RuntimeService flowableRuntimeService;

    @Mock
    private org.flowable.engine.TaskService flowableTaskService;

    @Mock
    private org.flowable.engine.HistoryService flowableHistoryService;

    @Mock
    private FlowableServiceTemplate serviceTemplate;

    @InjectMocks
    private ProcessVariableService processVariableService;

    @Test
    void testSetProcessVariables_Success() {
        Map<String, Object> variables = Map.of("key", "value");
        doNothing().when(serviceTemplate).execute(eq("RuntimeService.setVariables"), any(), eq("process1"), eq(variables));

        processVariableService.setProcessVariables("process1", variables);

        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.setVariables"), any(), eq("process1"), eq(variables));
    }

    @Test
    void testGetProcessVariables_Success() {
        Map<String, Object> variables = Map.of("key", "value");
        when(serviceTemplate.execute(eq("RuntimeService.getVariables"), any(), eq("process1"))).thenReturn(variables);

        Map<String, Object> result = processVariableService.getProcessVariables("process1");

        assertNotNull(result);
        assertEquals("value", result.get("key"));
        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.getVariables"), any(), eq("process1"));
    }

    @Test
    void testGetProcessVariable_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.getVariable"), any(), eq("process1"), eq("key")))
                .thenReturn("value");

        Object result = processVariableService.getProcessVariable("process1", "key");

        assertNotNull(result);
        assertEquals("value", result);
    }

    @Test
    void testSetTaskVariables_Success() {
        Map<String, Object> variables = Map.of("key", "value");
        doNothing().when(serviceTemplate).execute(eq("TaskService.setVariables"), any(), eq("task1"), eq(variables));

        processVariableService.setTaskVariables("task1", variables);

        verify(serviceTemplate, times(1)).execute(eq("TaskService.setVariables"), any(), eq("task1"), eq(variables));
    }

    @Test
    void testGetTaskVariables_Success() {
        Map<String, Object> variables = Map.of("localKey", "localValue");
        when(serviceTemplate.execute(eq("TaskService.getVariables"), any(), eq("task1"))).thenReturn(variables);

        Map<String, Object> result = processVariableService.getTaskVariables("task1");

        assertNotNull(result);
        assertEquals("localValue", result.get("localKey"));
    }

    @Test
    void testGetTaskVariable_Success() {
        when(serviceTemplate.execute(eq("TaskService.getVariable"), any(), eq("task1"), eq("localKey")))
                .thenReturn("localValue");

        Object result = processVariableService.getTaskVariable("task1", "localKey");

        assertNotNull(result);
        assertEquals("localValue", result);
    }
}
