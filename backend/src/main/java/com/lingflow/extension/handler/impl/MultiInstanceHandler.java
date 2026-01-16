package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 多实例节点处理器
 * 处理进入或离开多实例节点的流转
 */
@Slf4j
@Component
public class MultiInstanceHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && (nextNodeType == TaskCompletionContext.NodeType.MULTI_INSTANCE
                || isMultiInstanceTask(context));
    }

    /**
     * 检查是否为多实例任务
     */
    private boolean isMultiInstanceTask(TaskCompletionContext context) {
        Map<String, Object> variables = context.getVariables();
        return variables.containsKey("nrOfInstances")
            || variables.containsKey("nrOfActiveInstances")
            || variables.containsKey("nrOfCompletedInstances");
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String multiInstanceType = context.getVariables()
            .getOrDefault("multiInstanceType", "parallel").toString();

        log.info("进入多实例节点 - 当前任务: {}, 多实例类型: {}",
            currentTaskName, multiInstanceType);

        // 前置处理逻辑：
        // 1. 验证多实例配置
        // 2. 准备多实例变量
        // 3. 验证会签规则

        Integer nrOfInstances = context.getVariables()
            .get("nrOfInstances") != null
                ? (Integer) context.getVariables().get("nrOfInstances")
                : 0;

        if (nrOfInstances > 0) {
            log.info("多实例节点将创建 {} 个实例", nrOfInstances);
        }

        // 验证会签完成条件
        Object completionCondition = context.getVariables()
            .get("completionCondition");
        if (completionCondition != null) {
            log.debug("会签完成条件: {}", completionCondition);
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        String multiInstanceType = context.getVariables()
            .getOrDefault("multiInstanceType", "parallel").toString();

        log.debug("处理多实例流转 - 类型: {}", multiInstanceType);

        // 核心处理逻辑：
        // 1. 设置多实例变量
        // 2. 记录多实例执行状态
        // 3. 处理会签规则

        if ("sequential".equals(multiInstanceType)) {
            handleSequentialMultiInstance(context);
        } else {
            handleParallelMultiInstance(context);
        }

        // 设置多实例执行标记
        context.getVariables().put("multiInstanceExecuted", true);
        context.getVariables().put("multiInstanceType", multiInstanceType);
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String multiInstanceType = context.getVariables()
            .getOrDefault("multiInstanceType", "parallel").toString();

        log.info("多实例节点处理完成 - 类型: {}", multiInstanceType);

        // 后置处理逻辑：
        // 1. 记录多实例完成情况
        // 2. 处理会签结果
        // 3. 通知相关参与人

        Integer nrOfCompletedInstances = context.getVariables()
            .get("nrOfCompletedInstances") != null
                ? (Integer) context.getVariables().get("nrOfCompletedInstances")
                : 0;

        Integer nrOfInstances = context.getVariables()
            .get("nrOfInstances") != null
                ? (Integer) context.getVariables().get("nrOfInstances")
                : 0;

        if (nrOfInstances > 0) {
            log.info("多实例进度: {}/{}", nrOfCompletedInstances, nrOfInstances);

            // 检查是否所有实例都已完成
            if (nrOfCompletedInstances.equals(nrOfInstances)) {
                log.info("所有多实例任务已完成");
                handleAllInstancesCompleted(context);
            }
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        log.warn("多实例流转回滚 - 流程实例ID: {}",
            context.getProcessInstanceId());

        // 回滚逻辑：
        // 1. 清理已创建的多实例任务
        // 2. 恢复多实例前的状态
        // 3. 发送回滚通知

        Integer nrOfInstances = context.getVariables()
            .get("nrOfInstances") != null
                ? (Integer) context.getVariables().get("nrOfInstances")
                : 0;

        if (nrOfInstances > 0) {
            log.warn("需要清理 {} 个多实例任务", nrOfInstances);
        }
    }

    /**
     * 处理串行多实例
     */
    private void handleSequentialMultiInstance(TaskCompletionContext context) {
        log.debug("处理串行多实例节点");

        // 串行多实例：依次创建和执行任务
        // Flowable 引擎会自动处理串行执行

        context.getVariables().put("sequentialMultiInstance", true);

        // 可以在这里添加串行执行的特殊逻辑
        // 例如：记录每个实例的执行顺序
        Integer loopCounter = context.getVariables()
            .get("loopCounter") != null
                ? (Integer) context.getVariables().get("loopCounter")
                : 0;

        log.info("串行多实例当前循环计数: {}", loopCounter);
    }

    /**
     * 处理并行多实例
     */
    @SuppressWarnings("unchecked")
    private void handleParallelMultiInstance(TaskCompletionContext context) {
        log.debug("处理并行多实例节点");

        // 并行多实例：同时创建多个任务
        // Flowable 引擎会自动处理并行执行

        context.getVariables().put("parallelMultiInstance", true);

        // 可以在这里添加并行执行的特殊逻辑
        // 例如：汇总并行任务的执行结果

        List<String> assigneeList = context.getVariables()
            .get("assigneeList") != null
                ? (List<String>) context.getVariables().get("assigneeList")
                : List.of();

        if (!assigneeList.isEmpty()) {
            log.info("并行多实例将分配给 {} 个办理人: {}",
                assigneeList.size(), assigneeList);
        }
    }

    /**
     * 处理所有实例完成的情况
     */
    private void handleAllInstancesCompleted(TaskCompletionContext context) {
        log.info("处理所有多实例完成的逻辑");

        // 可以在这里添加会签完成的特殊逻辑
        // 例如：
        // 1. 汇总会签结果
        // 2. 计算会签结论（通过/拒绝）
        // 3. 触发后续流程

        // 示例：根据完成情况设置会签结果
        Integer approveCount = context.getVariables()
            .get("approveCount") != null
                ? (Integer) context.getVariables().get("approveCount")
                : 0;

        Integer rejectCount = context.getVariables()
            .get("rejectCount") != null
                ? (Integer) context.getVariables().get("rejectCount")
                : 0;

        if (approveCount > 0 || rejectCount > 0) {
            log.info("会签结果 - 同意: {}, 拒绝: {}", approveCount, rejectCount);

            // 设置会签最终结果
            String approvalResult = approveCount > rejectCount ? "approved" : "rejected";
            context.getVariables().put("approvalResult", approvalResult);
            log.info("会签最终结果: {}", approvalResult);
        }
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
