package com.lingflow.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程统计服务
 * 提供流程实例、任务、性能等统计数据
 */
@Slf4j
@Service
public class ProcessStatisticsService {

    @Autowired
    private ExtendedRuntimeService extendedRuntimeService;

    @Autowired
    private ExtendedTaskService extendedTaskService;

    @Autowired
    private ExtendedHistoryService extendedHistoryService;

    @Autowired
    private ExtendedRepositoryService extendedRepositoryService;

    /**
     * 获取流程实例统计
     *
     * @return 统计数据
     */
    public ProcessInstanceStatistics getProcessInstanceStatistics() {
        ProcessInstanceStatistics stats = new ProcessInstanceStatistics();

        // 获取运行中的实例数
        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances();
        stats.setRunningCount(runningInstances.size());

        // 获取已完结的实例数
        List<com.lingflow.dto.ProcessInstanceVO> completedInstances =
            extendedHistoryService.getCompletedProcessInstances();
        stats.setCompletedCount(completedInstances.size());

        // 总实例数
        stats.setTotalCount(stats.getRunningCount() + stats.getCompletedCount());

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

            stats.setAvgCompletionTime(totalDuration / completedInstances.size());

            // 计算最短和最长完成时间
            OptionalLong minDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .min();

            OptionalLong maxDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .max();

            minDuration.ifPresent(stats::setMinCompletionTime);
            maxDuration.ifPresent(stats::setMaxCompletionTime);
        }

        // 计算完成率
        if (stats.getTotalCount() > 0) {
            stats.setCompletionRate(
                (double) stats.getCompletedCount() / stats.getTotalCount() * 100
            );
        }

        stats.setStatisticsTime(LocalDateTime.now());

