package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.service.ProcessStatisticsService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 流程统计控制器
 * 提供流程统计相关的API
 */
@RestController
@RequestMapping("/api/statistics")
public class ProcessStatisticsController {

    @Autowired
    private ProcessStatisticsService statisticsService;

    /**
     * 获取流程实例统计
     *
     * @return 统计信息
     */
    @GetMapping("/process-instance")
    public Result<ProcessStatisticsService.ProcessInstanceStatistics> getProcessInstanceStatistics(
            @RequestParam(value = "processDefinitionKey", required = false) String processDefinitionKey) {
        try {
            ProcessStatisticsService.ProcessInstanceStatistics statistics =
                statisticsService.getProcessInstanceStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务统计
     *
     * @param processDefinitionKey 流程定义Key（可选）
     * @return 统计信息
     */
    @GetMapping("/task")
    public Result<ProcessStatisticsService.TaskStatistics> getTaskStatistics(
            @RequestParam(value = "processDefinitionKey", required = false) String processDefinitionKey) {
        try {
            ProcessStatisticsService.TaskStatistics statistics = statisticsService.getTaskStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程定义统计
     *
     * @return 统计信息列表
     */
    @GetMapping("/process-definition")
    public Result<List<ProcessStatisticsService.ProcessDefinitionStatistics>> getProcessDefinitionStatistics() {
        try {
            List<ProcessStatisticsService.ProcessDefinitionStatistics> statistics =
                statisticsService.getProcessDefinitionStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取每日流程统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息列表
     */
    @GetMapping("/daily")
    public Result<Map<String, ProcessStatisticsService.DailyStatistics>> getDailyStatistics(
            @RequestParam(value = "days", defaultValue = "30") Integer days) {
        try {
            Map<String, ProcessStatisticsService.DailyStatistics> statistics =
                statisticsService.getDailyStatistics(days);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户任务统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/user-task/{userId}")
    public Result<ProcessStatisticsService.UserTaskStatistics> getUserTaskStatistics(
            @PathVariable("userId") String userId) {
        try {
            ProcessStatisticsService.UserTaskStatistics statistics = statisticsService.getUserTaskStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程平均完成时间
     *
     * @param processDefinitionKey 流程定义Key（可选）
     * @return 平均完成时间
     */
    @GetMapping("/average-completion-time")
    public Result<ProcessStatisticsService.AverageCompletionTime> getAverageCompletionTime(
            @RequestParam(value = "processDefinitionKey", required = false) String processDefinitionKey) {
        try {
            ProcessStatisticsService.AverageCompletionTime statistics =
                statisticsService.getAverageCompletionTime(processDefinitionKey);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程趋势统计
     *
     * @param days 统计天数
     * @return 趋势数据
     */
    @GetMapping("/trend/{days}")
    public Result<Map<String, Object>> getProcessTrend(
            @PathVariable("days") Integer days) {
        try {
            Map<String, Object> trend = statisticsService.getProcessTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取超时流程统计
     *
     * @param timeoutHours 超时小时数
     * @return 统计信息
     */
    @GetMapping("/timeout/{timeoutHours}")
    public Result<ProcessStatisticsService.TimeoutStatistics> getTimeoutStatistics(
            @PathVariable("timeoutHours") Long timeoutHours) {
        try {
            ProcessStatisticsService.TimeoutStatistics statistics = statisticsService.getTimeoutStatistics(timeoutHours);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
