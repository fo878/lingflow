package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 子流程处理器
 * 处理用户任务到调用活动（CallActivity）或子流程的流转
 */
@Slf4j
@Component
public class SubProcessHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && nextNodeType == TaskCompletionContext.NodeType.CALL_ACTIVITY;
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String callActivityId = context.getNextNodeInfo().getId();
        String calledProcessKey = context.getVariables()
            .getOrDefault("calledProcessKey", "").toString();

        log.info("进入子流程 - 当前任务: {}, 调用活动ID: {}, 调用流程Key: {}",
            currentTaskName, callActivityId, calledProcessKey);

        // 前置处理逻辑：
        // 1. 验证子流程是否存在
        // 2. 准备子流程变量
        // 3. 设置流程传递参数

        if (calledProcessKey.isEmpty()) {
            log.warn("未指定调用的流程Key，可能需要在BPMN中配置");
        }

        // 验证子流程变量
        Object subProcessVariables = context.getVariables()
            .get("subProcessVariables");
        if (subProcessVariables != null) {
            log.debug("子流程变量已准备: {}", subProcessVariables);
        }

        // 检查是否需要传递业务Key
        String businessKey = context.getVariables()
            .getOrDefault("subProcessBusinessKey", "").toString();
        if (!businessKey.isEmpty()) {
            log.info("子流程业务Key: {}", businessKey);
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        String callActivityId = context.getNextNodeInfo().getId();
        String calledProcessKey = context.getVariables()
            .getOrDefault("calledProcessKey", "").toString();

        log.debug("处理子流程流转 - 调用流程: {}", calledProcessKey);

        // 核心处理逻辑：
        // 1. 设置子流程调用标记
        // 2. 记录调用关系
        // 3. 传递流程变量

        context.getVariables().put("callActivityExecuted", true);
        context.getVariables().put("callActivityId", callActivityId);
        context.getVariables().put("calledProcessKey", calledProcessKey);

        // 记录父流程信息，方便子流程回调
        context.getVariables().put("parentProcessInstanceId",
            context.getProcessInstanceId());
        context.getVariables().put("parentTaskId",
            context.getCurrentNodeInfo().getId());

        // 处理变量传递
        Object variablesToPass = context.getVariables()
            .get("variablesToPass");
        if (variablesToPass != null) {
            log.info("将传递变量到子流程: {}", variablesToPass);
            context.getVariables().put("subProcessInputVariables",
                variablesToPass);
        }

        // 处理InOut变量映射
        Object inOutMapping = context.getVariables()
            .get("inOutVariableMapping");
        if (inOutMapping != null) {
            log.debug("InOut变量映射: {}", inOutMapping);
            context.getVariables().put("inOutMapping", inOutMapping);
        }
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String callActivityId = context.getNextNodeInfo().getId();

        log.info("子流程调用完成 - 调用活动ID: {}", callActivityId);

        // 后置处理逻辑：
        // 1. 记录子流程实例ID
        // 2. 设置回调监听
        // 3. 通知子流程启动

        String subProcessInstanceId = context.getVariables()
            .getOrDefault("subProcessInstanceId", "").toString();
        if (!subProcessInstanceId.isEmpty()) {
            log.info("子流程实例ID: {}", subProcessInstanceId);
        }

        // 如果需要等待子流程完成
        Boolean waitForCompletion = context.getVariables()
            .get("waitForCompletion") != null
                ? (Boolean) context.getVariables().get("waitForCompletion")
                : false;

        if (waitForCompletion) {
            log.info("将等待子流程完成");
            context.getVariables().put("waitForSubProcess", true);
        } else {
            log.info("不等待子流程完成，继续执行");
            context.getVariables().put("waitForSubProcess", false);
        }

        // 设置子流程完成后的回调标记
        context.getVariables().put("subProcessCallbackRequired", true);
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        String callActivityId = context.getNextNodeInfo().getId();

        log.warn("子流程流转回滚 - 调用活动ID: {}, 流程实例ID: {}",
            callActivityId, context.getProcessInstanceId());

        // 回滚逻辑：
        // 1. 检查子流程是否已启动
        // 2. 如果已启动，取消子流程
        // 3. 清理调用关系
        // 4. 恢复父流程状态

        String subProcessInstanceId = context.getVariables()
            .getOrDefault("subProcessInstanceId", "").toString();

        if (!subProcessInstanceId.isEmpty()) {
            log.warn("需要取消子流程实例: {}", subProcessInstanceId);
            // 这里可以添加取消子流程的逻辑
            // 例如：调用 runtimeService.deleteProcessInstance(subProcessInstanceId, "回滚");
        }

        // 清理子流程相关变量
        context.getVariables().remove("callActivityExecuted");
        context.getVariables().remove("callActivityId");
        context.getVariables().remove("calledProcessKey");
        context.getVariables().remove("subProcessInstanceId");
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
