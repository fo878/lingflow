package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.dto.TaskVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * ProcessStatisticsService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessStatisticsServiceTest {

    @Mock
    private ExtendedRuntimeService extendedRuntimeService;

    @Mock
    private ExtendedHistoryService extendedHistoryService;

    @Mock
    private ExtendedTaskService extendedTaskService;

    @InjectMocks
    private ProcessStatisticsService statisticsService;

    @Test
    void testGetProcessInstanceStatistics_Success() {
        List<ProcessInstanceVO> runningInstances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        runningInstances.add(instance);

        List<ProcessInstanceVO> completedInstances = new ArrayList<>();

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(runningInstances);
        when(extendedHistoryService.getCompletedProcessInstances()).thenReturn(completedInstances);

        ProcessStatisticsService.ProcessInstanceStatistics result =
                statisticsService.getProcessInstanceStatistics();

        assertNotNull(result);
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
        verify(extendedHistoryService, times(1)).getCompletedProcessInstances();
    }

    @Test
    void testGetTaskStatistics_Success() {
        List<TaskVO> pendingTasks = new ArrayList<>();
        TaskVO task = new TaskVO();
        task.setId("task1");
        pendingTasks.add(task);

        when(extendedTaskService.getTasks()).thenReturn(pendingTasks);

        ProcessStatisticsService.TaskStatistics result = statisticsService.getTaskStatistics();

        assertNotNull(result);
        verify(extendedTaskService, times(1)).getTasks();
    }

    @Test
    void testGetProcessDefinitionStatistics_Success() {
        List<ProcessInstanceVO> runningInstances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setProcessDefinitionId("def1");
        runningInstances.add(instance);

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(runningInstances);

        List<ProcessStatisticsService.ProcessDefinitionStatistics> result =
                statisticsService.getProcessDefinitionStatistics();

        assertNotNull(result);
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
    }

    @Test
    void testGetUserTaskStatistics_Success() {
        List<TaskVO> userTasks = new ArrayList<>();
        TaskVO task1 = new TaskVO();
        task1.setId("task1");
        task1.setAssignee("user1");
        userTasks.add(task1);

        when(extendedTaskService.getTasksByAssignee("user1")).thenReturn(userTasks);

        ProcessStatisticsService.UserTaskStatistics result =
                statisticsService.getUserTaskStatistics("user1");

        assertNotNull(result);
        verify(extendedTaskService, times(1)).getTasksByAssignee("user1");
    }

    @Test
    void testGetProcessTrend_Success() {
        List<ProcessInstanceVO> runningInstances = new ArrayList<>();
        List<ProcessInstanceVO> completedInstances = new ArrayList<>();

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(runningInstances);
        when(extendedHistoryService.getCompletedProcessInstances()).thenReturn(completedInstances);

        java.util.Map<String, Object> result = statisticsService.getProcessTrend(7);

        assertNotNull(result);
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
        verify(extendedHistoryService, times(1)).getCompletedProcessInstances();
    }
}
