package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 流程实例运行时服务（扩展版）
 * 封装 Flowable RuntimeService，添加扩展逻辑
 */
@Slf4j
@Service
public class ExtendedRuntimeService {

    @Autowired
    private org.flowable.engine.RuntimeService flowableRuntimeService;

    @Autowired
    private FlowableServiceTemplate serviceTemplate;

    /**
     * 根据流程定义Key启动流程实例
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey 业务Key
     * @param variables 流程变量
     * @return 流程实例VO
     */
    public ProcessInstanceVO startProcessInstanceByKey(
        String processDefinitionKey,
        String businessKey,
        Map<String, Object> variables
    ) {
        return serviceTemplate.execute(
            "RuntimeService.startProcessInstanceByKey",
            () -> {
                ProcessInstance instance;
                if (businessKey != null && !businessKey.isEmpty()) {
                    if (variables != null && !variables.isEmpty()) {
                        instance = flowableRuntimeService.startProcessInstanceByKey(
                            processDefinitionKey,
                            businessKey,
                            variables
                        );
                    } else {
                        instance = flowableRuntimeService.startProcessInstanceByKey(
                            processDefinitionKey,
                            businessKey
                        );
                    }
                } else {
                    if (variables != null && !variables.isEmpty()) {
                        instance = flowableRuntimeService.startProcessInstanceByKey(
                            processDefinitionKey,
                            variables
                        );
                    } else {
                        instance = flowableRuntimeService.startProcessInstanceByKey(
                            processDefinitionKey
                        );
                    }
                }

                return convertToVO(instance);
            },
            processDefinitionKey, businessKey, variables
        );
    }

    /**
     * 创建流程实例查询
     *
     * @return 查询对象
     */
    public ProcessInstanceQuery createProcessInstanceQuery() {
        return serviceTemplate.execute(
            "RuntimeService.createProcessInstanceQuery",
            () -> flowableRuntimeService.createProcessInstanceQuery()
        );
    }

    /**
     * 获取运行中的流程实例列表
     *
     * @return 流程实例VO列表
     */
    public List<ProcessInstanceVO> getRunningProcessInstances() {
        ProcessInstanceQuery query = createProcessInstanceQuery()
            .orderByStartTime()
            .desc();

        List<ProcessInstance> instances = query.list();

        return instances.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据流程实例ID获取活动节点ID列表
     *
     * @param processInstanceId 流程实例ID
     * @return 活动节点ID列表
     */
    public List<String> getActiveActivityIds(String processInstanceId) {
        return serviceTemplate.execute(
            "RuntimeService.getActiveActivityIds",
            () -> flowableRuntimeService.getActiveActivityIds(processInstanceId),
            processInstanceId
        );
    }

    /**
     * 根据流程实例ID获取流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例VO
     */
    public ProcessInstanceVO getProcessInstance(String processInstanceId) {
        ProcessInstance instance = createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (instance == null) {
            return null;
        }

        return convertToVO(instance);
    }

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param deleteReason 删除原因
     * @param cascade 是否级联删除
     */
    public void deleteProcessInstance(
        String processInstanceId,
        String deleteReason,
        boolean cascade
    ) {
        serviceTemplate.execute(
            "RuntimeService.deleteProcessInstance",
            () -> {
                flowableRuntimeService.deleteProcessInstance(
                    processInstanceId,
                    deleteReason
                );
                return null;
            },
            processInstanceId, deleteReason, cascade
        );
    }

    /**
     * 挂起流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void suspendProcessInstance(String processInstanceId) {
        serviceTemplate.execute(
            "RuntimeService.suspendProcessInstanceById",
            () -> {
                flowableRuntimeService.suspendProcessInstanceById(processInstanceId);
                return null;
            },
            processInstanceId
        );
    }

    /**
     * 激活流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    public void activateProcessInstance(String processInstanceId) {
        serviceTemplate.execute(
            "RuntimeService.activateProcessInstanceById",
            () -> {
                flowableRuntimeService.activateProcessInstanceById(processInstanceId);
                return null;
            },
            processInstanceId
        );
    }

    /**
     * 转换为VO
     *
     * @param instance 流程实例实体
     * @return 流程实例VO
     */
    private ProcessInstanceVO convertToVO(ProcessInstance instance) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setStartTime(instance.getStartTime());
        return vo;
    }
}
