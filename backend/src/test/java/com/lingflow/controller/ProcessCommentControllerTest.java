package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.service.ProcessCommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessCommentController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessCommentControllerTest {

    @Mock
    private ProcessCommentService commentService;

    @InjectMocks
    private ProcessCommentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Test
    void testAddProcessComment_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        when(commentService.addProcessComment(anyString(), anyString(), anyString()))
                .thenReturn("comment1");

        ProcessCommentController.AddCommentRequest request =
                new ProcessCommentController.AddCommentRequest();
        request.setProcessInstanceId("process1");
        request.setUserId("user1");
        request.setContent("测试评论");

        mockMvc.perform(post("/api/comments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("comment1"));

        verify(commentService, times(1)).addProcessComment("process1", "user1", "测试评论");
    }

    @Test
    void testGetProcessComments_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        List<ProcessCommentService.Comment> comments = new ArrayList<>();

        when(commentService.getProcessComments("process1")).thenReturn(comments);

        mockMvc.perform(get("/api/comments/process/process1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(commentService, times(1)).getProcessComments("process1");
    }
}
