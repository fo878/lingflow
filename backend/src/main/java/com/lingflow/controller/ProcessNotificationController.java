package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.service.ProcessNotificationService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程通知控制器
 * 提供流程通知相关的API
 */
@RestController
@RequestMapping("/api/notifications")
public class ProcessNotificationController {

    @Autowired
    private ProcessNotificationService notificationService;

    /**
     * 发送任务分配通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/task-assigned")
    public Result<String> sendTaskAssignedNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendTaskAssignedNotification(
                request.getTaskId(),
                request.getAssignee(),
                request.getTaskName()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送任务完成通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/task-completed")
    public Result<String> sendTaskCompletedNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendTaskCompletedNotification(
                request.getTaskId(),
                request.getAssignee(),
                request.getTaskName(),
                request.getNextAssignees()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送流程超时通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/process-timeout")
    public Result<String> sendProcessTimeoutNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendProcessTimeoutNotification(
                request.getProcessInstanceId(),
                request.getAssignee(),
                request.getTaskName(),
                request.getTimeoutHours()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送流程拒绝通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/process-rejected")
    public Result<String> sendProcessRejectedNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendProcessRejectedNotification(
                request.getProcessInstanceId(),
                request.getInitiator(),
                request.getProcessName(),
                request.getReason()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送流程通过通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/process-approved")
    public Result<String> sendProcessApprovedNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendProcessApprovedNotification(
                request.getProcessInstanceId(),
                request.getInitiator(),
                request.getProcessName()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送流程撤回通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/process-withdrawn")
    public Result<String> sendProcessWithdrawnNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendProcessWithdrawnNotification(
                request.getProcessInstanceId(),
                request.getInitiator(),
                request.getProcessName(),
                request.getReason()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送任务转办通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/task-delegated")
    public Result<String> sendTaskDelegatedNotification(@RequestBody NotificationRequest request) {
        try {
            String notificationId = notificationService.sendTaskDelegatedNotification(
                request.getTaskId(),
                request.getFromUser(),
                request.getToUser(),
                request.getTaskName()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发送自定义通知
     *
     * @param request 请求参数
     * @return 通知ID
     */
    @PostMapping("/custom")
    public Result<String> sendCustomNotification(@RequestBody CustomNotificationRequest request) {
        try {
            String notificationId = notificationService.sendCustomNotification(
                request.getRecipients(),
                request.getTitle(),
                request.getContent(),
                request.getProcessInstanceId()
            );
            return Result.success(notificationId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的通知列表
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 通知列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ProcessNotificationService.NotificationRecordVO>> getUserNotifications(
            @PathVariable("userId") String userId,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        try {
            List<ProcessNotificationService.NotificationRecordVO> notifications =
                notificationService.getUserNotifications(userId, limit);
            return Result.success(notifications);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public Result<Long> getUnreadNotificationCount(@PathVariable("userId") String userId) {
        try {
            Long count = notificationService.getUnreadNotificationCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     *
     * @param userId         用户ID
     * @param notificationId 通知ID
     * @return 是否成功
     */
    @PostMapping("/user/{userId}/read/{notificationId}")
    public Result<Boolean> markNotificationAsRead(
            @PathVariable("userId") String userId,
            @PathVariable("notificationId") String notificationId) {
        try {
            boolean success = notificationService.markNotificationAsRead(userId, notificationId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量标记通知为已读
     *
     * @param userId 用户ID
     * @return 成功标记的数量
     */
    @PostMapping("/user/{userId}/read-all")
    public Result<Integer> markAllNotificationsAsRead(@PathVariable("userId") String userId) {
        try {
            int count = notificationService.markAllNotificationsAsRead(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== VO 类定义 ====================

    /**
     * 通知请求参数
     */
    @Data
    public static class NotificationRequest {
        private String taskId;
        private String processInstanceId;
        private String assignee;
        private String initiator;
        private String fromUser;
        private String toUser;
        private String taskName;
        private String processName;
        private String reason;
        private Long timeoutHours;
        private List<String> nextAssignees;
    }

    /**
     * 自定义通知请求参数
     */
    @Data
    public static class CustomNotificationRequest {
        /**
         * 接收人列表
         */
        private List<String> recipients;

        /**
         * 通知标题
         */
        private String title;

        /**
         * 通知内容
         */
        private String content;

        /**
         * 流程实例ID（可选）
         */
        private String processInstanceId;
    }
}
