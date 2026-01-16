package com.lingflow.service;

import com.lingflow.dto.TaskVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 任务服务（扩展版）
 * 封装 Flowable TaskService，添加扩展逻辑
 */
@Slf4j
@Service
public class ExtendedTaskService {

    @Autowired
    private org.flowable.engine.TaskService flowableTaskService;

    @Autowired
    private FlowableServiceTemplate serviceTemplate;

    /**
     * 创建任务查询
     *
     * @return 查询对象
     */
    public TaskQuery createTaskQuery() {
        return serviceTemplate.execute(
            "TaskService.createTaskQuery",
            () -> flowableTaskService.createTaskQuery()
        );
    }

    /**
     * 获取所有待办任务
     *
     * @return 任务VO列表
     */
    public List<TaskVO> getTasks() {
        TaskQuery query = createTaskQuery()
            .orderByTaskCreateTime()
            .desc();

        List<Task> tasks = query.list();

        return tasks.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据办理人获取待办任务
     *
     * @param assignee 办理人
     * @return 任务VO列表
     */
    public List<TaskVO> getTasksByAssignee(String assignee) {
        List<Task> tasks = createTaskQuery()
            .taskAssignee(assignee)
            .orderByTaskCreateTime()
            .desc()
            .list();

        return tasks.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据流程实例ID获取待办任务
     *
     * @param processInstanceId 流程实例ID
     * @return 任务VO列表
     */
    public List<TaskVO> getTasksByProcessInstanceId(String processInstanceId) {
        List<Task> tasks = createTaskQuery()
            .processInstanceId(processInstanceId)
            .orderByTaskCreateTime()
            .desc()
            .list();

        return tasks.stream()
            .map(this::convertToVO)
            .toList();
    }

    /**
     * 根据任务ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务VO
     */
    public TaskVO getTask(String taskId) {
        Task task = flowableTaskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();

        if (task == null) {
            return null;
        }

        return convertToVO(task);
    }

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param variables 流程变量
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        serviceTemplate.execute(
            "TaskService.completeTask",
            () -> {
                if (variables != null && !variables.isEmpty()) {
                    flowableTaskService.complete(taskId, variables);
                } else {
                    flowableTaskService.complete(taskId);
                }
                return null;
            },
            taskId, variables
        );
    }

    /**
     * 认领任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void claim(String taskId, String userId) {
        serviceTemplate.execute(
            "TaskService.claim",
            () -> {
                flowableTaskService.claim(taskId, userId);
                return null;
            },
            taskId, userId
        );
    }

    /**
     * 取消认领任务
     *
     * @param taskId 任务ID
     */
    public void unclaim(String taskId) {
        serviceTemplate.execute(
            "TaskService.unclaim",
            () -> {
                flowableTaskService.unclaim(taskId);
                return null;
            },
            taskId
        );
    }

    /**
     * 转办任务
     *
     * @param taskId 任务ID
     * @param userId 目标用户ID
     */
    public void delegateTask(String taskId, String userId) {
        serviceTemplate.execute(
            "TaskService.delegateTask",
            () -> {
                flowableTaskService.delegateTask(taskId, userId);
                return null;
            },
            taskId, userId
        );
    }

    /**
     * 分配任务
     *
     * @param taskId 任务ID
     * @param userId 目标用户ID
     */
    public void setAssignee(String taskId, String userId) {
        serviceTemplate.execute(
            "TaskService.setAssignee",
            () -> {
                flowableTaskService.setAssignee(taskId, userId);
                return null;
            },
            taskId, userId
        );
    }

    /**
     * 添加任务候选人（用户）
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void addCandidateUser(String taskId, String userId) {
        serviceTemplate.execute(
            "TaskService.addCandidateUser",
            () -> {
                flowableTaskService.addCandidateUser(taskId, userId);
                return null;
            },
            taskId, userId
        );
    }

    /**
     * 添加任务候选人（组）
     *
     * @param taskId 任务ID
     * @param groupId 组ID
     */
    public void addCandidateGroup(String taskId, String groupId) {
        serviceTemplate.execute(
            "TaskService.addCandidateGroup",
            () -> {
                flowableTaskService.addCandidateGroup(taskId, groupId);
                return null;
            },
            taskId, groupId
        );
    }

    /**
     * 删除任务候选人（用户）
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void deleteCandidateUser(String taskId, String userId) {
        serviceTemplate.execute(
            "TaskService.deleteCandidateUser",
            () -> {
                flowableTaskService.deleteCandidateUser(taskId, userId);
                return null;
            },
            taskId, userId
        );
    }

    /**
     * 删除任务候选人（组）
     *
     * @param taskId 任务ID
     * @param groupId 组ID
     */
    public void deleteCandidateGroup(String taskId, String groupId) {
        serviceTemplate.execute(
            "TaskService.deleteCandidateGroup",
            () -> {
                flowableTaskService.deleteCandidateGroup(taskId, groupId);
                return null;
            },
            taskId, groupId
        );
    }

    /**
     * 添加任务评论
     *
     * @param taskId 任务ID
     * @param processInstanceId 流程实例ID
     * @param message 评论内容
     */
    public void addComment(String taskId, String processInstanceId, String message) {
        serviceTemplate.execute(
            "TaskService.addComment",
            () -> {
                flowableTaskService.addComment(taskId, processInstanceId, message);
                return null;
            },
            taskId, processInstanceId, message
        );
    }

    /**
     * 设置流程变量
     *
     * @param taskId 任务ID
     * @param variables 流程变量
     */
    public void setVariables(String taskId, Map<String, Object> variables) {
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
     * 转换为VO
     *
     * @param task 任务实体
     * @return 任务VO
     */
    private TaskVO convertToVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(task.getCreateTime());
        vo.setAssignee(task.getAssignee());
        return vo;
    }
}
