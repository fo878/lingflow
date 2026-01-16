package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.dto.TaskVO;
import com.lingflow.service.ProcessDefinitionService;
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
 * TaskController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetTasks_Success() throws Exception {
        // 准备测试数据
        List<TaskVO> tasks = new ArrayList<>();
        TaskVO task1 = new TaskVO();
        task1.setId("task1");
        task1.setName("审批任务");
        task1.setProcessInstanceId("process1");
        tasks.add(task1);

        when(processDefinitionService.getTasks()).thenReturn(tasks);

        // 执行测试
        mockMvc.perform(get("/task/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].id").value("task1"))
                .andExpect(jsonPath("$.data[0].name").value("审批任务"));

        verify(processDefinitionService, times(1)).getTasks();
    }

    @Test
    void testGetTasks_EmptyList() throws Exception {
        List<TaskVO> tasks = new ArrayList<>();
        when(processDefinitionService.getTasks()).thenReturn(tasks);

        mockMvc.perform(get("/task/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(processDefinitionService, times(1)).getTasks();
    }

    @Test
    void testGetTasks_Exception() throws Exception {
        when(processDefinitionService.getTasks()).thenThrow(new RuntimeException("数据库错误"));

        mockMvc.perform(get("/task/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("数据库错误"));

        verify(processDefinitionService, times(1)).getTasks();
    }

    @Test
    void testCompleteTask_Success() throws Exception {
        doNothing().when(processDefinitionService).completeTask(eq("task1"), anyMap());

        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        mockMvc.perform(post("/task/complete/task1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variables)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processDefinitionService, times(1)).completeTask(eq("task1"), anyMap());
    }

    @Test
    void testCompleteTask_NoVariables() throws Exception {
        doNothing().when(processDefinitionService).completeTask(eq("task1"), isNull());

        mockMvc.perform(post("/task/complete/task1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processDefinitionService, times(1)).completeTask(eq("task1"), isNull());
    }

    @Test
    void testCompleteTask_Exception() throws Exception {
        doThrow(new RuntimeException("任务不存在"))
                .when(processDefinitionService).completeTask(anyString(), anyMap());

        Map<String, Object> variables = new HashMap<>();

        mockMvc.perform(post("/task/complete/invalid-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variables)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("任务不存在"));

        verify(processDefinitionService, times(1)).completeTask(eq("invalid-task"), anyMap());
    }

    @Test
    void testGetTaskForm_Success() throws Exception {
        mockMvc.perform(get("/task/form/task1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value("task1"));
    }

    @Test
    void testGetTaskForm_Exception() throws Exception {
        mockMvc.perform(get("/task/form/invalid-task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value("invalid-task"));
    }
}
