package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.service.ProcessNotificationService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessNotificationController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessNotificationControllerTest {

    @Mock
    private ProcessNotificationService notificationService;

    @InjectMocks
    private ProcessNotificationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSendTaskAssignedNotification_Success() throws Exception {
        when(notificationService.sendTaskAssignedNotification(anyString(), anyString(), anyString()))
                .thenReturn("notification1");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setTaskId("task1");
        request.setAssignee("user1");
        request.setTaskName("审批任务");

        mockMvc.perform(post("/api/notifications/task-assigned")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("notification1"));

        verify(notificationService, times(1)).sendTaskAssignedNotification("task1", "user1", "审批任务");
    }

    @Test
    void testSendTaskCompletedNotification_Success() throws Exception {
        when(notificationService.sendTaskCompletedNotification(
                anyString(), anyString(), anyString(), anyList()))
                .thenReturn("notification2");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setTaskId("task1");
        request.setAssignee("user1");
        request.setTaskName("审批任务");
        request.setNextAssignees(List.of("user2", "user3"));

        mockMvc.perform(post("/api/notifications/task-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("notification2"));

        verify(notificationService, times(1)).sendTaskCompletedNotification(
                "task1", "user1", "审批任务", List.of("user2", "user3"));
    }

    @Test
    void testSendProcessTimeoutNotification_Success() throws Exception {
        when(notificationService.sendProcessTimeoutNotification(
                anyString(), anyString(), anyString(), anyLong()))
                .thenReturn("notification3");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setProcessInstanceId("process1");
        request.setAssignee("user1");
        request.setTaskName("审批任务");
        request.setTimeoutHours(24L);

        mockMvc.perform(post("/api/notifications/process-timeout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService, times(1)).sendProcessTimeoutNotification(
                "process1", "user1", "审批任务", 24L);
    }

    @Test
    void testSendProcessRejectedNotification_Success() throws Exception {
        when(notificationService.sendProcessRejectedNotification(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn("notification4");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setProcessInstanceId("process1");
        request.setInitiator("user1");
        request.setProcessName("测试流程");
        request.setReason("不符合要求");

        mockMvc.perform(post("/api/notifications/process-rejected")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService, times(1)).sendProcessRejectedNotification(
                "process1", "user1", "测试流程", "不符合要求");
    }

    @Test
    void testSendProcessApprovedNotification_Success() throws Exception {
        when(notificationService.sendProcessApprovedNotification(
                anyString(), anyString(), anyString()))
                .thenReturn("notification5");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setProcessInstanceId("process1");
        request.setInitiator("user1");
        request.setProcessName("测试流程");

        mockMvc.perform(post("/api/notifications/process-approved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService, times(1)).sendProcessApprovedNotification(
                "process1", "user1", "测试流程");
    }

    @Test
    void testSendTaskDelegatedNotification_Success() throws Exception {
        when(notificationService.sendTaskDelegatedNotification(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn("notification6");

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setTaskId("task1");
        request.setFromUser("user1");
        request.setToUser("user2");
        request.setTaskName("审批任务");

        mockMvc.perform(post("/api/notifications/task-delegated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService, times(1)).sendTaskDelegatedNotification(
                "task1", "user1", "user2", "审批任务");
    }

    @Test
    void testSendCustomNotification_Success() throws Exception {
        when(notificationService.sendCustomNotification(
                anyList(), anyString(), anyString(), anyString()))
                .thenReturn("notification7");

        ProcessNotificationController.CustomNotificationRequest request =
                new ProcessNotificationController.CustomNotificationRequest();
        request.setRecipients(List.of("user1", "user2"));
        request.setTitle("自定义通知");
        request.setContent("这是一条自定义通知");
        request.setProcessInstanceId("process1");

        mockMvc.perform(post("/api/notifications/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(notificationService, times(1)).sendCustomNotification(
                List.of("user1", "user2"), "自定义通知", "这是一条自定义通知", "process1");
    }

    @Test
    void testGetUserNotifications_Success() throws Exception {
        List<ProcessNotificationService.NotificationRecordVO> notifications = new ArrayList<>();
        ProcessNotificationService.NotificationRecordVO notification =
                new ProcessNotificationService.NotificationRecordVO();
        notification.setId("notification1");
        notification.setTitle("测试通知");
        notifications.add(notification);

        when(notificationService.getUserNotifications("user1", 20)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("notification1"))
                .andExpect(jsonPath("$.data[0].title").value("测试通知"));

        verify(notificationService, times(1)).getUserNotifications("user1", 20);
    }

    @Test
    void testGetUnreadNotificationCount_Success() throws Exception {
        when(notificationService.getUnreadNotificationCount("user1")).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/user/user1/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService, times(1)).getUnreadNotificationCount("user1");
    }

    @Test
    void testMarkNotificationAsRead_Success() throws Exception {
        when(notificationService.markNotificationAsRead("user1", "notification1"))
                .thenReturn(true);

        mockMvc.perform(post("/api/notifications/user/user1/read/notification1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(notificationService, times(1)).markNotificationAsRead("user1", "notification1");
    }

    @Test
    void testMarkAllNotificationsAsRead_Success() throws Exception {
        when(notificationService.markAllNotificationsAsRead("user1")).thenReturn(10);

        mockMvc.perform(post("/api/notifications/user/user1/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(10));

        verify(notificationService, times(1)).markAllNotificationsAsRead("user1");
    }

    @Test
    void testSendTaskAssignedNotification_Exception() throws Exception {
        when(notificationService.sendTaskAssignedNotification(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("发送失败"));

        ProcessNotificationController.NotificationRequest request =
                new ProcessNotificationController.NotificationRequest();
        request.setTaskId("task1");
        request.setAssignee("user1");

        mockMvc.perform(post("/api/notifications/task-assigned")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("发送失败"));

        verify(notificationService, times(1)).sendTaskAssignedNotification(anyString(), anyString(), anyString());
    }
}
