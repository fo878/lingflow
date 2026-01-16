package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.dto.TaskVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExtendedHistoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExtendedHistoryServiceTest {

    @Mock
    private org.flowable.engine.HistoryService flowableHistoryService;

    @Mock
    private FlowableServiceTemplate serviceTemplate;

    @Mock
    private HistoricProcessInstanceQuery historicProcessInstanceQuery;

    @Mock
    private HistoricTaskInstanceQuery historicTaskInstanceQuery;

    @InjectMocks
    private ExtendedHistoryService extendedHistoryService;

    @Test
    void testGetCompletedProcessInstances_Success() {
        List<HistoricProcessInstance> historicInstances = new ArrayList<>();
        HistoricProcessInstance historicInstance = mock(HistoricProcessInstance.class);
        when(historicInstance.getId()).thenReturn("process1");
        when(historicInstance.getProcessDefinitionId()).thenReturn("def1");
        when(historicInstance.getStartTime()).thenReturn(new Date());
        when(historicInstance.getEndTime()).thenReturn(new Date());
        historicInstances.add(historicInstance);

        when(serviceTemplate.execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any()))
                .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.finished()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.orderByProcessInstanceEndTime()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.desc()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.list()).thenReturn(historicInstances);

        List<ProcessInstanceVO> result = extendedHistoryService.getCompletedProcessInstances();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("process1", result.get(0).getId());
        verify(serviceTemplate, times(1)).execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any());
    }

    @Test
    void testGetCompletedProcessInstances_Empty() {
        when(serviceTemplate.execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any()))
                .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.finished()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.orderByProcessInstanceEndTime()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.desc()).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.list()).thenReturn(new ArrayList<>());

        List<ProcessInstanceVO> result = extendedHistoryService.getCompletedProcessInstances();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateHistoricProcessInstanceQuery_Success() {
        when(serviceTemplate.execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any()))
                .thenReturn(historicProcessInstanceQuery);

        HistoricProcessInstanceQuery result = extendedHistoryService.createHistoricProcessInstanceQuery();

        assertNotNull(result);
        verify(serviceTemplate, times(1)).execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any());
    }

    @Test
    void testCreateHistoricTaskInstanceQuery_Success() {
        when(serviceTemplate.execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any()))
                .thenReturn(historicTaskInstanceQuery);

        HistoricTaskInstanceQuery result = extendedHistoryService.createHistoricTaskInstanceQuery();

        assertNotNull(result);
        verify(serviceTemplate, times(1)).execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any());
    }

    @Test
    void testGetHistoricProcessInstance_Success() {
        HistoricProcessInstance historicInstance = mock(HistoricProcessInstance.class);
        when(historicInstance.getId()).thenReturn("process1");
        when(historicInstance.getProcessDefinitionId()).thenReturn("def1");
        when(historicInstance.getStartTime()).thenReturn(new Date());

        when(serviceTemplate.execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any()))
                .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceId("process1")).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.singleResult()).thenReturn(historicInstance);

        ProcessInstanceVO result = extendedHistoryService.getHistoricProcessInstance("process1");

        assertNotNull(result);
        assertEquals("process1", result.getId());
    }

    @Test
    void testGetHistoricProcessInstance_NotFound() {
        when(serviceTemplate.execute(eq("HistoryService.createHistoricProcessInstanceQuery"), any()))
                .thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.processInstanceId("invalid-process")).thenReturn(historicProcessInstanceQuery);
        when(historicProcessInstanceQuery.singleResult()).thenReturn(null);

        ProcessInstanceVO result = extendedHistoryService.getHistoricProcessInstance("invalid-process");

        assertNull(result);
    }

    @Test
    void testGetHistoricTasksByAssignee_Success() {
        List<HistoricTaskInstance> historicTasks = new ArrayList<>();
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        when(historicTask.getId()).thenReturn("task1");
        when(historicTask.getName()).thenReturn("审批任务");
        historicTasks.add(historicTask);

        when(serviceTemplate.execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any()))
                .thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.taskAssignee("user1")).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.finished()).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.desc()).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.list()).thenReturn(historicTasks);

        List<TaskVO> result = extendedHistoryService.getHistoricTasksByAssignee("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceTemplate, times(1)).execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any());
    }

    @Test
    void testGetHistoricTask_Success() {
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        when(historicTask.getId()).thenReturn("task1");
        when(historicTask.getName()).thenReturn("审批任务");
        when(historicTask.getProcessInstanceId()).thenReturn("process1");
        when(historicTask.getAssignee()).thenReturn("user1");
        when(historicTask.getCreateTime()).thenReturn(new Date());

        when(serviceTemplate.execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any()))
                .thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.taskId("task1")).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.singleResult()).thenReturn(historicTask);

        TaskVO result = extendedHistoryService.getHistoricTask("task1");

        assertNotNull(result);
        assertEquals("task1", result.getId());
    }

    @Test
    void testGetHistoricTask_NotFound() {
        when(serviceTemplate.execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any()))
                .thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.taskId("invalid-task")).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.singleResult()).thenReturn(null);

        TaskVO result = extendedHistoryService.getHistoricTask("invalid-task");

        assertNull(result);
    }

    @Test
    void testGetHistoricTasksByProcessInstanceId_Success() {
        List<HistoricTaskInstance> historicTasks = new ArrayList<>();
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        when(historicTask.getId()).thenReturn("task1");
        historicTasks.add(historicTask);

        when(serviceTemplate.execute(eq("HistoryService.createHistoricTaskInstanceQuery"), any()))
                .thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.processInstanceId("process1")).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.desc()).thenReturn(historicTaskInstanceQuery);
        when(historicTaskInstanceQuery.list()).thenReturn(historicTasks);

        List<TaskVO> result = extendedHistoryService.getHistoricTasksByProcessInstanceId("process1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
