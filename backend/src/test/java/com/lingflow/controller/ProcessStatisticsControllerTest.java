package com.lingflow.controller;

import com.lingflow.service.ProcessStatisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessStatisticsController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessStatisticsControllerTest {

    @Mock
    private ProcessStatisticsService statisticsService;

    @InjectMocks
    private ProcessStatisticsController controller;

    private MockMvc mockMvc;

    @Test
    void testGetProcessInstanceStatistics_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ProcessStatisticsService.ProcessInstanceStatistics statistics =
                mock(ProcessStatisticsService.ProcessInstanceStatistics.class);

        when(statisticsService.getProcessInstanceStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/statistics/process-instance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).getProcessInstanceStatistics();
    }

    @Test
    void testGetTaskStatistics_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ProcessStatisticsService.TaskStatistics statistics =
                mock(ProcessStatisticsService.TaskStatistics.class);

        when(statisticsService.getTaskStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/statistics/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).getTaskStatistics();
    }

    @Test
    void testGetProcessDefinitionStatistics_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(statisticsService.getProcessDefinitionStatistics()).thenReturn(new java.util.ArrayList<>());

        mockMvc.perform(get("/api/statistics/process-definition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(statisticsService, times(1)).getProcessDefinitionStatistics();
    }

    @Test
    void testGetUserTaskStatistics_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ProcessStatisticsService.UserTaskStatistics statistics =
                mock(ProcessStatisticsService.UserTaskStatistics.class);

        when(statisticsService.getUserTaskStatistics("user1")).thenReturn(statistics);

        mockMvc.perform(get("/api/statistics/user-task/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).getUserTaskStatistics("user1");
    }
}
