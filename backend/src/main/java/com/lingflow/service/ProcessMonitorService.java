package com.lingflow.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程监控服务
 * 提供流程实例、任务的实时监控信息
 */
@Slf4j
@Service
public class ProcessMonitorService {

    @Autowired
    private ExtendedRuntimeService extendedRuntimeService;

    @Autowired
    private ExtendedTaskService extendedTaskService;

    @Autowired
    private ExtendedHistoryService extendedHistoryService;

    /**
     * 获取流程实例监控信息
     *
     * @param processInstanceId 流程实例ID
     * @return 监控信息
     */
    public ProcessInstanceMonitor getProcessInstanceMonitor(String processInstanceId) {
        // 获取流程实例信息
        ProcessInstance instance = extendedRuntimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (instance == null) {
            throw new RuntimeException("流程实例不存在: " + processInstanceId);
        }

        ProcessInstanceMonitor monitor = new ProcessInstanceMonitor();
        monitor.setProcessInstanceId(processInstanceId);
        monitor.setProcessDefinitionId(instance.getProcessDefinitionId());
        monitor.setBusinessKey(instance.getBusinessKey());
        monitor.setStartTime(instance.getStartTime());
        monitor.setTenantId(instance.getTenantId());

        // 计算运行时长
        if (instance.getStartTime() != null) {
            Duration duration = Duration.between(
                instance.getStartTime().toInstant(),
                new Date().toInstant()
            );
            monitor.setRunningDuration(duration.toMillis());
        }

        // 获取当前活动节点
        List<String> activeActivityIds = extendedRuntimeService.getActiveActivityIds(processInstanceId);
        monitor.setActiveActivityIds(activeActivityIds);
        monitor.setActiveActivityCount(activeActivityIds.size());

        // 获取当前待办任务
        List<com.lingflow.dto.TaskVO> tasks = extendedTaskService.getTasksByProcessInstanceId(processInstanceId);
        monitor.setCurrentTasks(tasks);
        monitor.setCurrentTaskCount(tasks.size());

        // 获取待办任务办理人
        Set<String> assignees = tasks.stream()
            .map(com.lingflow.dto.TaskVO::getAssignee)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        monitor.setCurrentAssignees(new ArrayList<>(assignees));

        // 计算超时任务
        long overdueTaskCount = tasks.stream()
            .filter(task -> {
                // 这里可以添加超时判断逻辑
                // 例如：任务的 dueDate 早于当前时间
                return false; // 简化实现
            })
            .count();
        monitor.setOverdueTaskCount((int) overdueTaskCount);

        // 计算进度（基于历史任务）
        try {
            List<com.lingflow.dto.TaskVO> historicTasks = extendedHistoryService
                .getHistoricTasksByProcessInstanceId(processInstanceId);

            int totalTasks = historicTasks.size() + tasks.size();
            int completedTasks = historicTasks.size();
            monitor.setProgress(totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0);
        } catch (Exception e) {
            log.debug("无法计算流程进度: {}", e.getMessage());
            monitor.setProgress(0.0);
        }

        // 设置监控时间
        monitor.setMonitorTime(LocalDateTime.now());

        // 状态判断
        monitor.setStatus("RUNNING");

        return monitor;
    }