        return stats;
    }

    /**
     * 获取任务统计
     *
     * @return 统计数据
     */
    public TaskStatistics getTaskStatistics() {
        TaskStatistics stats = new TaskStatistics();

        // 获取当前待办任务数
        List<com.lingflow.dto.TaskVO> pendingTasks = extendedTaskService.getTasks();
        stats.setPendingCount(pendingTasks.size());

        // 获取已办任务数
        List<com.lingflow.dto.TaskVO> completedTasks = extendedHistoryService
            .getHistoricTasksByAssignee("all"); // 简化实现
        stats.setCompletedCount(completedTasks.size());

        // 总任务数
        stats.setTotalCount(stats.getPendingCount() + stats.getCompletedCount());

        // 计算平均任务处理时间
        if (!completedTasks.isEmpty()) {
            // 这里需要从历史任务中获取处理时间
            // 简化实现：设置默认值
            stats.setAvgProcessingTime(0L);
        }

        // 计算完成率
        if (stats.getTotalCount() > 0) {
            stats.setCompletionRate(
                (double) stats.getCompletedCount() / stats.getTotalCount() * 100
            );
        }

        // 统计任务状态分布
        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("pending", stats.getPendingCount());
        statusDistribution.put("completed", stats.getCompletedCount());
        stats.setStatusDistribution(statusDistribution);

        stats.setStatisticsTime(LocalDateTime.now());

        return stats;
    }

    /**
     * 获取流程定义统计
     *
     * @return 统计数据
     */
    public List<ProcessDefinitionStatistics> getProcessDefinitionStatistics() {
        List<com.lingflow.dto.ProcessDefinitionVO> definitions =
            extendedRepositoryService.getProcessDefinitions();

        return definitions.stream()
            .map(definition -> {
                ProcessDefinitionStatistics stats = new ProcessDefinitionStatistics();
                stats.setProcessDefinitionKey(definition.getKey());
                stats.setProcessDefinitionName(definition.getName());
                stats.setVersion(definition.getVersion());

                // 获取该流程定义的运行实例数
                String processDefinitionId = definition.getId();
                List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
                    extendedRuntimeService.getRunningProcessInstances().stream()
                        .filter(instance -> instance.getProcessDefinitionId().equals(processDefinitionId))
                        .collect(Collectors.toList());
                stats.setRunningInstanceCount(runningInstances.size());

                // 获取该流程定义的已完成实例数
                List<com.lingflow.dto.ProcessInstanceVO> completedInstances =
                    extendedHistoryService.getCompletedProcessInstances().stream()
                        .filter(instance -> instance.getProcessDefinitionId().equals(processDefinitionId))
                        .collect(Collectors.toList());
                stats.setCompletedInstanceCount(completedInstances.size());

                // 总实例数
                stats.setTotalInstanceCount(
                    stats.getRunningInstanceCount() + stats.getCompletedInstanceCount()
                );

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

                    stats.setAvgCompletionTime(totalDuration / completedInstances.size());
                }

                // 计算完成率
                if (stats.getTotalInstanceCount() > 0) {
                    stats.setCompletionRate(
                        (double) stats.getCompletedInstanceCount() / stats.getTotalInstanceCount() * 100
                    );
                }

                stats.setStatisticsTime(LocalDateTime.now());

                return stats;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取每日流程实例统计
     *
     * @param days 统计天数
     * @return 按日期分组的统计数据
     */
    public Map<String, DailyStatistics> getDailyStatistics(int days) {
        Map<String, DailyStatistics> dailyStats = new LinkedHashMap<>();

        // 初始化日期
        LocalDateTime now = LocalDateTime.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DailyStatistics stats = new DailyStatistics();
            stats.setDate(dateStr);
            stats.setStartedCount(0);
            stats.setCompletedCount(0);
            dailyStats.put(dateStr, stats);
        }

        // 获取已完结的流程实例
        List<com.lingflow.dto.ProcessInstanceVO> completedInstances =
            extendedHistoryService.getCompletedProcessInstances();

        // 统计每日启动和完成的流程数
        for (com.lingflow.dto.ProcessInstanceVO instance : completedInstances) {
            if (instance.getStartTime() != null) {
                LocalDateTime startTime = instance.getStartTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                String startDateStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (dailyStats.containsKey(startDateStr)) {
                    DailyStatistics stats = dailyStats.get(startDateStr);
                    stats.setStartedCount(stats.getStartedCount() + 1);
                }
            }

            if (instance.getEndTime() != null) {
                LocalDateTime endTime = instance.getEndTime().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
                String endDateStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (dailyStats.containsKey(endDateStr)) {
                    DailyStatistics stats = dailyStats.get(endDateStr);
                    stats.setCompletedCount(stats.getCompletedCount() + 1);
                }
            }
        }

        return dailyStats;
    }

    /**
     * 流程实例统计数据
     */
    @Data
    public static class ProcessInstanceStatistics {
        private Integer totalCount;
        private Integer runningCount;
        private Integer completedCount;
        private Long avgCompletionTime; // 毫秒
        private Long minCompletionTime; // 毫秒
        private Long maxCompletionTime; // 毫秒
        private Double completionRate; // 百分比
        private LocalDateTime statisticsTime;
    }

    /**
     * 任务统计数据
     */
    @Data
    public static class TaskStatistics {
        private Integer totalCount;
        private Integer pendingCount;
        private Integer completedCount;
        private Long avgProcessingTime; // 毫秒
        private Double completionRate; // 百分比
        private Map<String, Integer> statusDistribution;
        private LocalDateTime statisticsTime;
    }

    /**
     * 流程定义统计数据
     */
    @Data
    public static class ProcessDefinitionStatistics {
        private String processDefinitionKey;
        private String processDefinitionName;
        private Integer version;
        private Integer totalInstanceCount;
        private Integer runningInstanceCount;
        private Integer completedInstanceCount;
        private Long avgCompletionTime; // 毫秒
        private Double completionRate; // 百分比
        private LocalDateTime statisticsTime;
    }

    /**
     * 每日统计数据
     */
    @Data
    public static class DailyStatistics {
        private String date;
        private Integer startedCount;
        private Integer completedCount;
    }

    /**
     * 获取用户任务统计
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    public UserTaskStatistics getUserTaskStatistics(String userId) {
        log.debug("获取用户任务统计 - 用户ID: {}", userId);
        UserTaskStatistics stats = new UserTaskStatistics();
        stats.setUserId(userId);

        // 获取用户待办任务数
        List<com.lingflow.dto.TaskVO> pendingTasks = extendedTaskService.getTasksByAssignee(userId);
        stats.setPendingTaskCount((long) pendingTasks.size());

        // 获取用户已办任务数
        List<com.lingflow.dto.TaskVO> completedTasks = extendedHistoryService
            .getHistoricTasksByAssignee(userId);
        stats.setCompletedTaskCount((long) completedTasks.size());

        // 计算超时任务数（简化实现）
        stats.setOverdueTaskCount(0L);

        // 计算平均任务处理时间（简化实现）
        stats.setAverageTaskTime(0L);

        return stats;
    }

    /**
     * 获取平均完成时间
     *
     * @param processDefinitionKey 流程定义Key（可选）
     * @return 平均完成时间数据
     */
    public AverageCompletionTime getAverageCompletionTime(String processDefinitionKey) {
        log.debug("获取平均完成时间 - 流程定义Key: {}", processDefinitionKey);
        AverageCompletionTime time = new AverageCompletionTime();
        time.setProcessDefinitionKey(processDefinitionKey);

        List<com.lingflow.dto.ProcessInstanceVO> completedInstances =
            extendedHistoryService.getCompletedProcessInstances();

        // 如果指定了流程定义Key，进行过滤
        if (processDefinitionKey != null && !processDefinitionKey.isEmpty()) {
            completedInstances = completedInstances.stream()
                .filter(instance -> instance.getProcessDefinitionId().startsWith(processDefinitionKey + ":"))
                .collect(Collectors.toList());
        }

        if (!completedInstances.isEmpty()) {
            // 计算平均完成时间
            long totalDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .sum();

            long count = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .count();

            if (count > 0) {
                time.setAverageCompletionTime(totalDuration / count);
            }

            // 计算最快完成时间
            OptionalLong minDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .min();
            minDuration.ifPresent(time::setFastestCompletionTime);

            // 计算最慢完成时间
            OptionalLong maxDuration = completedInstances.stream()
                .filter(instance -> instance.getStartTime() != null && instance.getEndTime() != null)
                .mapToLong(instance -> {
                    return Duration.between(
                        instance.getStartTime().toInstant(),
                        instance.getEndTime().toInstant()
                    ).toMillis();
                })
                .max();
            maxDuration.ifPresent(time::setSlowestCompletionTime);
        }

        return time;
    }

    /**
     * 获取流程趋势数据
     *
     * @param days 统计天数
     * @return 趋势数据
     */
    public Map<String, Object> getProcessTrend(Integer days) {
        log.debug("获取流程趋势 - 天数: {}", days);
        Map<String, Object> trend = new HashMap<>();

        Map<String, DailyStatistics> dailyStats = getDailyStatistics(days);

        // 提取趋势数据
        List<String> dates = new ArrayList<>(dailyStats.keySet());
        List<Integer> startedCounts = dailyStats.values().stream()
            .map(DailyStatistics::getStartedCount)
            .collect(Collectors.toList());
        List<Integer> completedCounts = dailyStats.values().stream()
            .map(DailyStatistics::getCompletedCount)
            .collect(Collectors.toList());

        trend.put("dates", dates);
        trend.put("startedCounts", startedCounts);
        trend.put("completedCounts", completedCounts);

        // 计算总体趋势
        long totalStarted = startedCounts.stream().mapToLong(Integer::longValue).sum();
        long totalCompleted = completedCounts.stream().mapToLong(Integer::longValue).sum();
        trend.put("totalStarted", totalStarted);
        trend.put("totalCompleted", totalCompleted);

        return trend;
    }

    /**
     * 获取超时统计
     *
     * @param timeoutHours 超时小时数
     * @return 超时统计数据
     */
    public TimeoutStatistics getTimeoutStatistics(Long timeoutHours) {
        log.debug("获取超时统计 - 超时小时数: {}", timeoutHours);
        TimeoutStatistics stats = new TimeoutStatistics();
        stats.setTimeoutHours(timeoutHours);

        List<com.lingflow.dto.ProcessInstanceVO> runningInstances =
            extendedRuntimeService.getRunningProcessInstances();
        long timeoutMillis = timeoutHours * 60 * 60 * 1000;

        // 找出超时的流程实例
        List<String> timeoutInstanceIds = runningInstances.stream()
            .filter(instance -> {
                if (instance.getStartTime() != null) {
                    long runningDuration = System.currentTimeMillis() - instance.getStartTime().getTime();
                    return runningDuration > timeoutMillis;
                }
                return false;
            })
            .map(com.lingflow.dto.ProcessInstanceVO::getId)
            .collect(Collectors.toList());

        stats.setTimeoutInstanceCount((long) timeoutInstanceIds.size());
        stats.setTimeoutInstanceIds(timeoutInstanceIds);

        // 统计超时任务（简化实现）
        stats.setTimeoutTaskCount(0L);
        stats.setTimeoutTaskIds(new ArrayList<>());

        return stats;
    }

    /**
     * 用户任务统计数据
     */
    @Data
    public static class UserTaskStatistics {
        private String userId;
        private Long pendingTaskCount;
        private Long completedTaskCount;
        private Long overdueTaskCount;
        private Long averageTaskTime;
    }

    /**
     * 平均完成时间数据
     */
    @Data
    public static class AverageCompletionTime {
        private String processDefinitionKey;
        private Long averageCompletionTime;
        private Long fastestCompletionTime;
        private Long slowestCompletionTime;
    }

    /**
     * 超时统计数据
     */
    @Data
    public static class TimeoutStatistics {
        private Long timeoutHours;
        private Long timeoutInstanceCount;
        private List<String> timeoutInstanceIds;
        private Long timeoutTaskCount;
        private List<String> timeoutTaskIds;
    }
}
