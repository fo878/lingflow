package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.service.ProcessMonitorService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程监控控制器
 * 提供流程监控和超时检测相关的API
 */
@RestController
@RequestMapping("/api/monitor")
public class ProcessMonitorController {

    @Autowired
    private ProcessMonitorService monitorService;

    /**
     * 获取流程实例监控信息
     *
     * @param processInstanceId 流程实例ID
     * @return 监控信息
     */
    @GetMapping("/process/{processInstanceId}")
    public Result<ProcessMonitorService.ProcessInstanceMonitor> getProcessInstanceMonitor(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            ProcessMonitorService.ProcessInstanceMonitor monitor = monitorService.getProcessInstanceMonitor(processInstanceId);
            return Result.success(monitor);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程定义监控信息
     *
     * @param processDefinitionId 流程定义ID
     * @return 监控信息
     */
    @GetMapping("/definition/{processDefinitionId}")
    public Result<ProcessMonitorService.ProcessDefinitionMonitor> getProcessDefinitionMonitor(
            @PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            ProcessMonitorService.ProcessDefinitionMonitor monitor = monitorService.getProcessDefinitionMonitor(processDefinitionId);
            return Result.success(monitor);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检测超时的流程实例
     *
     * @param timeoutHours 超时小时数
     * @return 超时流程实例列表
     */
    @GetMapping("/timeout/{timeoutHours}")
    public Result<List<String>> detectTimeoutProcesses(
            @PathVariable("timeoutHours") Long timeoutHours) {
        try {
            List<String> timeoutInstances = monitorService.detectTimeoutProcesses(timeoutHours);
            return Result.success(timeoutInstances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有运行中的流程实例监控信息
     *
     * @return 监控信息列表
     */
    @GetMapping("/process/running")
    public Result<List<ProcessMonitorService.ProcessInstanceMonitor>> getAllRunningProcessMonitors() {
        try {
            List<ProcessMonitorService.ProcessInstanceMonitor> monitors = monitorService.getAllRunningProcessMonitors();
            return Result.success(monitors);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取活动节点信息
     *
     * @param processInstanceId 流程实例ID
     * @return 活动节点列表
     */
    @GetMapping("/process/{processInstanceId}/activities")
    public Result<List<ProcessMonitorService.ActivityNode>> getActivityNodes(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            List<ProcessMonitorService.ActivityNode> activities = monitorService.getActivityNodes(processInstanceId);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程进度信息
     *
     * @param processInstanceId 流程实例ID
     * @return 进度信息
     */
    @GetMapping("/process/{processInstanceId}/progress")
    public Result<ProcessMonitorService.ProcessProgress> getProcessProgress(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            ProcessMonitorService.ProcessProgress progress = monitorService.getProcessProgress(processInstanceId);
            return Result.success(progress);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