    /**
     * 获取流程定义的运行实例统计
     *
     * @param processDefinitionKey 流程定义Key
     * @return 统计信息
     */
    public ProcessDefinitionMonitor getProcessDefinitionMonitor(String processDefinitionKey) {
        ProcessDefinitionMonitor monitor = new ProcessDefinitionMonitor();
        monitor.setProcessDefinitionKey(processDefinitionKey);

        // 获取运行中的实例
        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances().stream()
                .filter(instance -> instance.getProcessDefinitionId().startsWith(processDefinitionKey + ":"))
                .collect(Collectors.toList());

        monitor.setRunningInstanceCount(runningInstances.size());

        // 获取已完结的实例
        List<com.lingflow.dto.ProcessInstanceVO> completedInstances =
            extendedHistoryService.getCompletedProcessInstances().stream()
                .filter(instance -> instance.getProcessDefinitionId().startsWith(processDefinitionKey + ":"))
                .collect(Collectors.toList());

        monitor.setCompletedInstanceCount(completedInstances.size());

        // 计算平均完成时间
        if (!completedInstances.isEmpty()) {
            long totalDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .sum();

            long avgDuration = totalDuration / completedInstances.size();
            monitor.setAvgCompletionTime(avgDuration);
        }

        // 计算总实例数
        monitor.setTotalInstanceCount(
            runningInstances.size() + completedInstances.size()
        );

        // 获取当前待办任务数
        int currentTaskCount = runningInstances.stream()
            .mapToInt(instance -> {
                try {
                    return extendedTaskService.getTasksByProcessInstanceId(instance.getId()).size();
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        monitor.setCurrentTaskCount(currentTaskCount);

        // 计算完成率
        if (monitor.getTotalInstanceCount() > 0) {
            monitor.setCompletionRate(
                (double) monitor.getCompletedInstanceCount() / monitor.getTotalInstanceCount() * 100
            );
        }

        monitor.setMonitorTime(LocalDateTime.now());

        return monitor;
    }

    /**
     * 获取所有运行中实例的监控信息
     *
     * @return 监控信息列表
     */
    public List<ProcessInstanceMonitor> getAllRunningInstancesMonitor() {
        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances();

        return runningInstances.stream()
            .map(instance -> {
                try {
                    return getProcessInstanceMonitor(instance.getId());
                } catch (Exception e) {
                    log.error("获取流程实例监控信息失败: {}", instance.getId(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 检查流程实例是否超时
     *
     * @param processInstanceId 流程实例ID
     * @param timeoutMinutes 超时时间（分钟）
     * @return 是否超时
     */
    public boolean isProcessInstanceTimeout(
        String processInstanceId,
        long timeoutMinutes
    ) {
        ProcessInstance instance = extendedRuntimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        if (instance == null) {
            return false;
        }

        if (instance.getStartTime() != null) {
            Duration duration = Duration.between(
                instance.getStartTime().toInstant(),
                new Date().toInstant()
            );

            return duration.toMinutes() > timeoutMinutes;
        }

        return false;
    }

    /**
     * 获取超时的流程实例列表
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时实例列表
     */
    public List<String> getTimeoutProcessInstances(long timeoutMinutes) {
        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances();

        return runningInstances.stream()
            .filter(instance -> isProcessInstanceTimeout(instance.getId(), timeoutMinutes))
            .map(com.lingflow.dto.ProcessInstanceVO::getId)
            .collect(Collectors.toList());
    }

    /**
     * 流程实例监控信息
     */
    @Data
    public static class ProcessInstanceMonitor {
        private String processInstanceId;
        private String processDefinitionId;
        private String businessKey;
        private String tenantId;
        private Date startTime;
        private Long runningDuration; // 毫秒
        private List<String> activeActivityIds;
        private Integer activeActivityCount;
        private List<com.lingflow.dto.TaskVO> currentTasks;
        private Integer currentTaskCount;
        private List<String> currentAssignees;
        private Integer overdueTaskCount;
        private Double progress; // 百分比
        private String status; // RUNNING, SUSPENDED, ERROR
        private LocalDateTime monitorTime;
    }

    /**
     * 流程定义监控信息
     */
    @Data
    public static class ProcessDefinitionMonitor {
        private String processDefinitionKey;
        private Integer runningInstanceCount;
        private Integer completedInstanceCount;
        private Integer totalInstanceCount;
        private Integer currentTaskCount;
        private Long avgCompletionTime; // 毫秒
        private Double completionRate; // 百分比
        private LocalDateTime monitorTime;
    }

    /**
     * 检测超时流程实例
     *
     * @param timeoutHours 超时小时数
     * @return 超时流程实例ID列表
     */
    public List<String> detectTimeoutProcesses(Long timeoutHours) {
        log.debug("检测超时流程 - 超时小时数: {}", timeoutHours);
        long timeoutMinutes = timeoutHours * 60;
        return getTimeoutProcessInstances(timeoutMinutes);
    }

    /**
     * 获取所有运行中的流程实例监控信息
     *
     * @return 监控信息列表
     */
    public List<ProcessInstanceMonitor> getAllRunningProcessMonitors() {
        log.debug("获取所有运行中的流程实例监控信息");
        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances();

        return runningInstances.stream()
            .map(instance -> {
                try {
                    return getProcessInstanceMonitor(instance.getId());
                } catch (Exception e) {
                    log.error("获取流程实例监控信息失败: {}", instance.getId(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 获取活动节点信息
     *
     * @param processInstanceId 流程实例ID
     * @return 活动节点列表
     */
    public List<ActivityNode> getActivityNodes(String processInstanceId) {
        log.debug("获取活动节点 - 流程实例ID: {}", processInstanceId);

        List<String> activeActivityIds = extendedRuntimeService.getActiveActivityIds(processInstanceId);
        List<ActivityNode> nodes = new ArrayList<>();

        for (String activityId : activeActivityIds) {
            ActivityNode node = new ActivityNode();
            node.setActivityId(activityId);
            node.setActivityName("Activity-" + activityId); // 简化实现
            node.setActivityType("USER_TASK"); // 简化实现
            node.setStartTime(LocalDateTime.now().toString());
            nodes.add(node);
        }

        return nodes;
    }

    /**
     * 获取流程进度信息
     *
     * @param processInstanceId 流程实例ID
     * @return 进度信息
     */
    public ProcessProgress getProcessProgress(String processInstanceId) {
        log.debug("获取流程进度 - 流程实例ID: {}", processInstanceId);

        ProcessInstanceMonitor monitor = getProcessInstanceMonitor(processInstanceId);

        ProcessProgress progress = new ProcessProgress();
        progress.setProcessInstanceId(processInstanceId);

        // 简化实现：设置进度信息
        progress.setProgress(monitor.getProgress());
        progress.setProgressDescription(String.format("进度: %.1f%%", monitor.getProgress()));

        // 这里可以添加更详细的进度计算逻辑
        progress.setTotalNodes(10); // 简化实现
        progress.setCompletedNodes((int) (monitor.getProgress() * 10 / 100));
        progress.setCurrentNodes(monitor.getActiveActivityCount());
        progress.setRemainingNodes(10 - progress.getCompletedNodes());

        return progress;
    }

    /**
     * 活动节点信息
     */
    @Data
    public static class ActivityNode {
        private String activityId;
        private String activityName;
        private String activityType;
        private String startTime;
    }

    /**
     * 流程进度信息
     */
    @Data
    public static class ProcessProgress {
        private String processInstanceId;
        private Integer totalNodes;
        private Integer completedNodes;
        private Integer currentNodes;
        private Integer remainingNodes;
        private Double progress;
        private String progressDescription;
    }
}
