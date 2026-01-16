package com.lingflow.extension.event.impl;

import com.lingflow.extension.event.ProcessEvent;
import com.lingflow.extension.event.ProcessEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 任务创建事件监听器
 */
@Slf4j
@Component
public class TaskCreatedListener implements ProcessEventListener {

    @Override
    public void onBefore(ProcessEvent event) {
        // 任务创建前通常没有太多需要处理的逻辑
        log.debug("任务即将创建 - 任务名称: {}, 流程实例ID: {}",
            event.getData("taskName", String.class),
            event.getProcessInstanceId()
        );
    }

    @Override
    public void onAfter(ProcessEvent event) {
        String taskName = event.getData("taskName", String.class);
        String assignee = event.getData("assignee", String.class);
        String taskId = event.getTaskId();

        log.info("任务已创建 - 任务ID: {}, 任务名称: {}, 办理人: {}, 流程实例ID: {}",
            taskId, taskName, assignee, event.getProcessInstanceId()
        );

        // 可以在这里添加后置逻辑，如：
        // - 发送待办通知
        // - 添加候选人/组
        // - 设置任务到期时间
        // - 记录任务审计信息
    }

    @Override
    public boolean supports(String eventType) {
        return "TASK_CREATED".equals(eventType);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
