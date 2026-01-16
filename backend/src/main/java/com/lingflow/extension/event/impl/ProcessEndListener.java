package com.lingflow.extension.event.impl;

import com.lingflow.extension.event.ProcessEvent;
import com.lingflow.extension.event.ProcessEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 流程结束事件监听器
 */
@Slf4j
@Component
public class ProcessEndListener implements ProcessEventListener {

    @Override
    public void onBefore(ProcessEvent event) {
        String processDefinitionKey = event.getData("processDefinitionKey", String.class);
        String processInstanceId = event.getProcessInstanceId();

        log.info("流程即将结束 - 流程定义Key: {}, 流程实例ID: {}",
            processDefinitionKey, processInstanceId);

        // 前置处理逻辑：
        // 1. 验证流程结束条件
        // 2. 检查未完成任务
        // 3. 验证业务规则
        // 4. 准备结束审计数据

        // 计算流程执行时长
        LocalDateTime startTime = event.getData("startTime", LocalDateTime.class);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, event.getTriggerTime());
            log.info("流程执行时长: {} 毫秒", duration.toMillis());
        }

        // 记录流程结束原因
        String endReason = event.getData("endReason", String.class);
        if (endReason != null) {
            log.info("流程结束原因: {}", endReason);
        }
    }

    @Override
    public void onAfter(ProcessEvent event) {
        String processDefinitionKey = event.getData("processDefinitionKey", String.class);
        String processInstanceId = event.getProcessInstanceId();
        String businessKey = event.getData("businessKey", String.class);

        log.info("流程已结束 - 流程定义Key: {}, 流程实例ID: {}, 业务Key: {}",
            processDefinitionKey, processInstanceId, businessKey);

        // 后置处理逻辑：
        // 1. 归档流程数据
        // 2. 更新业务状态
        // 3. 发送流程结束通知
        // 4. 触发后续业务流程
        // 5. 生成流程报告
        // 6. 清理临时数据

        // 处理流程变量
        Object finalVariables = event.getData("finalVariables");
        if (finalVariables != null) {
            log.debug("流程最终变量: {}", finalVariables);
        }

        // 检查是否有子流程需要清理
        Boolean hasSubProcess = event.getData("hasSubProcess", Boolean.class);
        if (hasSubProcess != null && hasSubProcess) {
            log.info("流程包含子流程，可能需要关联处理");
        }

        // 处理流程结果
        String processResult = event.getData("processResult", String.class);
        if (processResult != null) {
            log.info("流程执行结果: {}", processResult);

            // 根据结果执行不同逻辑
            switch (processResult) {
                case "approved":
                    handleApprovedProcess(event);
                    break;
                case "rejected":
                    handleRejectedProcess(event);
                    break;
                case "cancelled":
                    handleCancelledProcess(event);
                    break;
                default:
                    handleCompletedProcess(event);
                    break;
            }
        }

        // 记录流程统计信息
        recordProcessStatistics(event);
    }

    /**
     * 处理通过的流程
     */
    private void handleApprovedProcess(ProcessEvent event) {
        log.info("处理已通过的流程 - 流程实例ID: {}", event.getProcessInstanceId());

        // 可以在这里添加流程通过后的特殊逻辑
        // 例如：
        // 1. 更新业务状态为"已通过"
        // 2. 发送通过通知
        // 3. 触发下游流程
        // 4. 生成审批单据
    }

    /**
     * 处理拒绝的流程
     */
    private void handleRejectedProcess(ProcessEvent event) {
        log.info("处理已拒绝的流程 - 流程实例ID: {}", event.getProcessInstanceId());

        // 可以在这里添加流程拒绝后的特殊逻辑
        // 例如：
        // 1. 更新业务状态为"已拒绝"
        // 2. 发送拒绝通知
        // 3. 记录拒绝原因
        // 4. 触发重新提交流程

        String rejectReason = event.getData("rejectReason", String.class);
        if (rejectReason != null) {
            log.info("拒绝原因: {}", rejectReason);
        }
    }

    /**
     * 处理取消的流程
     */
    private void handleCancelledProcess(ProcessEvent event) {
        log.info("处理已取消的流程 - 流程实例ID: {}", event.getProcessInstanceId());

        // 可以在这里添加流程取消后的特殊逻辑
        // 例如：
        // 1. 更新业务状态为"已取消"
        // 2. 发送取消通知
        // 3. 清理已创建的数据
        // 4. 释放占用的资源

        String cancelReason = event.getData("cancelReason", String.class);
        if (cancelReason != null) {
            log.info("取消原因: {}", cancelReason);
        }
    }

    /**
     * 处理正常完成的流程
     */
    private void handleCompletedProcess(ProcessEvent event) {
        log.info("处理正常完成的流程 - 流程实例ID: {}", event.getProcessInstanceId());

        // 可以在这里添加流程正常完成后的特殊逻辑
        // 例如：
        // 1. 更新业务状态为"已完成"
        // 2. 发送完成通知
        // 3. 归档流程数据
        // 4. 生成完成报告
    }

    /**
     * 记录流程统计信息
     */
    private void recordProcessStatistics(ProcessEvent event) {
        log.info("记录流程统计信息 - 流程实例ID: {}", event.getProcessInstanceId());

        // 可以在这里记录各种流程统计信息
        // 例如：
        // 1. 流程执行时长
        // 2. 任务完成率
        // 3. 平均任务处理时间
        // 4. 参与人统计

        Integer totalTasks = event.getData("totalTasks", Integer.class);
        Integer completedTasks = event.getData("completedTasks", Integer.class);

        if (totalTasks != null && completedTasks != null) {
            log.info("任务统计: 总任务数 {}, 已完成 {}", totalTasks, completedTasks);
        }
    }

    @Override
    public boolean supports(String eventType) {
        return "PROCESS_END".equals(eventType);
    }

    @Override
    public int getOrder() {
        return 5; // 流程结束监听器优先级较高
    }
}
