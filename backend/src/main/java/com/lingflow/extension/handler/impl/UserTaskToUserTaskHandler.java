package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户任务到用户任务的处理器
 */
@Slf4j
@Component
public class UserTaskToUserTaskHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && context.getNextNodeType() == TaskCompletionContext.NodeType.USER_TASK;
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String nextTaskName = context.getNextNodeInfo().getName();
        String assignee = context.getNextNodeInfo().getAssignee();

        log.info("用户任务流转 - 当前任务: {}, 下一任务: {}, 下一办理人: {}",
            currentTaskName, nextTaskName, assignee
        );

        // 前置处理逻辑：
        // 1. 验证当前任务的必填变量
        // 2. 检查下一任务的办理人是否有效
        // 3. 验证业务规则

        if (assignee == null || assignee.isEmpty()) {
            log.warn("下一任务 {} 没有指定办理人", nextTaskName);
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        // 核心处理逻辑：
        // 1. 传递流程变量
        // 2. 设置下一任务的候选人/组
        // 3. 记录流转日志

        log.debug("处理用户任务到用户任务的流转 - 流程实例ID: {}",
            context.getProcessInstanceId()
        );

        // 示例：自动设置下一任务的通知标记
        context.getVariables().put("notifyNextAssignee", true);
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String nextTaskName = context.getNextNodeInfo().getName();

        log.info("用户任务流转完成 - 下一任务: {} 已创建", nextTaskName);

        // 后置处理逻辑：
        // 1. 发送待办通知给下一办理人
        // 2. 更新业务状态
        // 3. 触发其他业务逻辑

        String assignee = context.getNextNodeInfo().getAssignee();
        if (assignee != null && !assignee.isEmpty()) {
            // 这里可以集成消息通知服务
            log.info("应向办理人 {} 发送待办通知: {}", assignee, nextTaskName);
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        log.warn("用户任务流转回滚 - 当前任务: {}, 流程实例ID: {}",
            context.getCurrentNodeInfo().getName(),
            context.getProcessInstanceId()
        );

        // 回滚逻辑：
        // 1. 清理已创建的下一任务
        // 2. 恢复变量状态
        // 3. 发送回滚通知
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
