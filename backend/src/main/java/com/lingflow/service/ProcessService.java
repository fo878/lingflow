package com.lingflow.service;

import com.lingflow.dto.*;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程服务类
 */
@Service
public class ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 部署流程
     */
    public void deployProcess(String name, String xml) {
        repositoryService.createDeployment()
                .name(name)
                .addBytes(name + ".bpmn20.xml", xml.getBytes(StandardCharsets.UTF_8))
                .deploy();
    }

    /**
     * 获取所有流程定义
     */
    public List<ProcessDefinitionVO> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc()
                .list();

        return definitions.stream().map(def -> {
            ProcessDefinitionVO vo = new ProcessDefinitionVO();
            vo.setKey(def.getKey());
            vo.setName(def.getName());
            vo.setVersion(def.getVersion());
            vo.setDeploymentId(def.getDeploymentId());
            vo.setId(def.getId());
            vo.setResource(def.getResourceName());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 删除流程定义
     */
    public void deleteProcessDefinition(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    /**
     * 启动流程实例
     */
    public ProcessInstanceVO startProcess(String processKey, String businessKey) {
        ProcessInstance instance;
        if (businessKey != null && !businessKey.isEmpty()) {
            instance = runtimeService.startProcessInstanceByKey(processKey, businessKey);
        } else {
            instance = runtimeService.startProcessInstanceByKey(processKey);
        }

        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setStartTime(instance.getStartTime());
        return vo;
    }

    /**
     * 获取运行中的流程实例
     */
    public List<ProcessInstanceVO> getRunningInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .list();

        return instances.stream().map(instance -> {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setId(instance.getId());
            vo.setProcessDefinitionId(instance.getProcessDefinitionId());
            vo.setBusinessKey(instance.getBusinessKey());
            vo.setStartTime(instance.getStartTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取已完结的流程实例
     */
    public List<ProcessInstanceVO> getCompletedInstances() {
        List<org.flowable.engine.history.HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .list();

        return instances.stream().map(instance -> {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setId(instance.getId());
            vo.setProcessDefinitionId(instance.getProcessDefinitionId());
            vo.setBusinessKey(instance.getBusinessKey());
            vo.setStartTime(instance.getStartTime());
            vo.setEndTime(instance.getEndTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取待办任务
     */
    public List<TaskVO> getTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .list();

        return tasks.stream().map(task -> {
            TaskVO vo = new TaskVO();
            vo.setId(task.getId());
            vo.setName(task.getName());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setCreateTime(task.getCreateTime());
            vo.setAssignee(task.getAssignee());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
    }

    /**
     * 生成流程图
     */
    public byte[] generateDiagram(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessDiagramGenerator diagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);

        InputStream is = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds,
                Collections.emptyList(), processEngine.getProcessEngineConfiguration().getActivityFontName(),
                processEngine.getProcessEngineConfiguration().getLabelFontName(),
                processEngine.getProcessEngineConfiguration().getAnnotationFontName(),
                processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0, true);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成流程图失败", e);
        }
    }

    /**
     * 生成流程定义图
     */
    public byte[] generateProcessDefinitionDiagram(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessDiagramGenerator diagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();

        InputStream is = diagramGenerator.generateDiagram(bpmnModel, "png", Collections.emptyList(),
                Collections.emptyList(), processEngine.getProcessEngineConfiguration().getActivityFontName(),
                processEngine.getProcessEngineConfiguration().getLabelFontName(),
                processEngine.getProcessEngineConfiguration().getAnnotationFontName(),
                processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0, true);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成流程图失败", e);
        }
    }
}
