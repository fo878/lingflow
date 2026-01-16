package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 接收任务处理器
 * 处理用户任务到接收任务的流转
 * 接收任务用于等待外部系统的信号
 */
@Slf4j
@Component
public class ReceiveTaskHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && nextNodeType == TaskCompletionContext.NodeType.RECEIVE_TASK;
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String receiveTaskId = context.getNextNodeInfo().getId();
        String receiveTaskName = context.getNextNodeInfo().getName();

        log.info("进入接收任务 - 当前任务: {}, 接收任务: {}({})",
            currentTaskName, receiveTaskName, receiveTaskId);

        // 前置处理逻辑：
        // 1. 验证接收任务配置
        // 2. 准备等待条件
        // 3. 设置超时时间

        // 获取接收任务的等待条件
        String waitCondition = context.getVariables()
            .getOrDefault("waitCondition", "").toString();
        if (!waitCondition.isEmpty()) {
            log.debug("等待条件: {}", waitCondition);
        }

        // 检查是否需要等待消息
        Boolean waitForMessage = context.getVariables()
            .get("waitForMessage") != null
                ? (Boolean) context.getVariables().get("waitForMessage")
                : true;
        log.debug("等待消息: {}", waitForMessage);

        // 检查是否需要等待信号
        String signalName = context.getVariables()
            .getOrDefault("signalName", "").toString();
        if (!signalName.isEmpty()) {
            log.debug("等待信号: {}", signalName);
            context.getVariables().put("signalWaiting", true);
        }

        // 设置超时时间
        Object timeout = context.getVariables().get("receiveTimeout");
        if (timeout != null) {
            log.debug("接收任务超时时间: {}", timeout);
            context.getVariables().put("receiveTimeoutSet", true);
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        String receiveTaskName = context.getNextNodeInfo().getName();

        log.debug("处理接收任务流转 - 接收任务: {}", receiveTaskName);

        // 核心处理逻辑：
        // 1. 设置接收任务执行标记
        // 2. 记录等待信息
        // 3. 准备回调配置

        context.getVariables().put("receiveTaskExecuted", true);
        context.getVariables().put("receiveTaskName", receiveTaskName);

        // 检查等待类型
        String signalName = context.getVariables()
            .getOrDefault("signalName", "").toString();

        if (!signalName.isEmpty()) {
            // 等待信号类型
            context.getVariables().put("receiveTaskType", "signal");
            context.getVariables().put("signalName", signalName);
            log.info("接收任务将等待信号: {}", signalName);
        } else {
            // 等待消息类型
            String messageName = context.getVariables()
                .getOrDefault("messageName", "defaultMessage").toString();
            context.getVariables().put("receiveTaskType", "message");
            context.getVariables().put("messageName", messageName);
            log.info("接收任务将等待消息: {}", messageName);
        }

        // 设置关联的业务Key（用于匹配外部调用）
        String correlationKey = context.getVariables()
            .getOrDefault("correlationKey", context.getProcessInstanceId()).toString();
        context.getVariables().put("receiveTaskCorrelationKey", correlationKey);
        log.debug("关联Key: {}", correlationKey);

        // 设置是否异步
        Boolean async = context.getVariables()
            .get("async") != null
                ? (Boolean) context.getVariables().get("async")
                : false;
        context.getVariables().put("receiveTaskAsync", async);
        log.debug("异步接收: {}", async);
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String receiveTaskName = context.getNextNodeInfo().getName();

        log.info("接收任务处理完成 - 接收任务: {}", receiveTaskName);

        // 后置处理逻辑：
        // 1. 检查接收任务状态
        // 2. 设置回调监听
        // 3. 记录等待开始时间

        // 设置等待开始时间
        context.getVariables().put("receiveTaskWaitStartTime",
            java.time.LocalDateTime.now());

        // 检查是否需要通知外部系统
        Boolean notifyExternal = context.getVariables()
            .get("notifyExternalSystem") != null
                ? (Boolean) context.getVariables().get("notifyExternalSystem")
                : false;

        if (notifyExternal) {
            String externalEndpoint = context.getVariables()
                .getOrDefault("externalEndpoint", "").toString();
            log.info("需要通知外部系统: {}", externalEndpoint);

            // 这里可以集成消息服务通知外部系统
            context.getVariables().put("externalSystemNotified", true);
        }

        // 检查是否需要记录等待事件
        Boolean recordWaitEvent = context.getVariables()
            .get("recordWaitEvent") != null
                ? (Boolean) context.getVariables().get("recordWaitEvent")
                : true;

        if (recordWaitEvent) {
            context.getVariables().put("waitEventRecorded", true);
            log.info("接收任务等待事件已记录");
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        String receiveTaskName = context.getNextNodeInfo().getName();

        log.warn("接收任务流转回滚 - 接收任务: {}, 流程实例ID: {}",
            receiveTaskName, context.getProcessInstanceId());

        // 回滚逻辑：
        // 1. 检查是否正在等待
        // 2. 如果正在等待，取消等待
        // 3. 清理等待状态
        // 4. 通知外部系统取消

        Boolean receiveTaskExecuted = context.getVariables()
            .get("receiveTaskExecuted") != null
                ? (Boolean) context.getVariables().get("receiveTaskExecuted")
                : false;

        if (receiveTaskExecuted) {
            log.warn("接收任务已执行，需要取消等待");

            // 检查等待类型并执行相应的取消逻辑
            String receiveTaskType = context.getVariables()
                .getOrDefault("receiveTaskType", "").toString();

            if ("signal".equals(receiveTaskType)) {
                String signalName = context.getVariables()
                    .getOrDefault("signalName", "").toString();
                log.info("取消信号等待: {}", signalName);
            } else if ("message".equals(receiveTaskType)) {
                String messageName = context.getVariables()
                    .getOrDefault("messageName", "").toString();
                log.info("取消消息等待: {}", messageName);
            }

            // 通知外部系统取消
            Boolean externalNotified = context.getVariables()
                .get("externalSystemNotified") != null
                    ? (Boolean) context.getVariables().get("externalSystemNotified")
                    : false;

            if (externalNotified) {
                String externalEndpoint = context.getVariables()
                    .getOrDefault("externalEndpoint", "").toString();
                log.info("通知外部系统取消等待: {}", externalEndpoint);
            }
        }

        // 清理接收任务相关变量
        context.getVariables().remove("receiveTaskExecuted");
        context.getVariables().remove("receiveTaskName");
        context.getVariables().remove("receiveTaskType");
        context.getVariables().remove("signalWaiting");
        context.getVariables().remove("receiveTaskCorrelationKey");
    }

    /**
     * 处理信号到达
     *
     * @param context 任务提交上下文
     * @param signalName 信号名称
     * @param payload 信号载荷
     */
    public void handleSignalReceived(
        TaskCompletionContext context,
        String signalName,
        Object payload
    ) {
        log.info("接收到信号 - 信号名称: {}, 载荷: {}", signalName, payload);

        // 验证信号名称
        String expectedSignal = context.getVariables()
            .getOrDefault("signalName", "").toString();

        if (!expectedSignal.equals(signalName)) {
            log.warn("信号名称不匹配 - 期望: {}, 实际: {}", expectedSignal, signalName);
            return;
        }

        // 设置信号接收标记
        context.getVariables().put("signalReceived", true);
        context.getVariables().put("signalPayload", payload);
        context.getVariables().put("signalReceiveTime",
            java.time.LocalDateTime.now());

        log.info("信号处理完成 - 流程实例ID: {}", context.getProcessInstanceId());
    }

    /**
     * 处理消息到达
     *
     * @param context 任务提交上下文
     * @param messageName 消息名称
     * @param payload 消息载荷
     */
    public void handleMessageReceived(
        TaskCompletionContext context,
        String messageName,
        Object payload
    ) {
        log.info("接收到消息 - 消息名称: {}, 载荷: {}", messageName, payload);

        // 验证消息名称
        String expectedMessage = context.getVariables()
            .getOrDefault("messageName", "").toString();

        if (!expectedMessage.equals(messageName)) {
            log.warn("消息名称不匹配 - 期望: {}, 实际: {}", expectedMessage, messageName);
            return;
        }

        // 设置消息接收标记
        context.getVariables().put("messageReceived", true);
        context.getVariables().put("messagePayload", payload);
        context.getVariables().put("messageReceiveTime",
            java.time.LocalDateTime.now());

        log.info("消息处理完成 - 流程实例ID: {}", context.getProcessInstanceId());
    }

    @Override
    public int getOrder() {
        return 70;
    }
}
