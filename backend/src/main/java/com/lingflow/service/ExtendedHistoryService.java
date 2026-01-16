package com.lingflow.service;

import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.dto.TaskVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 历史服务（扩展版）
 * 封装 Flowable HistoryService，添加扩展逻辑
 */
@Slf4j
@Service
public class ExtendedHistoryService {

    @Autowired
    private org.flowable.engine.HistoryService flowableHistoryService;

    @Autowired
    private FlowableServiceTemplate serviceTemplate;

    /**
     * 创建历史流程实例查询
     *
     * @return 查询对象
     */
    public org.flowable.engine.history.HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
        return serviceTemplate.execute(
            "HistoryService.createHistoricProcessInstanceQuery",
            () -> flowableHistoryService.createHistoricProcessInstanceQuery()
        );
    }

    /**
     * 创建历史任务查询
     *
     * @return 查询对象
     */
    public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
        return serviceTemplate.execute(
            "HistoryService.createHistoricTaskInstanceQuery",
            () -> flowableHistoryService.createHistoricTaskInstanceQuery()
        );
    }

    /**
     * 获取已完结的流程实例列表
     *
     * @return 流程实例VO列表
     */
    public List<ProcessInstanceVO> getCompletedProcessInstances() {
        List<HistoricProcessInstance> instances = createHistoricProcessInstanceQuery()
            .finished()
            .orderByProcessInstanceEndTime()
            .desc()
            .list();

        return instances.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据流程实例ID获取历史流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例VO
     */
    public ProcessInstanceVO getHistoricProcessInstance(String processInstanceId) {
        HistoricProcessInstance instance = createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (instance == null) {
            return null;
        }

        return convertToVO(instance);
    }

    /**
     * 根据流程定义Key获取历史流程实例列表
     *
     * @param processDefinitionKey 流程定义Key
     * @return 流程实例VO列表
     */
    public List<ProcessInstanceVO> getHistoricProcessInstancesByKey(String processDefinitionKey) {
        List<HistoricProcessInstance> instances = createHistoricProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .orderByProcessInstanceStartTime()
            .desc()
            .list();

        return instances.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 获取已办任务列表
     *
     * @param assignee 办理人
     * @return 任务VO列表
     */
    public List<TaskVO> getHistoricTasksByAssignee(String assignee) {
        List<HistoricTaskInstance> tasks = createHistoricTaskInstanceQuery()
            .taskAssignee(assignee)
            .finished()
            .orderByHistoricTaskInstanceEndTime()
            .desc()
            .list();

        return tasks.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据流程实例ID获取历史任务列表
     *
     * @param processInstanceId 流程实例ID
     * @return 任务VO列表
     */
    public List<TaskVO> getHistoricTasksByProcessInstanceId(String processInstanceId) {
        List<HistoricTaskInstance> tasks = createHistoricTaskInstanceQuery()
            .processInstanceId(processInstanceId)
            .orderByHistoricTaskInstanceEndTime()
            .desc()
            .list();

        return tasks.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据任务ID获取历史任务
     *
     * @param taskId 任务ID
     * @return 任务VO
     */
    public TaskVO getHistoricTask(String taskId) {
        HistoricTaskInstance task = createHistoricTaskInstanceQuery()
            .taskId(taskId)
            .singleResult();

        if (task == null) {
            return null;
        }

        return convertToVO(task);
    }

    /**
     * 删除历史流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param cascade 是否级联删除
     */
    public void deleteHistoricProcessInstance(String processInstanceId, boolean cascade) {
        serviceTemplate.execute(
            "HistoryService.deleteHistoricProcessInstance",
            () -> {
                // Flowable 7.x: deleteHistoricProcessInstance 只接受 processInstanceId 参数
                flowableHistoryService.deleteHistoricProcessInstance(processInstanceId);
                return null;
            },
            processInstanceId
        );
    }

    /**
     * 转换为VO
     *
     * @param instance 历史流程实例实体
     * @return 流程实例VO
     */
    private ProcessInstanceVO convertToVO(HistoricProcessInstance instance) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setStartTime(instance.getStartTime());
        vo.setEndTime(instance.getEndTime());
        return vo;
    }

    /**
     * 转换为VO
     *
     * @param task 历史任务实体
     * @return 任务VO
     */
    private TaskVO convertToVO(HistoricTaskInstance task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(task.getCreateTime());
        vo.setAssignee(task.getAssignee());
        return vo;
    }
}
