package com.lingflow.service;

import com.lingflow.entity.NotificationRecord;
import com.lingflow.mapper.NotificationRecordMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 流程通知服务
 * 管理流程相关的通知发送和记录
 */
@Slf4j
@Service
public class ProcessNotificationService {

    @Autowired
    private ExtendedTaskService extendedTaskService;

    @Autowired
    private NotificationRecordMapper notificationRecordMapper;

    /**
     * 发送任务分配通知
     *
     * @param taskId 任务ID
     * @param assignee 受理人
     * @param taskName 任务名称
     * @return 通知ID
     */
    @Transactional
    public String sendTaskAssignedNotification(
        String taskId,
        String assignee,
        String taskName
    ) {
        log.info("发送任务分配通知 - 任务ID: {}, 受理人: {}, 任务名称: {}",
            taskId, assignee, taskName);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.TASK_ASSIGNED);
        message.setRecipient(assignee);
        message.setTitle("新任务分配");
        message.setContent(String.format("您有一个新的待办任务：%s", taskName));
        message.setTaskId(taskId);
        message.setTaskName(taskName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送任务完成通知
     *
     * @param taskId 任务ID
     * @param assignee 受理人
     * @param taskName 任务名称
     * @param nextAssignees 下一环节受理人列表
     * @return 通知ID列表
     */
    @Transactional
    public String sendTaskCompletedNotification(
        String taskId,
        String assignee,
        String taskName,
        List<String> nextAssignees
    ) {
        log.info("发送任务完成通知 - 任务ID: {}, 受理人: {}, 任务名称: {}",
            taskId, assignee, taskName);

        String notificationId = null;

        // 通知发起人任务已完成
        if (nextAssignees != null && !nextAssignees.isEmpty()) {
            for (String nextAssignee : nextAssignees) {
                NotificationMessage message = new NotificationMessage();
                message.setType(NotificationType.TASK_COMPLETED);
                message.setRecipient(nextAssignee);
                message.setTitle("前置任务已完成");
                message.setContent(String.format("任务【%s】已完成，等待您处理", taskName));
                message.setTaskId(taskId);
                message.setTaskName(taskName);
                message.setSendTime(LocalDateTime.now());

                sendNotification(message);
                notificationId = recordNotification(message, true);
            }
        }
        return notificationId;
    }

    /**
     * 发送流程超时通知
     *
     * @param processInstanceId 流程实例ID
     * @param assignee 受理人
     * @param taskName 任务名称
     * @param timeoutHours 超时小时数
     * @return 通知ID
     */
    @Transactional
    public String sendProcessTimeoutNotification(
        String processInstanceId,
        String assignee,
        String taskName,
        Long timeoutHours
    ) {
        log.info("发送流程超时通知 - 流程实例ID: {}, 受理人: {}, 任务名称: {}, 超时: {}小时",
            processInstanceId, assignee, taskName, timeoutHours);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.PROCESS_TIMEOUT);
        message.setRecipient(assignee);
        message.setTitle("流程超时提醒");
        message.setContent(String.format("您的任务【%s】已超时%d小时，请尽快处理", taskName, timeoutHours));
        message.setProcessInstanceId(processInstanceId);
        message.setTaskName(taskName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送流程拒绝通知
     *
     * @param processInstanceId 流程实例ID
     * @param initiator 发起人
     * @param processName 流程名称
     * @param reason 拒绝原因
     * @return 通知ID
     */
    @Transactional
    public String sendProcessRejectedNotification(
        String processInstanceId,
        String initiator,
        String processName,
        String reason
    ) {
        log.info("发送流程拒绝通知 - 流程实例ID: {}, 发起人: {}, 流程名称: {}",
            processInstanceId, initiator, processName);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.PROCESS_REJECTED);
        message.setRecipient(initiator);
        message.setTitle("流程已被拒绝");
        message.setContent(String.format("您的流程【%s】已被拒绝。原因：%s", processName, reason));
        message.setProcessInstanceId(processInstanceId);
        message.setProcessName(processName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送流程通过通知
     *
     * @param processInstanceId 流程实例ID
     * @param initiator 发起人
     * @param processName 流程名称
     * @return 通知ID
     */
    @Transactional
    public String sendProcessApprovedNotification(
        String processInstanceId,
        String initiator,
        String processName
    ) {
        log.info("发送流程通过通知 - 流程实例ID: {}, 发起人: {}, 流程名称: {}",
            processInstanceId, initiator, processName);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.PROCESS_APPROVED);
        message.setRecipient(initiator);
        message.setTitle("流程已通过");
        message.setContent(String.format("恭喜！您的流程【%s】已审批通过", processName));
        message.setProcessInstanceId(processInstanceId);
        message.setProcessName(processName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送流程撤回通知
     *
     * @param processInstanceId 流程实例ID
     * @param initiator 发起人
     * @param processName 流程名称
     * @param reason 撤回原因
     * @return 通知ID
     */
    @Transactional
    public String sendProcessWithdrawnNotification(
        String processInstanceId,
        String initiator,
        String processName,
        String reason
    ) {
        log.info("发送流程撤回通知 - 流程实例ID: {}, 发起人: {}, 流程名称: {}",
            processInstanceId, initiator, processName);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.PROCESS_WITHDRAWN);
        message.setRecipient(initiator);
        message.setTitle("流程已撤回");
        message.setContent(String.format("您的流程【%s】已撤回。原因：%s", processName, reason));
        message.setProcessInstanceId(processInstanceId);
        message.setProcessName(processName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送任务转办通知
     *
     * @param taskId 任务ID
     * @param fromUser 转办人
     * @param toUser 被转办人
     * @param taskName 任务名称
     * @return 通知ID
     */
    @Transactional
    public String sendTaskDelegatedNotification(
        String taskId,
        String fromUser,
        String toUser,
        String taskName
    ) {
        log.info("发送任务转办通知 - 任务ID: {}, 转办人: {}, 被转办人: {}, 任务名称: {}",
            taskId, fromUser, toUser, taskName);

        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationType.TASK_DELEGATED);
        message.setRecipient(toUser);
        message.setTitle("任务已转办");
        message.setContent(String.format("%s将任务【%s】转办给您", fromUser, taskName));
        message.setTaskId(taskId);
        message.setTaskName(taskName);
        message.setSendTime(LocalDateTime.now());

        sendNotification(message);
        return recordNotification(message, true);
    }

    /**
     * 发送自定义通知
     *
     * @param recipients 接收人列表
     * @param title 通知标题
     * @param content 通知内容
     * @param processInstanceId 流程实例ID（可选）
     * @return 通知ID
     */
    @Transactional
    public String sendCustomNotification(
        List<String> recipients,
        String title,
        String content,
        String processInstanceId
    ) {
        log.info("发送自定义通知 - 接收人: {}, 标题: {}, 内容: {}",
            recipients, title, content);

        String notificationId = null;

        for (String recipient : recipients) {
            NotificationMessage message = new NotificationMessage();
            message.setType(NotificationType.CUSTOM);
            message.setRecipient(recipient);
            message.setTitle(title);
            message.setContent(content);
            message.setProcessInstanceId(processInstanceId);
            message.setSendTime(LocalDateTime.now());

            sendNotification(message);
            notificationId = recordNotification(message, true);
        }
        return notificationId;
    }

    /**
     * 获取用户的通知列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 通知列表
     */
    public List<NotificationRecordVO> getUserNotifications(String userId, int limit) {
        log.info("获取用户通知列表 - 用户ID: {}, 限制数量: {}", userId, limit);

        List<com.lingflow.entity.NotificationRecord> records =
            notificationRecordMapper.findByRecipientIdWithLimit(userId, limit);

        List<NotificationRecordVO> result = new ArrayList<>();
        for (com.lingflow.entity.NotificationRecord record : records) {
            NotificationRecordVO vo = new NotificationRecordVO();
            vo.setId(record.getNotificationId());
            vo.setType(record.getType());
            vo.setRecipientId(record.getRecipientId());
            vo.setTitle(record.getTitle());
            vo.setContent(record.getContent());
            vo.setProcessInstanceId(record.getProcessInstanceId());
            vo.setTaskId(record.getTaskId());
            vo.setRead(record.getIsRead());
            vo.setCreateTime(formatDateTime(record.getCreateTime()));
            vo.setReadTime(formatDateTime(record.getReadTime()));
            result.add(vo);
        }

        return result;
    }

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public Long getUnreadNotificationCount(String userId) {
        log.info("获取未读通知数量 - 用户ID: {}", userId);

        return notificationRecordMapper.countByRecipientIdAndIsRead(userId, false);
    }

    /**
     * 标记通知为已读
     *
     * @param userId 用户ID
     * @param notificationId 通知ID
     * @return 是否成功
     */
    @Transactional
    public boolean markNotificationAsRead(String userId, String notificationId) {
        log.info("标记通知为已读 - 用户ID: {}, 通知ID: {}", userId, notificationId);

        try {
            int updated = notificationRecordMapper.markAsRead(notificationId, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return false;
        }
    }

    /**
     * 批量标记通知为已读
     *
     * @param userId 用户ID
     * @return 标记的数量
     */
    @Transactional
    public int markAllNotificationsAsRead(String userId) {
        log.info("批量标记通知为已读 - 用户ID: {}", userId);

        try {
            return notificationRecordMapper.markAllAsReadForUser(userId, LocalDateTime.now());
        } catch (Exception e) {
            log.error("批量标记通知为已读失败", e);
            return 0;
        }
    }

    /**
     * 发送通知
     *
     * @param message 通知消息
     */
    private void sendNotification(NotificationMessage message) {
        // 这里集成实际的通知渠道（邮件、短信、企业微信等）
        log.info("发送通知: 类型={}, 接收人={}, 标题={}",
            message.getType(), message.getRecipient(), message.getTitle());

        // TODO: 实现实际的通知发送逻辑
        // 1. 邮件通知
        // 2. 短信通知
        // 3. 企业微信通知
        // 4. 钉钉通知
        // 5. WebSocket实时推送
    }

    /**
     * 记录通知
     *
     * @param message 通知消息
     * @param success 是否成功
     * @return 通知ID
     */
    private String recordNotification(NotificationMessage message, boolean success) {
        com.lingflow.entity.NotificationRecord record =
            new com.lingflow.entity.NotificationRecord(
                message.getType().name(),
                message.getRecipient(),
                message.getTitle(),
                message.getContent()
            );

        record.setProcessInstanceId(message.getProcessInstanceId());
        record.setTaskId(message.getTaskId());
        record.setTaskName(message.getTaskName());
        record.setProcessName(message.getProcessName());

        notificationRecordMapper.insert(record);
        com.lingflow.entity.NotificationRecord saved = record;

        return saved.getNotificationId();
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toString();
    }

    /**
     * 通知消息
     */
    @Data
    public static class NotificationMessage {
        private NotificationType type;
        private String recipient;
        private String title;
        private String content;
        private String processInstanceId;
        private String processName;
        private String taskId;
        private String taskName;
        private LocalDateTime sendTime;
    }

    /**
     * 通知记录VO
     */
    @Data
    public static class NotificationRecordVO {
        private String id;
        private String type;
        private String recipientId;
        private String title;
        private String content;
        private String processInstanceId;
        private String taskId;
        private Boolean read;
        private String createTime;
        private String readTime;
    }

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        TASK_ASSIGNED,      // 任务分配
        TASK_COMPLETED,     // 任务完成
        TASK_DELEGATED,     // 任务转办
        PROCESS_TIMEOUT,    // 流程超时
        PROCESS_APPROVED,   // 流程通过
        PROCESS_REJECTED,   // 流程拒绝
        PROCESS_WITHDRAWN,  // 流程撤回
        CUSTOM              // 自定义
    }
}
