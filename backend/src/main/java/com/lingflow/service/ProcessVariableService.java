package com.lingflow.service;

import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.flowable.variable.api.history.HistoricVariableInstance;

/**
 * 流程变量管理服务
 * 封装流程变量的增删改查操作
 */
@Slf4j
@Service
public class ProcessVariableService {

    @Autowired
    private org.flowable.engine.RuntimeService flowableRuntimeService;

    @Autowired
    private org.flowable.engine.TaskService flowableTaskService;

    @Autowired
    private org.flowable.engine.HistoryService flowableHistoryService;

    @Autowired
    private FlowableServiceTemplate serviceTemplate;

    /**
     * 设置流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @param variables 变量Map
     */
    public void setProcessInstanceVariables(
        String processInstanceId,
        Map<String, Object> variables
    ) {
        serviceTemplate.execute(
            "RuntimeService.setVariables",
            () -> {
                flowableRuntimeService.setVariables(processInstanceId, variables);
                return null;
            },
            processInstanceId, variables
        );
    }

    /**
     * 设置单个流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName 变量名
     * @param value 变量值
     */
    public void setProcessInstanceVariable(
        String processInstanceId,
        String variableName,
        Object value
    ) {
        serviceTemplate.execute(
            "RuntimeService.setVariable",
            () -> {
                flowableRuntimeService.setVariable(processInstanceId, variableName, value);
                return null;
            },
            processInstanceId, variableName, value
        );
    }

    /**
     * 获取流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @return 变量Map
     */
    public Map<String, Object> getProcessInstanceVariables(String processInstanceId) {
        return serviceTemplate.execute(
            "RuntimeService.getVariables",
            () -> flowableRuntimeService.getVariables(processInstanceId),
            processInstanceId
        );
    }

    /**
     * 获取单个流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName 变量名
     * @return 变量值
     */
    public Object getProcessInstanceVariable(
        String processInstanceId,
        String variableName
    ) {
        return serviceTemplate.execute(
            "RuntimeService.getVariable",
            () -> flowableRuntimeService.getVariable(processInstanceId, variableName),
            processInstanceId, variableName
        );
    }

    /**
     * 删除流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableNames 变量名数组
     */
    public void removeProcessInstanceVariables(
        String processInstanceId,
        String... variableNames
    ) {
        serviceTemplate.execute(
            "RuntimeService.removeVariables",
            () -> {
                // Flowable 7.x: removeVariables 需要 Collection<String> 而不是 String[]
                if (variableNames != null && variableNames.length > 0) {
                    flowableRuntimeService.removeVariables(
                        processInstanceId,
                        java.util.Arrays.asList(variableNames)
                    );
                }
                return null;
            },
            processInstanceId, variableNames
        );
    }

    /**
     * 设置任务本地变量
     *
     * @param taskId 任务ID
     * @param variables 变量Map
     */
    public void setTaskLocalVariables(
        String taskId,
        Map<String, Object> variables
    ) {
        serviceTemplate.execute(
            "TaskService.setVariablesLocal",
            () -> {
                flowableTaskService.setVariablesLocal(taskId, variables);
                return null;
            },
            taskId, variables
        );
    }

    /**
     * 设置单个任务本地变量
     *
     * @param taskId 任务ID
     * @param variableName 变量名
     * @param value 变量值
     */
    public void setTaskLocalVariable(
        String taskId,
        String variableName,
        Object value
    ) {
        serviceTemplate.execute(
            "TaskService.setVariableLocal",
            () -> {
                flowableTaskService.setVariableLocal(taskId, variableName, value);
                return null;
            },
            taskId, variableName, value
        );
    }

    /**
     * 获取任务本地变量
     *
     * @param taskId 任务ID
     * @return 变量Map
     */
    public Map<String, Object> getTaskLocalVariables(String taskId) {
        return serviceTemplate.execute(
            "TaskService.getVariablesLocal",
            () -> flowableTaskService.getVariablesLocal(taskId),
            taskId
        );
    }

    /**
     * 获取单个任务本地变量
     *
     * @param taskId 任务ID
     * @param variableName 变量名
     * @return 变量值
     */
    public Object getTaskLocalVariable(
        String taskId,
        String variableName
    ) {
        return serviceTemplate.execute(
            "TaskService.getVariableLocal",
            () -> flowableTaskService.getVariableLocal(taskId, variableName),
            taskId, variableName
        );
    }

    /**
     * 设置任务变量（影响流程范围）
     *
     * @param taskId 任务ID
     * @param variables 变量Map
     */
    public void setTaskVariables(
        String taskId,
        Map<String, Object> variables
    ) {
        serviceTemplate.execute(
            "TaskService.setVariables",
            () -> {
                flowableTaskService.setVariables(taskId, variables);
                return null;
            },
            taskId, variables
        );
    }

    /**
     * 获取任务变量
     *
     * @param taskId 任务ID
     * @return 变量Map
     */
    public Map<String, Object> getTaskVariables(String taskId) {
        return serviceTemplate.execute(
            "TaskService.getVariables",
            () -> flowableTaskService.getVariables(taskId),
            taskId
        );
    }

    /**
     * 获取历史流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @return 变量Map
     */
    public Map<String, Object> getHistoricProcessInstanceVariables(
        String processInstanceId
    ) {
        return serviceTemplate.execute(
            "HistoryService.getHistoricVariableInstances",
            () -> {
                return flowableHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(
                        java.util.stream.Collectors.toMap(
                            var -> var.getVariableName(),
                            var -> var.getValue(),
                            (v1, v2) -> v1
                        )
                    );
            },
            processInstanceId
        );
    }

