package com.lingflow.service;

import com.lingflow.entity.ProcessComment;
import com.lingflow.mapper.ProcessCommentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProcessCommentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessCommentServiceTest {

    @Mock
    private ExtendedTaskService extendedTaskService;

    @Mock
    private ProcessCommentMapper processCommentMapper;

    @InjectMocks
    private ProcessCommentService processCommentService;

    @Test
    void testAddProcessComment_Success() {
        doAnswer(invocation -> {
            ProcessComment comment = invocation.getArgument(0);
            comment.setCommentId("comment1");
            return null;
        }).when(processCommentMapper).insert(any(ProcessComment.class));

        String result = processCommentService.addProcessComment("process1", "user1", "测试评论");

        assertNotNull(result);
        verify(processCommentMapper, times(1)).insert(any(ProcessComment.class));
    }

    @Test
    void testAddTaskComment_Success() {
        doAnswer(invocation -> {
            ProcessComment comment = invocation.getArgument(0);
            comment.setCommentId("comment2");
            return null;
        }).when(processCommentMapper).insert(any(ProcessComment.class));

        String result = processCommentService.addTaskComment("task1", "user1", "任务评论");

        assertNotNull(result);
        verify(processCommentMapper, times(1)).insert(any(ProcessComment.class));
    }

    @Test
    void testAddSystemComment_Success() {
        doAnswer(invocation -> {
            ProcessComment comment = invocation.getArgument(0);
            comment.setCommentId("comment3");
            return null;
        }).when(processCommentMapper).insert(any(ProcessComment.class));

        String result = processCommentService.addSystemComment("process1", "系统自动评论");

        assertNotNull(result);
        verify(processCommentMapper, times(1)).insert(any(ProcessComment.class));
    }

    @Test
    void testGetProcessComments_Success() {
        java.util.List<ProcessComment> comments = new java.util.ArrayList<>();
        ProcessComment comment = new ProcessComment();
        comment.setCommentId("comment1");
        comment.setProcessInstanceId("process1");
        comment.setUserId("user1");
        comment.setContent("测试评论");
        comments.add(comment);

        when(processCommentMapper.findByProcessInstanceId("process1")).thenReturn(comments);

        java.util.List<ProcessCommentService.Comment> result = processCommentService.getProcessComments("process1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processCommentMapper, times(1)).findByProcessInstanceId("process1");
    }

    @Test
    void testGetTaskComments_Success() {
        java.util.List<ProcessComment> comments = new java.util.ArrayList<>();
        ProcessComment comment = new ProcessComment();
        comment.setCommentId("comment1");
        comment.setTaskId("task1");
        comment.setUserId("user1");
        comment.setContent("任务评论");
        comments.add(comment);

        when(processCommentMapper.findByTaskId("task1")).thenReturn(comments);

        java.util.List<ProcessCommentService.Comment> result = processCommentService.getTaskComments("task1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processCommentMapper, times(1)).findByTaskId("task1");
    }
}
