package com.lingflow.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程验证器
 * 提供流程相关的验证功能
 */
@Slf4j
@Component
public class ProcessValidator {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 验证BPMN XML格式
     *
     * @param bpmnXml BPMN XML字符串
     * @return 验证结果
     */
    public ValidationResult validateBpmnXml(String bpmnXml) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            // 转换为BpmnModel验证格式
            BpmnXMLConverter converter = new BpmnXMLConverter();

            // 将 ByteArrayInputStream 转换为 XMLStreamReader
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(
                new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))
            );

            BpmnModel model = converter.convertToBpmnModel(reader);

            if (model == null) {
                result.setValid(false);
                result.addError("无效的 BPMN XML");
                return result;
            }

            // 验证基本信息
            Collection<Process> processes = model.getProcesses();
            if (processes == null || processes.isEmpty()) {
                result.setValid(false);
                result.addError("BPMN XML 中没有找到流程定义");
                return result;
            }

            // 验证流程定义
            for (Process process : processes) {
                validateProcess(process, result);
            }

            if (!result.getErrors().isEmpty()) {
                result.setValid(false);
            }

        } catch (Exception e) {
            result.setValid(false);
            result.addError("BPMN XML 解析失败: " + e.getMessage());
            log.error("验证BpmnXml失败", e);
        }

        return result;
    }

    /**
     * 验证流程定义是否存在
     *
     * @param processDefinitionKey 流程定义Key
     * @return 验证结果
     */
    public ValidationResult validateProcessDefinition(String processDefinitionKey) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

            if (definition == null) {
                result.setValid(false);
                result.addError("流程定义不存在: " + processDefinitionKey);
            }

        } catch (Exception e) {
            result.setValid(false);
            result.addError("验证流程定义失败: " + e.getMessage());
            log.error("validateProcessDefinition失败", e);
        }

        return result;
    }

    /**
     * 验证流程变量
     *
     * @param variables 变量Map
     * @param requiredVariables 必需变量名集合
     * @return 验证结果
     */
    public ValidationResult validateProcessVariables(
        Map<String, Object> variables,
        Set<String> requiredVariables
    ) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        if (requiredVariables == null || requiredVariables.isEmpty()) {
            return result;
        }

        for (String varName : requiredVariables) {
            if (variables == null || !variables.containsKey(varName)) {
                result.setValid(false);
                result.addError("缺少必需的流程变量: " + varName);
            }
        }

        return result;
    }

    /**
     * 验证用户任务配置
     *
     * @param processDefinitionId 流程定义ID
     * @return 验证结果
     */
    public ValidationResult validateUserTaskConfiguration(String processDefinitionId) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            Collection<org.flowable.bpmn.model.FlowElement> flowElements =
                bpmnModel.getMainProcess().getFlowElements();

            List<UserTask> userTasks = flowElements.stream()
                .filter(element -> element instanceof UserTask)
                .map(element -> (UserTask) element)
                .collect(Collectors.toList());

            // 验证每个用户任务
            for (UserTask userTask : userTasks) {
                // 检查是否有assignee或候选用户/组
                if (userTask.getAssignee() == null &&
                    (userTask.getCandidateUsers() == null || userTask.getCandidateUsers().isEmpty()) &&
                    (userTask.getCandidateGroups() == null || userTask.getCandidateGroups().isEmpty())) {

                    result.setValid(false);
                    result.addError(String.format(
                        "用户任务【%s】没有配置办理人或候选人/组: %s",
                        userTask.getName(), userTask.getId()
                    ));
                }
            }

            if (!result.getErrors().isEmpty()) {
                result.setValid(false);
            }

        } catch (Exception e) {
            result.setValid(false);
            result.addError("验证用户任务配置失败: " + e.getMessage());
            log.error("validateUserTaskConfiguration失败", e);
        }

        return result;
    }

    /**
     * 验证流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @param expectedStates 期望的状态集合（RUNNING, SUSPENDED, COMPLETED）
     * @return 验证结果
     */
    public ValidationResult validateProcessInstanceState(
        String processInstanceId,
        Set<String> expectedStates
    ) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            // 这里需要注入 RuntimeService
            // 简化实现：只返回有效
            // 实际使用时应该查询流程实例状态

        } catch (Exception e) {
            result.setValid(false);
            result.addError("验证流程实例状态失败: " + e.getMessage());
            log.error("validateProcessInstanceState失败", e);
        }

        return result;
    }

    /**
     * 验证任务操作权限
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 验证结果
     */
    public ValidationResult validateTaskPermission(String taskId, String userId) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            // 这里需要注入 TaskService
            // 简化实现：只返回有效
            // 实际使用时应该查询任务办理人

        } catch (Exception e) {
            result.setValid(false);
            result.addError("验证任务权限失败: " + e.getMessage());
            log.error("validateTaskPermission失败", e);
        }

        return result;
    }

    /**
     * 验证流程定义是否可部署
     *
     * @param bpmnXml BPMN XML
     * @return 验证结果
     */
    public ValidationResult validateDeployment(String bpmnXml) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        // 先验证XML格式
        ValidationResult xmlResult = validateBpmnXml(bpmnXml);
        if (!xmlResult.isValid()) {
            return xmlResult;
        }

        try {
            // 转换为BpmnModel
            BpmnXMLConverter converter = new BpmnXMLConverter();

            // 将 ByteArrayInputStream 转换为 XMLStreamReader
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(
                new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))
            );

            BpmnModel model = converter.convertToBpmnModel(reader);

            // 验证流程ID
            Collection<Process> processes = model.getProcesses();
            if (processes != null && !processes.isEmpty()) {
                Process process = processes.iterator().next();
                if (process.getId() == null || process.getId().isEmpty()) {
                    result.setValid(false);
                    result.addError("流程必须定义ID");
                }
            }

            if (!result.getErrors().isEmpty()) {
                result.setValid(false);
            }

        } catch (Exception e) {
            result.setValid(false);
            result.addError("验证部署失败: " + e.getMessage());
            log.error("validateDeployment失败", e);
        }

        return result;
    }

    /**
     * 验证流程
     *
     * @param process 流程定义
     * @param result 验证结果
     */
    private void validateProcess(Process process, ValidationResult result) {
        // 验证流程ID
        if (process.getId() == null || process.getId().isEmpty()) {
            result.addError("流程缺少ID");
        }

        // 验证流程名称
        if (process.getName() == null || process.getName().isEmpty()) {
            result.addWarning("流程缺少名称");
        }

        // 验证至少有一个开始节点
        if (process.getInitialFlowElement() == null) {
            result.addError("流程缺少开始节点");
        }

        // 验证用户任务配置
        Collection<org.flowable.bpmn.model.FlowElement> flowElements = process.getFlowElements();
        List<UserTask> userTasks = flowElements.stream()
            .filter(element -> element instanceof UserTask)
            .map(element -> (UserTask) element)
            .collect(Collectors.toList());

        for (UserTask userTask : userTasks) {
            if (userTask.getAssignee() == null &&
                (userTask.getCandidateUsers() == null || userTask.getCandidateUsers().isEmpty()) &&
                (userTask.getCandidateGroups() == null || userTask.getCandidateGroups().isEmpty())) {

                result.addWarning(String.format(
                    "用户任务【%s】未配置办理人或候选人/组: %s",
                    userTask.getName(), userTask.getId()
                ));
            }
        }
    }

    /**
     * 验证结果
     */
    @Data
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public void addError(String error) {
            this.errors.add(error);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
}
