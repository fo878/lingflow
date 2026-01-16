package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.dto.CreateSnapshotRequest;
import com.lingflow.entity.ProcessSnapshot;
import com.lingflow.service.ProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SnapshotController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SnapshotControllerTest {

    @Mock
    private ProcessService processService;

    @InjectMocks
    private SnapshotController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateSnapshot_Success() throws Exception {
        doNothing().when(processService).createProcessSnapshot(
                anyString(), anyString(), anyString(), anyString());

        CreateSnapshotRequest request = new CreateSnapshotRequest();
        request.setProcessDefinitionKey("testProcess");
        request.setSnapshotName("v1.0");
        request.setDescription("初始版本");
        request.setCreator("admin");

        mockMvc.perform(post("/snapshot/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).createProcessSnapshot(
                "testProcess", "v1.0", "初始版本", "admin");
    }

    @Test
    void testCreateSnapshot_Exception() throws Exception {
        doThrow(new RuntimeException("快照名称已存在"))
                .when(processService).createProcessSnapshot(
                        anyString(), anyString(), anyString(), anyString());

        CreateSnapshotRequest request = new CreateSnapshotRequest();
        request.setProcessDefinitionKey("testProcess");
        request.setSnapshotName("v1.0");

        mockMvc.perform(post("/snapshot/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("快照名称已存在"));
    }

    @Test
    void testGetSnapshots_Success() throws Exception {
        List<ProcessSnapshot> snapshots = new ArrayList<>();
        ProcessSnapshot snapshot = new ProcessSnapshot();
        snapshot.setId("snapshot1");
        snapshot.setSnapshotName("v1.0");
        snapshot.setSnapshotVersion(1);
        snapshot.setCreator("admin");
        snapshot.setCreatedTime(LocalDateTime.now());
        snapshots.add(snapshot);

        when(processService.getProcessSnapshots("testProcess")).thenReturn(snapshots);

        mockMvc.perform(get("/snapshot/list/testProcess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].snapshotName").value("v1.0"))
                .andExpect(jsonPath("$.data[0].snapshotVersion").value(1));

        verify(processService, times(1)).getProcessSnapshots("testProcess");
    }

    @Test
    void testGetSnapshots_Empty() throws Exception {
        List<ProcessSnapshot> snapshots = new ArrayList<>();
        when(processService.getProcessSnapshots("testProcess")).thenReturn(snapshots);

        mockMvc.perform(get("/snapshot/list/testProcess"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(processService, times(1)).getProcessSnapshots("testProcess");
    }

    @Test
    void testRollbackToSnapshot_Success() throws Exception {
        doNothing().when(processService).rollbackToSnapshot(anyString());

        mockMvc.perform(post("/snapshot/rollback/snapshot1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).rollbackToSnapshot("snapshot1");
    }

    @Test
    void testRollbackToSnapshot_NotFound() throws Exception {
        doThrow(new RuntimeException("快照不存在"))
                .when(processService).rollbackToSnapshot(anyString());

        mockMvc.perform(post("/snapshot/rollback/invalid-snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("快照不存在"));

        verify(processService, times(1)).rollbackToSnapshot("invalid-snapshot");
    }

    @Test
    void testDeleteSnapshot_Success() throws Exception {
        doNothing().when(processService).deleteSnapshot(anyString());

        mockMvc.perform(delete("/snapshot/snapshot1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).deleteSnapshot("snapshot1");
    }

    @Test
    void testDeleteSnapshot_Exception() throws Exception {
        doThrow(new RuntimeException("删除快照失败"))
                .when(processService).deleteSnapshot(anyString());

        mockMvc.perform(delete("/snapshot/invalid-snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除快照失败"));

        verify(processService, times(1)).deleteSnapshot("invalid-snapshot");
    }
}
