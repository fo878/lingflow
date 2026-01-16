package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.service.ProcessVariableService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程变量控制器
 * 提供流程变量管理相关的API
 */
@RestController
@RequestMapping("/api/variables")
public class ProcessVariableController {

    @Autowired
    private ProcessVariableService variableService;

    /**
     * 获取流程实例的所有变量
     *
     * @param processInstanceId 流程实例ID
     * @return 变量Map
     */
    @GetMapping("/process/{processInstanceId}")
    public Result<Map<String, Object>> getProcessVariables(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            Map<String, Object> variables = variableService.getProcessVariables(processInstanceId);
            return Result.success(variables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程实例的单个变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName     变量名
     * @return 变量值
     */
    @GetMapping("/process/{processInstanceId}/variable/{variableName}")
    public Result<Object> getProcessVariable(
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("variableName") String variableName) {
        try {
            Object value = variableService.getProcessVariable(processInstanceId, variableName);
            return Result.success(value);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置流程实例变量
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PostMapping("/process")
    public Result<Boolean> setProcessVariables(@RequestBody SetVariablesRequest request) {
        try {
            variableService.setProcessVariables(
                request.getProcessInstanceId(),
                request.getVariables()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新流程实例变量
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PutMapping("/process")
    public Result<Boolean> updateProcessVariable(@RequestBody UpdateVariableRequest request) {
        try {
            variableService.updateProcessVariable(
                request.getProcessInstanceId(),
                request.getVariableName(),
                request.getValue()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除流程实例变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName     变量名
     * @return 是否成功
     */
    @DeleteMapping("/process/{processInstanceId}/variable/{variableName}")
    public Result<Boolean> removeProcessVariable(
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("variableName") String variableName) {
        try {
            variableService.removeProcessVariable(processInstanceId, variableName);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务的所有本地变量
     *
     * @param taskId 任务ID
     * @return 变量Map
     */
    @GetMapping("/task/{taskId}")
    public Result<Map<String, Object>> getTaskVariables(@PathVariable("taskId") String taskId) {
        try {
            Map<String, Object> variables = variableService.getTaskVariables(taskId);
            return Result.success(variables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务的单个本地变量
     *
     * @param taskId       任务ID
     * @param variableName 变量名
     * @return 变量值
     */
    @GetMapping("/task/{taskId}/variable/{variableName}")
    public Result<Object> getTaskVariable(
            @PathVariable("taskId") String taskId,
            @PathVariable("variableName") String variableName) {
        try {
            Object value = variableService.getTaskVariable(taskId, variableName);
            return Result.success(value);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置任务本地变量
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PostMapping("/task")
    public Result<Boolean> setTaskVariables(@RequestBody SetTaskVariablesRequest request) {
        try {
            variableService.setTaskVariables(
                request.getTaskId(),
                request.getVariables()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新任务本地变量
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PutMapping("/task")
    public Result<Boolean> updateTaskVariable(@RequestBody UpdateTaskVariableRequest request) {
        try {
            variableService.updateTaskVariable(
                request.getTaskId(),
                request.getVariableName(),
                request.getValue()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取历史变量
     *
     * @param processInstanceId 流程实例ID
     * @return 历史变量列表
     */
    @GetMapping("/history/{processInstanceId}")
    public Result<Map<String, Object>> getHistoricVariables(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            Map<String, Object> variables = variableService.getHistoricVariables(processInstanceId);
            return Result.success(variables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取变量的历史变更记录
     *
     * @param processInstanceId 流程实例ID
     * @param variableName     变量名
     * @return 历史记录列表
     */
    @GetMapping("/history/{processInstanceId}/variable/{variableName}")
    public Result<List<org.flowable.variable.api.history.HistoricVariableInstance>> getVariableHistory(
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("variableName") String variableName) {
        try {
            List<org.flowable.variable.api.history.HistoricVariableInstance> history =
                variableService.getVariableHistory(processInstanceId, variableName);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 复制变量
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PostMapping("/copy")
    public Result<Boolean> copyVariables(@RequestBody CopyVariablesRequest request) {
        try {
            variableService.copyVariables(
                request.getSourceProcessInstanceId(),
                request.getTargetProcessInstanceId(),
                request.getVariableNames()
            );
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== VO 类定义 ====================

    /**
     * 设置变量请求参数
     */
    @Data
    public static class SetVariablesRequest {
        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 变量Map
         */
        private Map<String, Object> variables;
    }

    /**
     * 更新变量请求参数
     */
    @Data
    public static class UpdateVariableRequest {
        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 变量名
         */
        private String variableName;

        /**
         * 变量值
         */
        private Object value;
    }

    /**
     * 设置任务变量请求参数
     */
    @Data
    public static class SetTaskVariablesRequest {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 变量Map
         */
        private Map<String, Object> variables;
    }

    /**
     * 更新任务变量请求参数
     */
    @Data
    public static class UpdateTaskVariableRequest {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 变量名
         */
        private String variableName;

        /**
         * 变量值
         */
        private Object value;
    }

    /**
     * 复制变量请求参数
     */
    @Data
    public static class CopyVariablesRequest {
        /**
         * 源流程实例ID
         */
        private String sourceProcessInstanceId;

        /**
         * 目标流程实例ID
         */
        private String targetProcessInstanceId;

        /**
         * 要复制的变量名列表（为空则复制全部）
         */
        private List<String> variableNames;
    }

    /**
     * 变量信息
     */
    @Data
    public static class VariableInfoVO {
        /**
         * 变量名
         */
        private String variableName;

        /**
         * 变量值
         */
        private Object value;

        /**
         * 变量类型
         */
        private String variableType;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 最后更新时间
         */
        private String lastUpdatedTime;

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 任务ID（如果是本地变量）
         */
        private String taskId;
    }
}
