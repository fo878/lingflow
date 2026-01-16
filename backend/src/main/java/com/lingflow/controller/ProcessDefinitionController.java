package com.lingflow.controller;

import com.lingflow.dto.ProcessDefinitionDTO;
import com.lingflow.service.ProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程定义管理控制器
 * 处理流程定义的保存、更新、激活、停用等操作
 */
@Slf4j
@RestController
@RequestMapping("/api/process/definition")
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 保存流程定义（草稿）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveProcessDefinition(@RequestBody ProcessDefinitionDTO dto) {
        log.info("保存流程定义: name={}, key={}, categoryId={}",
                dto.getName(), dto.getKey(), dto.getCategoryId());

        try {
            Map<String, Object> result = processDefinitionService.saveProcessDefinition(dto);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "success",
                    "data", result
            ));
        } catch (Exception e) {
            log.error("保存流程定义失败", e);
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "保存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新流程定义属性
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProcessDefinition(@RequestBody ProcessDefinitionDTO dto) {
        log.info("更新流程定义: id={}, name={}, key={}",
                dto.getId(), dto.getName(), dto.getKey());

        try {
            processDefinitionService.updateProcessDefinition(dto);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "更新成功"
            ));
        } catch (Exception e) {
            log.error("更新流程定义失败", e);
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "更新失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 激活流程定义
     */
    @PostMapping("/{processDefinitionId}/activate")
    public ResponseEntity<Map<String, Object>> activateProcessDefinition(
            @PathVariable String processDefinitionId) {
        log.info("激活流程定义: id={}", processDefinitionId);

        try {
            processDefinitionService.activateProcessDefinition(processDefinitionId);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "激活成功"
            ));
        } catch (Exception e) {
            log.error("激活流程定义失败", e);
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "激活失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 停用流程定义
     */
    @PostMapping("/{processDefinitionId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendProcessDefinition(
            @PathVariable String processDefinitionId) {
        log.info("停用流程定义: id={}", processDefinitionId);

        try {
            processDefinitionService.suspendProcessDefinition(processDefinitionId);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "停用成功"
            ));
        } catch (Exception e) {
            log.error("停用流程定义失败", e);
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "停用失败: " + e.getMessage()
            ));
        }
    }
}
