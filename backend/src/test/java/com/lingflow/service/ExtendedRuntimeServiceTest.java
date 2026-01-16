package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExtendedRuntimeService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExtendedRuntimeServiceTest {

    @Mock
    private org.flowable.engine.RuntimeService flowableRuntimeService;

    @Mock
    private FlowableServiceTemplate serviceTemplate;

    @Mock
    private ProcessInstanceQuery processInstanceQuery;

    @InjectMocks
    private ExtendedRuntimeService extendedRuntimeService;

    private ProcessInstance processInstance;

    @BeforeEach
    void setUp() {
        processInstance = mock(ProcessInstance.class);
        when(processInstance.getId()).thenReturn("process1");
        when(processInstance.getProcessDefinitionId()).thenReturn("def1");
        when(processInstance.getBusinessKey()).thenReturn("business1");
        when(processInstance.getStartTime()).thenReturn(new Date());
    }

    @Test
    void testStartProcessInstanceByKey_WithBusinessKeyAndVariables() {
        ProcessInstanceVO instanceVO = new ProcessInstanceVO();
        instanceVO.setId("process1");
        instanceVO.setBusinessKey("business1");

        when(serviceTemplate.execute(eq("RuntimeService.startProcessInstanceByKey"), any(),
                eq("testProcess"), eq("business1"), eq(Map.of("key", "value")))).thenReturn(instanceVO);

        ProcessInstanceVO result = extendedRuntimeService.startProcessInstanceByKey(
                "testProcess", "business1", Map.of("key", "value"));

        assertNotNull(result);
        assertEquals("process1", result.getId());
        assertEquals("business1", result.getBusinessKey());
    }

    @Test
    void testStartProcessInstanceByKey_WithBusinessKeyOnly() {
        ProcessInstanceVO instanceVO = new ProcessInstanceVO();
        instanceVO.setId("process1");

        when(serviceTemplate.execute(eq("RuntimeService.startProcessInstanceByKey"), any(),
                eq("testProcess"), eq("business1"), isNull())).thenReturn(instanceVO);

        ProcessInstanceVO result = extendedRuntimeService.startProcessInstanceByKey(
                "testProcess", "business1", null);

        assertNotNull(result);
        assertEquals("process1", result.getId());
    }

    @Test
    void testStartProcessInstanceByKey_WithVariablesOnly() {
        ProcessInstanceVO instanceVO = new ProcessInstanceVO();
        instanceVO.setId("process1");

        when(serviceTemplate.execute(eq("RuntimeService.startProcessInstanceByKey"), any(),
                eq("testProcess"), eq(""), eq(Map.of("key", "value")))).thenReturn(instanceVO);

        ProcessInstanceVO result = extendedRuntimeService.startProcessInstanceByKey(
                "testProcess", "", Map.of("key", "value"));

        assertNotNull(result);
    }

    @Test
    void testStartProcessInstanceByKey_NoParameters() {
        ProcessInstanceVO instanceVO = new ProcessInstanceVO();
        instanceVO.setId("process1");

        when(serviceTemplate.execute(eq("RuntimeService.startProcessInstanceByKey"), any(),
                eq("testProcess"), eq(""), isNull())).thenReturn(instanceVO);

        ProcessInstanceVO result = extendedRuntimeService.startProcessInstanceByKey(
                "testProcess", null, null);

        assertNotNull(result);
    }

    @Test
    void testCreateProcessInstanceQuery_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.createProcessInstanceQuery"), any()))
                .thenReturn(processInstanceQuery);

        ProcessInstanceQuery result = extendedRuntimeService.createProcessInstanceQuery();

        assertNotNull(result);
        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.createProcessInstanceQuery"), any());
    }

    @Test
    void testGetRunningProcessInstances_Success() {
        List<ProcessInstance> instances = List.of(processInstance);

        when(serviceTemplate.execute(eq("RuntimeService.createProcessInstanceQuery"), any()))
                .thenReturn(processInstanceQuery);
        when(processInstanceQuery.orderByStartTime()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.desc()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.list()).thenReturn(instances);

        List<ProcessInstanceVO> result = extendedRuntimeService.getRunningProcessInstances();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("process1", result.get(0).getId());
    }

    @Test
    void testGetRunningProcessInstances_Empty() {
        when(serviceTemplate.execute(eq("RuntimeService.createProcessInstanceQuery"), any()))
                .thenReturn(processInstanceQuery);
        when(processInstanceQuery.orderByStartTime()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.desc()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.list()).thenReturn(new ArrayList<>());

        List<ProcessInstanceVO> result = extendedRuntimeService.getRunningProcessInstances();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveActivityIds_Success() {
        List<String> activityIds = List.of("task1", "task2");

        when(serviceTemplate.execute(eq("RuntimeService.getActiveActivityIds"), any(), eq("process1")))
                .thenReturn(activityIds);

        List<String> result = extendedRuntimeService.getActiveActivityIds("process1");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("task1"));
        assertTrue(result.contains("task2"));
    }

    @Test
    void testGetProcessInstance_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.createProcessInstanceQuery"), any()))
                .thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId("process1")).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(processInstance);

        ProcessInstanceVO result = extendedRuntimeService.getProcessInstance("process1");

        assertNotNull(result);
        assertEquals("process1", result.getId());
        assertEquals("def1", result.getProcessDefinitionId());
    }

    @Test
    void testGetProcessInstance_NotFound() {
        when(serviceTemplate.execute(eq("RuntimeService.createProcessInstanceQuery"), any()))
                .thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId("invalid-process")).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(null);

        ProcessInstanceVO result = extendedRuntimeService.getProcessInstance("invalid-process");

        assertNull(result);
    }

    @Test
    void testDeleteProcessInstance_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.deleteProcessInstance"), any(),
                eq("process1"), eq("测试原因"), eq(true))).thenReturn(null);

        extendedRuntimeService.deleteProcessInstance("process1", "测试原因", true);

        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.deleteProcessInstance"), any(),
                eq("process1"), eq("测试原因"), eq(true));
    }

    @Test
    void testSuspendProcessInstance_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.suspendProcessInstanceById"), any(), eq("process1")))
                .thenReturn(null);

        extendedRuntimeService.suspendProcessInstance("process1");

        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.suspendProcessInstanceById"), any(), eq("process1"));
    }

    @Test
    void testActivateProcessInstance_Success() {
        when(serviceTemplate.execute(eq("RuntimeService.activateProcessInstanceById"), any(), eq("process1")))
                .thenReturn(null);

        extendedRuntimeService.activateProcessInstance("process1");

        verify(serviceTemplate, times(1)).execute(eq("RuntimeService.activateProcessInstanceById"), any(), eq("process1"));
    }
}
