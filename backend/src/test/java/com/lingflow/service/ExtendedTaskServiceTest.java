package com.lingflow.service;

import com.lingflow.dto.TaskVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
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
 * ExtendedTaskService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExtendedTaskServiceTest {

    @Mock
    private org.flowable.engine.TaskService flowableTaskService;

    @Mock
    private FlowableServiceTemplate serviceTemplate;

    @Mock
    private TaskQuery taskQuery;

    @InjectMocks
    private ExtendedTaskService extendedTaskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = mock(Task.class);
        when(task.getId()).thenReturn("task1");
        when(task.getName()).thenReturn("审批任务");
        when(task.getProcessInstanceId()).thenReturn("process1");
        when(task.getCreateTime()).thenReturn(new Date());
        when(task.getAssignee()).thenReturn("user1");
    }

    @Test
    void testCreateTaskQuery_Success() {
        when(serviceTemplate.execute(eq("TaskService.createTaskQuery"), any())).thenReturn(taskQuery);

        TaskQuery result = extendedTaskService.createTaskQuery();

        assertNotNull(result);
        verify(serviceTemplate, times(1)).execute(eq("TaskService.createTaskQuery"), any());
    }

    @Test
    void testGetTasks_Success() {
        List<Task> tasks = List.of(task);

        when(serviceTemplate.execute(eq("TaskService.createTaskQuery"), any())).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(tasks);

        List<TaskVO> result = extendedTaskService.getTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getId());
        assertEquals("审批任务", result.get(0).getName());
    }

    @Test
    void testGetTasks_Empty() {
        when(serviceTemplate.execute(eq("TaskService.createTaskQuery"), any())).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(new ArrayList<>());

        List<TaskVO> result = extendedTaskService.getTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTasksByAssignee_Success() {
        List<Task> tasks = List.of(task);

        when(serviceTemplate.execute(eq("TaskService.createTaskQuery"), any())).thenReturn(taskQuery);
        when(taskQuery.taskAssignee("user1")).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(tasks);

        List<TaskVO> result = extendedTaskService.getTasksByAssignee("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskQuery, times(1)).taskAssignee("user1");
    }

    @Test
    void testGetTasksByProcessInstanceId_Success() {
        List<Task> tasks = List.of(task);

        when(serviceTemplate.execute(eq("TaskService.createTaskQuery"), any())).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process1")).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(tasks);

        List<TaskVO> result = extendedTaskService.getTasksByProcessInstanceId("process1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskQuery, times(1)).processInstanceId("process1");
    }

    @Test
    void testGetTask_Success() {
        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task1")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);

        TaskVO result = extendedTaskService.getTask("task1");

        assertNotNull(result);
        assertEquals("task1", result.getId());
        assertEquals("审批任务", result.getName());
    }

    @Test
    void testGetTask_NotFound() {
        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("invalid-task")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        TaskVO result = extendedTaskService.getTask("invalid-task");

        assertNull(result);
    }

    @Test
    void testCompleteTask_WithVariables() {
        Map<String, Object> variables = Map.of("approved", true);

        when(serviceTemplate.execute(eq("TaskService.completeTask"), any(), eq("task1"), eq(variables)))
                .thenReturn(null);

        extendedTaskService.completeTask("task1", variables);

        verify(serviceTemplate, times(1)).execute(eq("TaskService.completeTask"), any(), eq("task1"), eq(variables));
    }

    @Test
    void testCompleteTask_WithoutVariables() {
        when(serviceTemplate.execute(eq("TaskService.completeTask"), any(), eq("task1"), isNull()))
                .thenReturn(null);

        extendedTaskService.completeTask("task1", null);

        verify(serviceTemplate, times(1)).execute(eq("TaskService.completeTask"), any(), eq("task1"), isNull());
    }

    @Test
    void testClaim_Success() {
        when(serviceTemplate.execute(eq("TaskService.claim"), any(), eq("task1"), eq("user1")))
                .thenReturn(null);

        extendedTaskService.claim("task1", "user1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.claim"), any(), eq("task1"), eq("user1"));
    }

    @Test
    void testUnclaim_Success() {
        when(serviceTemplate.execute(eq("TaskService.unclaim"), any(), eq("task1")))
                .thenReturn(null);

        extendedTaskService.unclaim("task1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.unclaim"), any(), eq("task1"));
    }

    @Test
    void testDelegateTask_Success() {
        when(serviceTemplate.execute(eq("TaskService.delegateTask"), any(), eq("task1"), eq("user2")))
                .thenReturn(null);

        extendedTaskService.delegateTask("task1", "user2");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.delegateTask"), any(), eq("task1"), eq("user2"));
    }

    @Test
    void testSetAssignee_Success() {
        when(serviceTemplate.execute(eq("TaskService.setAssignee"), any(), eq("task1"), eq("user2")))
                .thenReturn(null);

        extendedTaskService.setAssignee("task1", "user2");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.setAssignee"), any(), eq("task1"), eq("user2"));
    }

    @Test
    void testAddCandidateUser_Success() {
        when(serviceTemplate.execute(eq("TaskService.addCandidateUser"), any(), eq("task1"), eq("user1")))
                .thenReturn(null);

        extendedTaskService.addCandidateUser("task1", "user1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.addCandidateUser"), any(), eq("task1"), eq("user1"));
    }

    @Test
    void testAddCandidateGroup_Success() {
        when(serviceTemplate.execute(eq("TaskService.addCandidateGroup"), any(), eq("task1"), eq("group1")))
                .thenReturn(null);

        extendedTaskService.addCandidateGroup("task1", "group1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.addCandidateGroup"), any(), eq("task1"), eq("group1"));
    }

    @Test
    void testDeleteCandidateUser_Success() {
        when(serviceTemplate.execute(eq("TaskService.deleteCandidateUser"), any(), eq("task1"), eq("user1")))
                .thenReturn(null);

        extendedTaskService.deleteCandidateUser("task1", "user1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.deleteCandidateUser"), any(), eq("task1"), eq("user1"));
    }

    @Test
    void testDeleteCandidateGroup_Success() {
        when(serviceTemplate.execute(eq("TaskService.deleteCandidateGroup"), any(), eq("task1"), eq("group1")))
                .thenReturn(null);

        extendedTaskService.deleteCandidateGroup("task1", "group1");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.deleteCandidateGroup"), any(), eq("task1"), eq("group1"));
    }

    @Test
    void testAddComment_Success() {
        when(serviceTemplate.execute(eq("TaskService.addComment"), any(), eq("task1"), eq("process1"), eq("测试评论")))
                .thenReturn(null);

        extendedTaskService.addComment("task1", "process1", "测试评论");

        verify(serviceTemplate, times(1)).execute(eq("TaskService.addComment"), any(), eq("task1"), eq("process1"), eq("测试评论"));
    }

    @Test
    void testSetVariables_Success() {
        Map<String, Object> variables = Map.of("key", "value");

        when(serviceTemplate.execute(eq("TaskService.setVariables"), any(), eq("task1"), eq(variables)))
                .thenReturn(null);

        extendedTaskService.setVariables("task1", variables);

        verify(serviceTemplate, times(1)).execute(eq("TaskService.setVariables"), any(), eq("task1"), eq(variables));
    }
}
