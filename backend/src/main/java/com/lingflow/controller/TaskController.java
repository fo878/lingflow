package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.dto.TaskVO;
import com.lingflow.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private ProcessService processService;

    /**
     * 获取待办任务列表
     */
    @GetMapping("/list")
    public Result<List<TaskVO>> getTasks() {
        try {
            List<TaskVO> tasks = processService.getTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成任务
     */
    @PostMapping("/complete/{taskId}")
    public Result<Void> completeTask(
            @PathVariable("taskId") String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            processService.completeTask(taskId, variables);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务表单（简化版本，暂时返回空）
     */
    @GetMapping("/form/{taskId}")
    public Result<Map<String, Object>> getTaskForm(@PathVariable("taskId") String taskId) {
        try {
            // 简化版本，返回空表单
            return Result.success(Map.of("taskId", taskId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
