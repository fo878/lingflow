package com.lingflow.controller;

import com.lingflow.dto.ProcessDefinitionVO;
import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.dto.Result;
import com.lingflow.dto.BpmnElementExtensionDTO;
import com.lingflow.dto.ElementExtensionQueryResult;
import com.lingflow.dto.DeployProcessRequest;
import com.lingflow.entity.BpmnElementExtension;
import com.lingflow.service.ProcessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程控制器
 */
@RestController
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 部署流程
     */
    @PostMapping("/deploy")
    public Result<Void> deployProcess(@Valid @RequestBody DeployProcessRequest request) {
        try {
            processService.deployProcess(request.getName(), request.getXml());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有流程定义
     */
    @GetMapping("/definitions")
    public Result<List<ProcessDefinitionVO>> getProcessDefinitions() {
        try {
            List<ProcessDefinitionVO> definitions = processService.getProcessDefinitions();
            return Result.success(definitions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/definition/{deploymentId}")
    public Result<Void> deleteProcessDefinition(@PathVariable("deploymentId") String deploymentId) {
        try {
            processService.deleteProcessDefinition(deploymentId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启动流程实例
     */
    @PostMapping("/start/{processKey}")
    public Result<ProcessInstanceVO> startProcess(
            @PathVariable("processKey") String processKey,
            @RequestBody(required = false) Map<String, String> variables) {
        try {
            String businessKey = variables != null ? variables.get("businessKey") : null;
            ProcessInstanceVO instance = processService.startProcess(processKey, businessKey);
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取运行中的流程实例
     */
    @GetMapping("/running")
    public Result<List<ProcessInstanceVO>> getRunningInstances() {
        try {
            List<ProcessInstanceVO> instances = processService.getRunningInstances();
            return Result.success(instances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取已完结的流程实例
     */
    @GetMapping("/completed")
    public Result<List<ProcessInstanceVO>> getCompletedInstances() {
        try {
            List<ProcessInstanceVO> instances = processService.getCompletedInstances();
            return Result.success(instances);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程图
     */
    @GetMapping("/diagram/{processInstanceId}")
    public ResponseEntity<byte[]> getProcessDiagram(@PathVariable("processInstanceId") String processInstanceId) {
        try {
            byte[] diagram = processService.generateDiagram(processInstanceId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(diagram, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取流程定义图
     */
    @GetMapping("/definition/diagram/{processDefinitionId}")
    public ResponseEntity<byte[]> getProcessDefinitionDiagram(@PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            byte[] diagram = processService.generateProcessDefinitionDiagram(processDefinitionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(diagram, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取流程定义的BPMN XML
     */
    @GetMapping("/definition/xml/{processDefinitionId}")
    public Result<Map<String, Object>> getProcessDefinitionXml(@PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            Map<String, Object> result = processService.getProcessDefinitionXml(processDefinitionId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程实例的BPMN XML和节点信息
     */
    @GetMapping("/bpmn/{processInstanceId}")
    public Result<Map<String, Object>> getProcessBpmn(@PathVariable("processInstanceId") String processInstanceId) {
        try {
            Map<String, Object> result = processService.getProcessBpmnWithNodeInfo(processInstanceId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== BPMN元素扩展属性管理 ====================

    /**
     * 保存BPMN元素扩展属性
     */
    @PostMapping("/extension")
    @SuppressWarnings("unchecked")
    public Result<Void> saveElementExtension(@RequestBody Map<String, Object> request) {
        try {
            String processDefinitionId = (String) request.get("processDefinitionId");
            String elementId = (String) request.get("elementId");
            String elementType = (String) request.get("elementType");
            com.fasterxml.jackson.databind.JsonNode extensionAttributes = objectMapper.valueToTree(request.get("extensionAttributes"));
            
            processService.saveElementExtension(processDefinitionId, elementId, elementType, extensionAttributes);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取BPMN元素扩展属性
     */
    @GetMapping("/extension/{processDefinitionId}/{elementId}")
    public Result<ElementExtensionQueryResult> getElementExtension(
            @PathVariable("processDefinitionId") String processDefinitionId,
            @PathVariable("elementId") String elementId) {
        try {
            ElementExtensionQueryResult result = processService.getElementExtension(processDefinitionId, elementId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量保存BPMN元素扩展属性
     */
    @PostMapping("/extensions/batch")
    @SuppressWarnings("unchecked")
    public Result<Void> batchSaveElementExtensions(@RequestBody Map<String, Object> request) {
        try {
            String processDefinitionId = (String) request.get("processDefinitionId");
            java.util.List<Map<String, Object>> extensions = (java.util.List<Map<String, Object>>) request.get("extensions");
            
            java.util.List<BpmnElementExtensionDTO> extensionDTOs = new java.util.ArrayList<>();
            for (Map<String, Object> ext : extensions) {
                BpmnElementExtensionDTO dto = new BpmnElementExtensionDTO();
                dto.setElementId((String) ext.get("elementId"));
                dto.setElementType((String) ext.get("elementType"));
                dto.setExtensionAttributes(objectMapper.valueToTree(ext.get("extensionAttributes")));
                extensionDTOs.add(dto);
            }
            
            processService.batchSaveElementExtensions(processDefinitionId, extensionDTOs);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程定义的所有扩展属性
     */
    @GetMapping("/extensions/{processDefinitionId}")
    public Result<java.util.List<BpmnElementExtension>> getAllElementExtensions(@PathVariable("processDefinitionId") String processDefinitionId) {
        try {
            java.util.List<BpmnElementExtension> extensions = processService.getAllElementExtensions(processDefinitionId);
            return Result.success(extensions);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除BPMN元素扩展属性
     */
    @DeleteMapping("/extension/{processDefinitionId}/{elementId}")
    public Result<Void> deleteElementExtension(
            @PathVariable("processDefinitionId") String processDefinitionId,
            @PathVariable("elementId") String elementId) {
        try {
            processService.deleteElementExtension(processDefinitionId, elementId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
