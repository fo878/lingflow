package com.lingflow.controller;

import com.lingflow.dto.ProcessDefinitionVO;
import com.lingflow.dto.ProcessInstanceVO;
import com.lingflow.dto.Result;
import com.lingflow.dto.TaskVO;
import com.lingflow.service.ProcessService;
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
@CrossOrigin(origins = "*")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    /**
     * 部署流程
     */
    @PostMapping("/deploy")
    public Result<Void> deployProcess(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String xml = request.get("xml");
            processService.deployProcess(name, xml);
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
    public Result<Void> deleteProcessDefinition(@PathVariable String deploymentId) {
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
            @PathVariable String processKey,
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
    public ResponseEntity<byte[]> getProcessDiagram(@PathVariable String processInstanceId) {
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
    public ResponseEntity<byte[]> getProcessDefinitionDiagram(@PathVariable String processDefinitionId) {
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
    public Result<Map<String, Object>> getProcessDefinitionXml(@PathVariable String processDefinitionId) {
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
    public Result<Map<String, Object>> getProcessBpmn(@PathVariable String processInstanceId) {
        try {
            Map<String, Object> result = processService.getProcessBpmnWithNodeInfo(processInstanceId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
