package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户任务到网关的处理器
 * 支持排他网关、并行网关、包容网关
 */
@Slf4j
@Component
public class GatewayHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && (nextNodeType == TaskCompletionContext.NodeType.EXCLUSIVE_GATEWAY
                || nextNodeType == TaskCompletionContext.NodeType.PARALLEL_GATEWAY
                || nextNodeType == TaskCompletionContext.NodeType.INCLUSIVE_GATEWAY);
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        TaskCompletionContext.NodeType gatewayType = context.getNextNodeType();
        String gatewayId = context.getNextNodeInfo().getId();

        log.info("任务流转到网关 - 当前任务: {}, 网关类型: {}, 网关ID: {}",
            currentTaskName, gatewayType, gatewayId
        );

        // 前置处理逻辑：
        // 1. 根据网关类型验证必需变量
        // 2. 预计算网关条件
        // 3. 验证业务规则

        switch (gatewayType) {
            case EXCLUSIVE_GATEWAY:
                preHandleExclusiveGateway(context);
                break;
            case PARALLEL_GATEWAY:
                preHandleParallelGateway(context);
                break;
            case INCLUSIVE_GATEWAY:
                preHandleInclusiveGateway(context);
                break;
            default:
                break;
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        TaskCompletionContext.NodeType gatewayType = context.getNextNodeType();

        log.debug("处理网关流转 - 网关类型: {}", gatewayType);

        // 核心处理逻辑：
        // 1. 设置网关决策变量
        // 2. 记录网关执行日志
        // 3. 准备并行路径数据

        switch (gatewayType) {
            case EXCLUSIVE_GATEWAY:
                handleExclusiveGateway(context);
                break;
            case PARALLEL_GATEWAY:
                handleParallelGateway(context);
                break;
            case INCLUSIVE_GATEWAY:
                handleInclusiveGateway(context);
                break;
            default:
                break;
        }
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        TaskCompletionContext.NodeType gatewayType = context.getNextNodeType();
        String gatewayId = context.getNextNodeInfo().getId();

        log.info("网关处理完成 - 网关类型: {}, 网关ID: {}", gatewayType, gatewayId);

        // 后置处理逻辑：
        // 1. 记录网关决策结果
        // 2. 并行网关：记录生成的并行任务数
        // 3. 触发后续业务逻辑

        if (gatewayType == TaskCompletionContext.NodeType.PARALLEL_GATEWAY) {
            // 并行网关可能产生多个并行路径
            log.info("并行网关已执行，后续将产生多个并行任务");
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        TaskCompletionContext.NodeType gatewayType = context.getNextNodeType();

        log.warn("网关流转回滚 - 网关类型: {}, 流程实例ID: {}",
            gatewayType, context.getProcessInstanceId()
        );

        // 回滚逻辑：
        // 1. 清理已创建的并行路径
        // 2. 恢复网关前的状态
        // 3. 发送回滚通知

        if (gatewayType == TaskCompletionContext.NodeType.PARALLEL_GATEWAY) {
            // 并行网关需要清理所有并行路径
            log.warn("需要清理并行网关创建的所有并行路径");
        }
    }

    /**
     * 处理排他网关前置逻辑
     */
    private void preHandleExclusiveGateway(TaskCompletionContext context) {
        // 排他网关根据条件选择一条路径
        String conditionVariable = context.getVariables()
            .getOrDefault("gatewayCondition", "default").toString();
        log.debug("排他网关条件变量: {}", conditionVariable);
    }

    /**
     * 处理并行网关前置逻辑
     */
    private void preHandleParallelGateway(TaskCompletionContext context) {
        // 并行网关会同时执行所有输出路径
        log.debug("并行网关将创建多条并行执行路径");

        // 验证所有并行路径的数据准备情况
        // 可以在这里添加并行路径的数据验证逻辑
    }

    /**
     * 处理包容网关前置逻辑
     */
    private void preHandleInclusiveGateway(TaskCompletionContext context) {
        // 包容网关根据条件执行满足条件的路径
        log.debug("包容网关将根据条件执行满足条件的路径");
    }

    /**
     * 处理排他网关核心逻辑
     */
    private void handleExclusiveGateway(TaskCompletionContext context) {
        // 设置网关决策结果（实际由 Flowable 引擎执行）
        context.getVariables().put("gatewayExecuted", true);
        context.getVariables().put("gatewayType", "exclusive");
    }

    /**
     * 处理并行网关核心逻辑
     */
    private void handleParallelGateway(TaskCompletionContext context) {
        // 记录并行网关执行
        context.getVariables().put("gatewayExecuted", true);
        context.getVariables().put("gatewayType", "parallel");
    }

    /**
     * 处理包容网关核心逻辑
     */
    private void handleInclusiveGateway(TaskCompletionContext context) {
        // 记录包容网关执行
        context.getVariables().put("gatewayExecuted", true);
        context.getVariables().put("gatewayType", "inclusive");
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
