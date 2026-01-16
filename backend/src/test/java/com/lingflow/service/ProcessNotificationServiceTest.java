package com.lingflow.service;

import com.lingflow.entity.NotificationRecord;
import com.lingflow.mapper.NotificationRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProcessNotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessNotificationServiceTest {

    @Mock
    private ExtendedTaskService extendedTaskService;

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @InjectMocks
    private ProcessNotificationService notificationService;

    @Test
    void testSendTaskAssignedNotification_Success() {
        doAnswer(invocation -> {
            NotificationRecord record = invocation.getArgument(0);
            record.setId(1L);
            return null;
        }).when(notificationRecordMapper).insert(any(NotificationRecord.class));

        String result = notificationService.sendTaskAssignedNotification("task1", "user1", "审批任务");

        assertNotNull(result);
        verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
    }

    @Test
    void testSendTaskCompletedNotification_Success() {
        doAnswer(invocation -> {
            NotificationRecord record = invocation.getArgument(0);
            record.setId(2L);
            return null;
        }).when(notificationRecordMapper).insert(any(NotificationRecord.class));

        String result = notificationService.sendTaskCompletedNotification(
                "task1", "user1", "审批任务", List.of("user2", "user3"));

        assertNotNull(result);
        verify(notificationRecordMapper, atLeastOnce()).insert(any(NotificationRecord.class));
    }

    @Test
    void testSendProcessApprovedNotification_Success() {
        doAnswer(invocation -> {
            NotificationRecord record = invocation.getArgument(0);
            record.setId(3L);
            return null;
        }).when(notificationRecordMapper).insert(any(NotificationRecord.class));

        String result = notificationService.sendProcessApprovedNotification(
                "process1", "user1", "测试流程");

        assertNotNull(result);
        verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
    }

    @Test
    void testGetUserNotifications_Success() {
        List<NotificationRecord> records = new ArrayList<>();
        NotificationRecord record = new NotificationRecord();
        record.setId(1L);
        record.setNotificationId("notification1");
        record.setRecipientId("user1");
        record.setTitle("测试通知");
        record.setIsRead(false);
        records.add(record);

        when(notificationRecordMapper.findByRecipientIdWithLimit("user1", 20)).thenReturn(records);

        List<ProcessNotificationService.NotificationRecordVO> result =
                notificationService.getUserNotifications("user1", 20);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRecordMapper, times(1)).findByRecipientIdWithLimit("user1", 20);
    }
}
