package com.lingflow.extension.event.impl;

import com.lingflow.extension.event.ProcessEvent;
import com.lingflow.extension.event.ProcessEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 任务完成事件监听器
 */
@Slf4j
@Component
public class TaskCompletedListener implements ProcessEventListener {

    @Override
    public void onBefore(ProcessEvent event) {
        String taskName = event.getData("taskName", String.class);
        String assignee = event.getData("assignee", String.class);
        String taskId = event.getTaskId();

        log.info("任务即将完成 - 任务ID: {}, 任务名称: {}, 办理人: {}, 流程实例ID: {}",
            taskId, taskName, assignee, event.getProcessInstanceId()
        );

        // 可以在这里添加前置逻辑，如：
        // - 验证任务完成权限
        // - 验证必填变量
        // - 检查业务规则
        // - 记录完成时间
    }

    @Override
    public void onAfter(ProcessEvent event) {
        String taskName = event.getData("taskName", String.class);
        String taskId = event.getTaskId();

        log.info("任务已完成 - 任务ID: {}, 任务名称: {}, 流程实例ID: {}",
            taskId, taskName, event.getProcessInstanceId()
        );

        // 可以在这里添加后置逻辑，如：
        // - 发送完成通知
        // - 触发下游流程
        // - 更新业务数据
        // - 生成业务报告
    }

    @Override
    public boolean supports(String eventType) {
        return "TASK_COMPLETED".equals(eventType);
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
