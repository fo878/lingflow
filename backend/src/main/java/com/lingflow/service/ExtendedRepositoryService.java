package com.lingflow.service;

import org.flowable.engine.RepositoryService;
import com.lingflow.dto.ProcessDefinitionVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义服务（扩展版）
 * 封装 Flowable RepositoryService，添加扩展逻辑
 */
@Slf4j
@Service
public class ExtendedRepositoryService {

    @Autowired
    private RepositoryService flowableRepositoryService;

    @Autowired
    private FlowableServiceTemplate serviceTemplate;

    /**
     * 部署流程定义
     *
     * @param deploymentName 部署名称
     * @param bpmnXml BPMN XML 内容
     * @return 部署ID
     */
    public String deploy(String deploymentName, String bpmnXml) {
        return serviceTemplate.execute(
            "RepositoryService.deploy",
            () -> {
                DeploymentBuilder deploymentBuilder = flowableRepositoryService
                    .createDeployment()
                    .name(deploymentName)
                    .addInputStream("process.bpmn20.xml",
                        new java.io.ByteArrayInputStream(
                            bpmnXml.getBytes(StandardCharsets.UTF_8)
                        )
                    );

                // 验证 BPMN XML
                validateBpmnXml(bpmnXml);

                return deploymentBuilder.deploy().getId();
            },
            deploymentName, bpmnXml
        );
    }

    /**
     * 创建流程定义查询
     *
     * @return 查询对象
     */
    public ProcessDefinitionQuery createProcessDefinitionQuery() {
        return serviceTemplate.execute(
            "RepositoryService.createProcessDefinitionQuery",
            () -> flowableRepositoryService.createProcessDefinitionQuery()
        );
    }

    /**
     * 查询所有流程定义
     *
     * @return 流程定义列表
     */
    public List<ProcessDefinitionVO> getProcessDefinitions() {
        ProcessDefinitionQuery query = createProcessDefinitionQuery()
            .orderByProcessDefinitionVersion()
            .desc();

        List<ProcessDefinition> definitions = query.list();

        return definitions.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    /**
     * 根据Key查询流程定义
     *
     * @param key 流程定义Key
     * @return 流程定义VO
     */
    public ProcessDefinitionVO getProcessDefinitionByKey(String key) {
        ProcessDefinition definition = createProcessDefinitionQuery()
            .processDefinitionKey(key)
            .latestVersion()
            .singleResult();

        if (definition == null) {
            return null;
        }

        return convertToVO(definition);
    }

    /**
     * 获取流程定义的BPMN XML
     *
     * @param processDefinitionId 流程定义ID
     * @return BPMN XML字符串
     */
    public String getProcessDefinitionXml(String processDefinitionId) {
        return serviceTemplate.execute(
            "RepositoryService.getProcessDefinitionXml",
            () -> {
                ProcessDefinition definition = flowableRepositoryService
                    .createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

                if (definition == null) {
                    throw new RuntimeException(
                        "流程定义不存在: " + processDefinitionId
                    );
                }

                String resourceName = definition.getResourceName();
                InputStream inputStream = flowableRepositoryService
                    .getResourceAsStream(
                        definition.getDeploymentId(),
                        resourceName
                    );

                if (inputStream == null) {
                    throw new RuntimeException(
                        "无法获取流程资源: " + resourceName
                    );
                }

                try {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("读取流程资源失败", e);
                }
            },
            processDefinitionId
        );
    }

    /**
     * 删除部署
     *
     * @param deploymentId 部署ID
     * @param cascade 是否级联删除
     * @param skipCustomListeners 是否跳过自定义监听器
     */
    public void deleteDeployment(
        String deploymentId,
        boolean cascade,
        boolean skipCustomListeners
    ) {
        serviceTemplate.execute(
            "RepositoryService.deleteDeployment",
            () -> {
                // Flowable 7.x: deleteDeployment 只接受 deploymentId 和 cascade 参数
                flowableRepositoryService.deleteDeployment(
                    deploymentId,
                    cascade
                );
                return null;
            },
            deploymentId, cascade
        );
    }

    /**
     * 验证 BPMN XML
     *
     * @param bpmnXml BPMN XML 字符串
     */
    private void validateBpmnXml(String bpmnXml) {
        try {
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

            // 将 String 转换为 XMLStreamReader
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newFactory();
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(bpmnXml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);

            // 验证基本结构
            if (model == null) {
                throw new RuntimeException("无效的 BPMN XML");
            }

            // 可以添加更多验证逻辑
            log.debug("BPMN XML validation passed");

        } catch (Exception e) {
            throw new RuntimeException("BPMN XML 验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换为VO
     *
     * @param definition 流程定义实体
     * @return 流程定义VO
     */
    private ProcessDefinitionVO convertToVO(ProcessDefinition definition) {
        return ProcessDefinitionVO.builder()
            .id(definition.getId())
            .key(definition.getKey())
            .name(definition.getName())
            .version(definition.getVersion())
            .deploymentId(definition.getDeploymentId())
            .resource(definition.getResourceName())
            .suspended(definition.isSuspended())
            .build();
    }
}
