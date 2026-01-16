package com.lingflow.util;

import com.lingflow.dto.ProcessDefinitionVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程工具类
 * 提供流程相关的实用工具方法
 */
@Slf4j
@Component
public class ProcessUtils {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 计算流程实例的运行时长（毫秒）
     *
     * @param processInstanceId 流程实例ID
     * @return 运行时长（毫秒），如果流程不存在返回null
     */
    public Long calculateProcessDuration(String processInstanceId) {
        try {
            org.flowable.engine.runtime.ProcessInstance instance =
                runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (instance == null) {
                // 查询历史流程实例
                org.flowable.engine.history.HistoricProcessInstance historicInstance =
                    historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

                if (historicInstance != null && historicInstance.getEndTime() != null) {
                    return Duration.between(
                        historicInstance.getStartTime().toInstant(),
                        historicInstance.getEndTime().toInstant()
                    ).toMillis();
                }
                return null;
            }

            if (instance.getStartTime() != null) {
                return Duration.between(
                    instance.getStartTime().toInstant(),
                    new Date().toInstant()
                ).toMillis();
            }

            return null;
        } catch (Exception e) {
            log.error("计算流程运行时长失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 格式化时长为可读字符串
     *
     * @param milliseconds 毫秒数
     * @return 格式化后的字符串（如：1天2小时30分钟15秒）
     */
    public String formatDuration(Long milliseconds) {
        if (milliseconds == null) {
            return "未知";
        }

        long seconds = milliseconds / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("秒");
        }

        return sb.toString();
    }

    /**
     * 获取流程定义的所有用户任务节点
     *
     * @param processDefinitionId 流程定义ID
     * @return 用户任务节点列表
     */
    public List<UserTaskInfo> getUserTaskNodes(String processDefinitionId) {
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();

            return flowElements.stream()
                .filter(element -> element instanceof UserTask)
                .map(element -> (UserTask) element)
                .map(userTask -> {
                    UserTaskInfo info = new UserTaskInfo();
                    info.setId(userTask.getId());
                    info.setName(userTask.getName());
                    info.setAssignee(userTask.getAssignee());
                    // 将 List<String> 转换为 Set<String>
                    info.setCandidateGroups(userTask.getCandidateGroups() != null
                        ? new HashSet<>(userTask.getCandidateGroups())
                        : new HashSet<>());
                    info.setCandidateUsers(userTask.getCandidateUsers() != null
                        ? new HashSet<>(userTask.getCandidateUsers())
                        : new HashSet<>());
                    return info;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户任务节点失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取流程实例的下一个用户任务节点
     *
     * @param processInstanceId 流程实例ID
     * @return 下一个用户任务节点信息
     */
    public UserTaskInfo getNextUserTask(String processInstanceId) {
        try {
            List<org.flowable.task.api.Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

            if (!tasks.isEmpty()) {
                org.flowable.task.api.Task task = tasks.get(0);
                UserTaskInfo info = new UserTaskInfo();
                info.setId(task.getId());
                info.setName(task.getName());
                info.setAssignee(task.getAssignee());
                info.setCreateTime(task.getCreateTime());
                return info;
            }

            return null;
        } catch (Exception e) {
            log.error("获取下一个用户任务失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查流程实例是否已完成
     *
     * @param processInstanceId 流程实例ID
     * @return 是否已完成
     */
    public boolean isProcessCompleted(String processInstanceId) {
        try {
            org.flowable.engine.runtime.ProcessInstance instance =
                runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            return instance == null;
        } catch (Exception e) {
            log.error("检查流程是否完成失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取流程实例的所有变量
     *
     * @param processInstanceId 流程实例ID
     * @return 变量Map
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        try {
            // 先尝试从运行时获取
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            if (variables != null && !variables.isEmpty()) {
                return variables;
            }

            // 如果运行时没有，从历史获取
            return historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .collect(Collectors.toMap(
                    var -> var.getVariableName(),
                    var -> var.getValue(),
                    (v1, v2) -> v1
                ));
        } catch (Exception e) {
            log.error("获取流程变量失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 获取流程实例的单个变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName 变量名
     * @return 变量值
     */
    public Object getProcessVariable(String processInstanceId, String variableName) {
        try {
            // 先尝试从运行时获取
            Object value = runtimeService.getVariable(processInstanceId, variableName);
            if (value != null) {
                return value;
            }

            // 如果运行时没有，从历史获取
            org.flowable.variable.api.history.HistoricVariableInstance variable =
                historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .variableName(variableName)
                    .singleResult();

            return variable != null ? variable.getValue() : null;
        } catch (Exception e) {
            log.error("获取流程变量失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 设置流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variables 变量Map
     */
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        try {
            runtimeService.setVariables(processInstanceId, variables);
        } catch (Exception e) {
            log.error("设置流程变量失败: {}", e.getMessage());
            throw new RuntimeException("设置流程变量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param deleteReason 删除原因
     */
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        try {
            runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
        } catch (Exception e) {
            log.error("删除流程实例失败: {}", e.getMessage());
            throw new RuntimeException("删除流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 挂起流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void suspendProcessInstance(String processInstanceId) {
        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
        } catch (Exception e) {
            log.error("挂起流程实例失败: {}", e.getMessage());
            throw new RuntimeException("挂起流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 激活流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void activateProcessInstance(String processInstanceId) {
        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
        } catch (Exception e) {
            log.error("激活流程实例失败: {}", e.getMessage());
            throw new RuntimeException("激活流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取流程定义的最新版本
     *
     * @param processDefinitionKey 流程定义Key
     * @return 流程定义VO
     */
    public ProcessDefinitionVO getLatestProcessDefinition(String processDefinitionKey) {
        try {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

            if (definition == null) {
                return null;
            }

            ProcessDefinitionVO vo = new ProcessDefinitionVO();
            vo.setId(definition.getId());
            vo.setKey(definition.getKey());
            vo.setName(definition.getName());
            vo.setVersion(definition.getVersion());
            vo.setDeploymentId(definition.getDeploymentId());
            vo.setResource(definition.getResourceName());
            vo.setSuspended(definition.isSuspended());

            return vo;
        } catch (Exception e) {
            log.error("获取流程定义失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 用户任务节点信息
     */
    @Data
    public static class UserTaskInfo {
        private String id;
        private String name;
        private String assignee;
        private Set<String> candidateGroups;
        private Set<String> candidateUsers;
        private Date createTime;
    }
}
