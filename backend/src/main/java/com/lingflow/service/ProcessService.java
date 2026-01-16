package com.lingflow.service;

import com.lingflow.dto.*;
import com.lingflow.entity.ProcessSnapshot;
import com.lingflow.entity.BpmnElementExtension;
import com.lingflow.entity.BpmnElementExtensionHistory;
import com.lingflow.repository.ProcessSnapshotRepository;
import com.lingflow.repository.BpmnElementExtensionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程服务类
 */
@Service
public class ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

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

    @Autowired
    private ExtendedRepositoryService extendedRepositoryService;

    @Autowired
    private ExtendedRuntimeService extendedRuntimeService;

    @Autowired
    private ExtendedTaskService extendedTaskService;

    @Autowired
    private ExtendedHistoryService extendedHistoryService;

    /**
     * 部署流程
     */
    public void deployProcess(String name, String xml) {
        extendedRepositoryService.deploy(name, xml);
    }

    /**
     * 获取所有流程定义
     */
    public List<ProcessDefinitionVO> getProcessDefinitions() {
        return extendedRepositoryService.getProcessDefinitions();
    }

    /**
     * 删除流程定义
     */
    public void deleteProcessDefinition(String deploymentId) {
        extendedRepositoryService.deleteDeployment(deploymentId, true, false);
    }

    /**
     * 启动流程实例
     */
    public ProcessInstanceVO startProcess(String processKey, String businessKey) {
        return extendedRuntimeService.startProcessInstanceByKey(processKey, businessKey, null);
    }

    /**
     * 获取运行中的流程实例
     */
    public List<ProcessInstanceVO> getRunningInstances() {
        return extendedRuntimeService.getRunningProcessInstances();
    }

    /**
     * 获取已完结的流程实例
     */
    public List<ProcessInstanceVO> getCompletedInstances() {
        return extendedHistoryService.getCompletedProcessInstances();
    }

    /**
     * 获取待办任务
     */
    public List<TaskVO> getTasks() {
        return extendedTaskService.getTasks();
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        extendedTaskService.completeTask(taskId, variables);
    }

    /**
     * 生成流程图
     */
    public byte[] generateDiagram(String processInstanceId) {
        // 先尝试从运行时查询流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        String processDefinitionId;
        List<String> activeActivityIds = Collections.emptyList();

        if (processInstance != null) {
            // 运行中的流程实例
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = extendedRuntimeService.getActiveActivityIds(processInstanceId);
        } else {
            // 查询历史流程实例
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (historicInstance == null) {
                throw new RuntimeException("流程实例不存在");
            }

            // 已完结的流程实例，不需要高亮活动节点
            processDefinitionId = historicInstance.getProcessDefinitionId();
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessDiagramGenerator diagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();

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

    /**
     * 获取流程实例的BPMN XML和节点信息
     */
    public Map<String, Object> getProcessBpmnWithNodeInfo(String processInstanceId) {
        // 查询流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        String processDefinitionId;
        boolean isFinished = false;

        if (processInstance != null) {
            processDefinitionId = processInstance.getProcessDefinitionId();
        } else {
            // 查询历史流程实例
            org.flowable.engine.history.HistoricProcessInstance historicInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (historicInstance == null) {
                throw new RuntimeException("流程实例不存在");
            }
            processDefinitionId = historicInstance.getProcessDefinitionId();
            isFinished = true;
        }

        // 获取流程定义XML
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        InputStream resourceStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                processDefinition.getResourceName());

        String bpmnXml;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = resourceStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            bpmnXml = outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("获取流程XML失败", e);
        }

        // 获取当前活动节点ID（仅运行中流程）
        List<String> activeActivityIds = Collections.emptyList();
        if (!isFinished) {
            activeActivityIds = extendedRuntimeService.getActiveActivityIds(processInstanceId);
        }

        // 获取历史任务信息
        List<org.flowable.task.api.history.HistoricTaskInstance> historicTasks = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        // 构建节点信息映射
        Map<String, Object> nodeInfoMap = new HashMap<>();
        for (org.flowable.task.api.history.HistoricTaskInstance task : historicTasks) {
            String taskId = task.getTaskDefinitionKey();
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("taskId", task.getId());
            nodeInfo.put("taskName", task.getName());
            nodeInfo.put("assignee", task.getAssignee());
            nodeInfo.put("startTime", task.getCreateTime());
            nodeInfo.put("endTime", task.getEndTime());
            nodeInfo.put("duration", task.getDurationInMillis());
            nodeInfoMap.put(taskId, nodeInfo);
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("bpmnXml", bpmnXml);
        result.put("nodeInfo", nodeInfoMap);
        result.put("activeActivityIds", activeActivityIds);
        result.put("isFinished", isFinished);

        return result;
    }

    /**
     * 获取流程定义的BPMN XML
     */
    public Map<String, Object> getProcessDefinitionXml(String processDefinitionId) {
        logger.info("查询流程定义XML, processDefinitionId: {}", processDefinitionId);
        
        // 查询流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (processDefinition == null) {
            logger.error("流程定义不存在, processDefinitionId: {}", processDefinitionId);
            // 返回空的result而不是抛出异常，让前端能处理
            Map<String, Object> result = new HashMap<>();
            result.put("error", "流程定义不存在");
            result.put("processDefinitionId", processDefinitionId);
            return result;
        }

        // 获取流程定义XML
        InputStream resourceStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                processDefinition.getResourceName());

        String bpmnXml;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = resourceStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            bpmnXml = outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("获取流程XML失败", e);
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("bpmnXml", bpmnXml);
        result.put("processDefinitionId", processDefinitionId);
        result.put("name", processDefinition.getName());
        result.put("key", processDefinition.getKey());
        result.put("version", processDefinition.getVersion());
        
        return result;
    }

    // ==================== 流程快照管理 ====================

    @Autowired
    private ProcessSnapshotRepository processSnapshotRepository;

    /**
     * 创建流程快照
     * @param processDefinitionKey 流程定义KEY
     * @param snapshotName 快照名称
     * @param description 快照描述
     * @param creator 创建人
     */
    @Transactional
    public void createProcessSnapshot(String processDefinitionKey, String snapshotName, String description, String creator) {
        // 获取当前最新流程定义的XML
        ProcessDefinition latestDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

        if (latestDefinition == null) {
            throw new RuntimeException("未找到流程定义: " + processDefinitionKey);
        }

        // 获取XML内容
        InputStream resourceStream = repositoryService.getResourceAsStream(
                latestDefinition.getDeploymentId(),
                latestDefinition.getResourceName());

        String bpmnXml;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = resourceStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            bpmnXml = outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("获取流程XML失败", e);
        }

        // 检查快照名称是否重复
        List<ProcessSnapshot> snapshots = processSnapshotRepository.findByProcessDefinitionKey(processDefinitionKey);
        Optional<ProcessSnapshot> existingSnapshot = snapshots.stream()
                .filter(snapshot -> snapshot.getSnapshotName().equals(snapshotName))
                .findFirst();

        if (existingSnapshot.isPresent()) {
            throw new RuntimeException("快照名称已存在: " + snapshotName);
        }

        // 计算下一个快照版本号
        int nextVersion = 1;
        if (!snapshots.isEmpty()) {
            nextVersion = snapshots.stream()
                    .mapToInt(ProcessSnapshot::getSnapshotVersion)
                    .max()
                    .orElse(0) + 1;
        }

        // 保存快照
        ProcessSnapshot snapshot = new ProcessSnapshot();
        snapshot.setProcessDefinitionKey(processDefinitionKey);
        snapshot.setSnapshotName(snapshotName);
        snapshot.setSnapshotVersion(nextVersion);
        snapshot.setBpmnXml(bpmnXml);
        snapshot.setDescription(description);
        snapshot.setCreator(creator);
        snapshot.setCreatedTime(LocalDateTime.now());

        processSnapshotRepository.save(snapshot);
    }

    /**
     * 获取流程快照列表
     * @param processDefinitionKey 流程定义KEY
     * @return 快照列表
     */
    public List<ProcessSnapshot> getProcessSnapshots(String processDefinitionKey) {
        return processSnapshotRepository.findByProcessDefinitionKey(processDefinitionKey);
    }

    /**
     * 回滚到指定快照
     * @param snapshotId 快照ID
     */
    @Transactional
    public void rollbackToSnapshot(String snapshotId) {
        ProcessSnapshot snapshot = processSnapshotRepository.findById(snapshotId);
        if (snapshot == null) {
            throw new RuntimeException("未找到快照: " + snapshotId);
        }

        // 重新部署快照中的BPMN XML
        String snapshotName = snapshot.getSnapshotName() + "_rollback_" + System.currentTimeMillis();
        extendedRepositoryService.deploy(snapshotName, snapshot.getBpmnXml());
    }

    /**
     * 删除快照
     * @param snapshotId 快照ID
     */
    public void deleteSnapshot(String snapshotId) {
        processSnapshotRepository.deleteById(snapshotId);
    }

    // ==================== BPMN元素扩展属性管理 ====================

    @Autowired
    private BpmnElementExtensionRepository bpmnElementExtensionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 保存BPMN元素扩展属性
     * @param processDefinitionId 流程定义ID
     * @param elementId 元素ID
     * @param elementType 元素类型
     * @param extensionAttributes 扩展属性JSON
     */
    @Transactional
    public void saveElementExtension(String processDefinitionId, String elementId, String elementType, JsonNode extensionAttributes) {
        // 检查是否已存在该元素的扩展属性
        BpmnElementExtension existing = bpmnElementExtensionRepository.findByProcessAndElement(processDefinitionId, elementId);
        
        if (existing != null) {
            // 更新现有记录
            existing.setExtensionAttributes(extensionAttributes);
            existing.setUpdatedTime(java.time.LocalDateTime.now());
            bpmnElementExtensionRepository.update(existing);
            
            // 记录历史
            BpmnElementExtensionHistory history = new BpmnElementExtensionHistory();
            history.setExtensionId(existing.getId());
            history.setProcessDefinitionId(processDefinitionId);
            history.setElementId(elementId);
            history.setElementType(elementType);
            history.setExtensionAttributes(extensionAttributes);
            history.setVersion(existing.getVersion());
            history.setOperationType("UPDATE");
            history.setOperationTime(java.time.LocalDateTime.now());
            bpmnElementExtensionRepository.saveHistory(history);
        } else {
            // 创建新记录
            BpmnElementExtension extension = new BpmnElementExtension();
            extension.setProcessDefinitionId(processDefinitionId);
            extension.setElementId(elementId);
            extension.setElementType(elementType);
            extension.setExtensionAttributes(extensionAttributes);
            extension.setVersion(1);
            extension.setCreatedTime(java.time.LocalDateTime.now());
            extension.setUpdatedTime(java.time.LocalDateTime.now());
            bpmnElementExtensionRepository.save(extension);
            
            // 记录历史
            BpmnElementExtensionHistory history = new BpmnElementExtensionHistory();
            history.setExtensionId(extension.getId());
            history.setProcessDefinitionId(processDefinitionId);
            history.setElementId(elementId);
            history.setElementType(elementType);
            history.setExtensionAttributes(extensionAttributes);
            history.setVersion(1);
            history.setOperationType("CREATE");
            history.setOperationTime(java.time.LocalDateTime.now());
            bpmnElementExtensionRepository.saveHistory(history);
        }
    }

    /**
     * 获取BPMN元素扩展属性
     * @param processDefinitionId 流程定义ID
     * @param elementId 元素ID
     * @return 扩展属性查询结果
     */
    public ElementExtensionQueryResult getElementExtension(String processDefinitionId, String elementId) {
        BpmnElementExtension extension = bpmnElementExtensionRepository.findByProcessAndElement(processDefinitionId, elementId);
        
        ElementExtensionQueryResult result = new ElementExtensionQueryResult();
        if (extension != null) {
            result.setElementId(extension.getElementId());
            result.setElementType(extension.getElementType());
            result.setExtensionAttributes(extension.getExtensionAttributes());
            result.setVersion(extension.getVersion());
            result.setExists(true);
        } else {
            result.setElementId(elementId);
            result.setExists(false);
        }
        
        return result;
    }

    /**
     * 批量保存BPMN元素扩展属性
     * @param processDefinitionId 流程定义ID
     * @param extensions 扩展属性列表
     */
    @Transactional
    public void batchSaveElementExtensions(String processDefinitionId, java.util.List<BpmnElementExtensionDTO> extensions) {
        for (BpmnElementExtensionDTO extension : extensions) {
            saveElementExtension(processDefinitionId, extension.getElementId(), 
                               extension.getElementType(), extension.getExtensionAttributes());
        }
    }

    /**
     * 获取流程定义的所有扩展属性
     * @param processDefinitionId 流程定义ID
     * @return 扩展属性列表
     */
    public java.util.List<BpmnElementExtension> getAllElementExtensions(String processDefinitionId) {
        return bpmnElementExtensionRepository.findByProcessDefinitionId(processDefinitionId);
    }

    /**
     * 删除BPMN元素扩展属性
     * @param processDefinitionId 流程定义ID
     * @param elementId 元素ID
     */
    @Transactional
    public void deleteElementExtension(String processDefinitionId, String elementId) {
        BpmnElementExtension existing = bpmnElementExtensionRepository.findByProcessAndElement(processDefinitionId, elementId);
        if (existing != null) {
            // 记录删除历史
            BpmnElementExtensionHistory history = new BpmnElementExtensionHistory();
            history.setExtensionId(existing.getId());
            history.setProcessDefinitionId(processDefinitionId);
            history.setElementId(elementId);
            history.setElementType(existing.getElementType());
            history.setExtensionAttributes(existing.getExtensionAttributes());
            history.setVersion(existing.getVersion());
            history.setOperationType("DELETE");
            history.setOperationTime(java.time.LocalDateTime.now());
            bpmnElementExtensionRepository.saveHistory(history);
            
            // 删除记录
            bpmnElementExtensionRepository.deleteByProcessAndElement(processDefinitionId, elementId);
        }
    }
}