    /**
     * 获取历史任务变量
     *
     * @param taskId 任务ID
     * @return 变量Map
     */
    public Map<String, Object> getHistoricTaskVariables(String taskId) {
        return serviceTemplate.execute(
            "HistoryService.getHistoricTaskVariableInstances",
            () -> {
                return flowableHistoryService
                    .createHistoricVariableInstanceQuery()
                    .taskId(taskId)
                    .list()
                    .stream()
                    .collect(
                        java.util.stream.Collectors.toMap(
                            var -> var.getVariableName(),
                            var -> var.getValue(),
                            (v1, v2) -> v1
                        )
                    );
            },
            taskId
        );
    }

    /**
     * 更新或插入流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variables 变量Map
     */
    public void upsertProcessInstanceVariables(
        String processInstanceId,
        Map<String, Object> variables
    ) {
        serviceTemplate.execute(
            "ProcessVariableService.upsertVariables",
            () -> {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    Object existingValue = flowableRuntimeService.getVariable(
                        processInstanceId,
                        entry.getKey()
                    );
                    if (existingValue != null) {
                        // 更新现有变量
                        flowableRuntimeService.setVariable(
                            processInstanceId,
                            entry.getKey(),
                            entry.getValue()
                        );
                        log.debug("更新流程变量 - 名称: {}, 值: {}", entry.getKey(), entry.getValue());
                    } else {
                        // 插入新变量
                        flowableRuntimeService.setVariable(
                            processInstanceId,
                            entry.getKey(),
                            entry.getValue()
                        );
                        log.debug("插入流程变量 - 名称: {}, 值: {}", entry.getKey(), entry.getValue());
                    }
                }
                return null;
            },
            processInstanceId, variables
        );
    }

    /**
     * 复制流程变量
     *
     * @param sourceProcessInstanceId 源流程实例ID
     * @param targetProcessInstanceId 目标流程实例ID
     * @param variableNames 要复制的变量名数组（为空则复制全部）
     */
    public void copyProcessInstanceVariables(
        String sourceProcessInstanceId,
        String targetProcessInstanceId,
        String... variableNames
    ) {
        serviceTemplate.execute(
            "ProcessVariableService.copyVariables",
            () -> {
                Map<String, Object> sourceVariables;
                if (variableNames == null || variableNames.length == 0) {
                    // 复制全部变量
                    sourceVariables = flowableRuntimeService.getVariables(
                        sourceProcessInstanceId
                    );
                } else {
                    // 复制指定变量
                    sourceVariables = flowableRuntimeService.getVariables(
                        sourceProcessInstanceId,
                        java.util.Arrays.asList(variableNames)
                    );
                }

                if (!sourceVariables.isEmpty()) {
                    flowableRuntimeService.setVariables(
                        targetProcessInstanceId,
                        sourceVariables
                    );
                    log.info("复制流程变量 - 从: {} 到: {}, 变量数: {}",
                        sourceProcessInstanceId,
                        targetProcessInstanceId,
                        sourceVariables.size()
                    );
                }
                return null;
            },
            sourceProcessInstanceId, targetProcessInstanceId, variableNames
        );
    }

    // ==================== Controller 调用的别名方法 ====================

    /**
     * 获取流程变量（别名方法）
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return getProcessInstanceVariables(processInstanceId);
    }

    /**
     * 设置流程变量（别名方法）
     */
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        setProcessInstanceVariables(processInstanceId, variables);
    }

    /**
     * 获取流程变量（别名方法）
     */
    public Object getProcessVariable(String processInstanceId, String variableName) {
        return getProcessInstanceVariable(processInstanceId, variableName);
    }

    /**
     * 更新流程变量（别名方法）
     */
    public void updateProcessVariable(String processInstanceId, String variableName, Object value) {
        upsertProcessInstanceVariables(processInstanceId, Map.of(variableName, value));
    }

    /**
     * 删除流程变量（别名方法）
     */
    public void removeProcessVariable(String processInstanceId, String variableName) {
        removeProcessInstanceVariables(processInstanceId, variableName);
    }

    /**
     * 获取任务变量（别名方法）
     */
    public Object getTaskVariable(String taskId, String variableName) {
        return flowableTaskService.getVariable(taskId, variableName);
    }

    /**
     * 更新任务变量（别名方法）
     */
    public void updateTaskVariable(String taskId, String variableName, Object value) {
        flowableTaskService.setVariable(taskId, variableName, value);
    }

    /**
     * 获取历史变量（别名方法）
     */
    public Map<String, Object> getHistoricVariables(String processInstanceId) {
        return getHistoricProcessInstanceVariables(processInstanceId);
    }

    /**
     * 获取变量历史（别名方法）
     */
    public java.util.List<HistoricVariableInstance> getVariableHistory(
        String processInstanceId,
        String variableName
    ) {
        return serviceTemplate.execute(
            "HistoryService.createHistoricVariableInstanceQuery",
            () -> {
                java.util.List<HistoricVariableInstance> list = flowableHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .variableName(variableName)
                    .list();
                // 手动排序（按更新时间倒序）
                return list.stream()
                    .sorted((a, b) -> {
                        if (a.getCreateTime() == null) return 1;
                        if (b.getCreateTime() == null) return -1;
                        return b.getCreateTime().compareTo(a.getCreateTime());
                    })
                    .collect(java.util.stream.Collectors.toList());
            },
            processInstanceId, variableName
        );
    }

    /**
     * 复制变量（别名方法）
     */
    public void copyVariables(
        String sourceProcessInstanceId,
        String targetProcessInstanceId,
        java.util.Collection<String> variableNames
    ) {
        copyProcessInstanceVariables(
            sourceProcessInstanceId,
            targetProcessInstanceId,
            variableNames != null ? variableNames.toArray(new String[0]) : null
        );
    }
}
