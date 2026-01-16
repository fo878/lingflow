package com.lingflow.extension.event.impl;

import com.lingflow.extension.event.ProcessEvent;
import com.lingflow.extension.event.ProcessEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 任务分配事件监听器
 */
@Slf4j
@Component
public class TaskAssignedListener implements ProcessEventListener {

    @Override
    public void onBefore(ProcessEvent event) {
        String taskId = event.getTaskId();
        String assignee = event.getData("assignee", String.class);
        String taskName = event.getData("taskName", String.class);

        log.info("任务即将分配 - 任务ID: {}, 任务名称: {}, 办理人: {}",
            taskId, taskName, assignee);

        // 前置处理逻辑：
        // 1. 验证办理人有效性
        // 2. 检查办理人权限
        // 3. 验证办理人工作负载
        // 4. 记录分配前状态

        if (assignee != null && !assignee.isEmpty()) {
            // 验证办理人是否存在
            // 这里可以集成用户服务进行验证
            log.debug("验证办理人: {}", assignee);
        }

        // 检查是否需要通知
        Boolean notifyAssignee = event.getData("notifyAssignee", Boolean.class);
        if (notifyAssignee != null && notifyAssignee) {
            log.debug("分配后需要通知办理人");
        }
    }

    @Override
    public void onAfter(ProcessEvent event) {
        String taskId = event.getTaskId();
        String assignee = event.getData("assignee", String.class);
        String taskName = event.getData("taskName", String.class);
        String processInstanceId = event.getProcessInstanceId();

        log.info("任务已分配 - 任务ID: {}, 任务名称: {}, 办理人: {}, 流程实例ID: {}",
            taskId, taskName, assignee, processInstanceId);

        // 后置处理逻辑：
        // 1. 发送任务分配通知
        // 2. 更新办理人任务列表
        // 3. 记录分配审计日志
        // 4. 触发相关业务逻辑

        if (assignee != null && !assignee.isEmpty()) {
            // 发送通知给办理人
            sendAssignmentNotification(event);
        }

        // 检查是否需要设置任务到期时间
        Boolean dueDateSet = event.getData("dueDateSet", Boolean.class);
        if (dueDateSet != null && dueDateSet) {
            Object dueDate = event.getData("dueDate");
            log.info("任务到期时间: {}", dueDate);
        }

        // 记录分配时间
        Object assignmentTime = event.getData("assignmentTime");
        if (assignmentTime != null) {
            log.debug("任务分配时间: {}", assignmentTime);
        }

        // 更新办理人任务统计
        updateAssigneeTaskStats(event);
    }

    /**
     * 发送任务分配通知
     */
    private void sendAssignmentNotification(ProcessEvent event) {
        String assignee = event.getData("assignee", String.class);
        String taskName = event.getData("taskName", String.class);
        String processInstanceId = event.getProcessInstanceId();

        log.info("发送任务分配通知 - 接收人: {}, 任务: {}, 流程实例: {}",
            assignee, taskName, processInstanceId);

        // 这里可以集成消息通知服务
        // 例如：
        // - 邮件通知
        // - 站内消息
        // - 短信通知
        // - 企业微信/钉钉通知

        // 通知内容
        String notificationContent = String.format(
            "您有一个新的待办任务：%s (流程实例：%s)",
            taskName, processInstanceId
        );

        log.debug("通知内容: {}", notificationContent);

        // 设置通知已发送标记
        event.getData().put("assignmentNotificationSent", true);
    }

    /**
     * 更新办理人任务统计
     */
    private void updateAssigneeTaskStats(ProcessEvent event) {
        String assignee = event.getData("assignee", String.class);

        log.debug("更新办理人任务统计 - 办理人: {}", assignee);

        // 这里可以更新办理人的任务统计信息
        // 例如：
        // - 待办任务数量
        // - 已办任务数量
        // - 超时任务数量
        // - 平均处理时间

        event.getData().put("assigneeStatsUpdated", true);
    }

    /**
     * 处理任务重新分配
     */
    private void handleReassignment(ProcessEvent event) {
        String oldAssignee = event.getData("oldAssignee", String.class);
        String newAssignee = event.getData("assignee", String.class);

        if (oldAssignee != null && !oldAssignee.equals(newAssignee)) {
            log.info("任务重新分配 - 原办理人: {}, 新办理人: {}", oldAssignee, newAssignee);

            // 发送重新分配通知
            sendReassignmentNotification(event);
        }
    }

    /**
     * 发送重新分配通知
     */
    private void sendReassignmentNotification(ProcessEvent event) {
        String oldAssignee = event.getData("oldAssignee", String.class);
        String newAssignee = event.getData("assignee", String.class);
        String taskName = event.getData("taskName", String.class);

        log.info("发送任务重新分配通知 - 从: {} 到: {}, 任务: {}",
            oldAssignee, newAssignee, taskName);

        // 通知原办理人
        log.debug("通知原办理人 {}: 任务已被重新分配", oldAssignee);

        // 通知新办理人
        log.debug("通知新办理人 {}: 已分配新任务", newAssignee);
    }

    @Override
    public boolean supports(String eventType) {
        return "TASK_ASSIGNED".equals(eventType);
    }

    @Override
    public int getOrder() {
        return 25; // 在任务创建后、任务完成前
    }
}
