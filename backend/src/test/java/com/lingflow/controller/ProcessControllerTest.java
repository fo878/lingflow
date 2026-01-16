package com.lingflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.dto.*;
import com.lingflow.entity.BpmnElementExtension;
import com.lingflow.service.ProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProcessController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessControllerTest {

    @Mock
    private ProcessService processService;

    @InjectMocks
    private ProcessController processController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(processController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDeployProcess_Success() throws Exception {
        DeployProcessRequest request = new DeployProcessRequest();
        request.setName("测试流程");
        request.setXml("<xml>content</xml>");

        doNothing().when(processService).deployProcess(anyString(), anyString());

        mockMvc.perform(post("/process/deploy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).deployProcess(eq("测试流程"), eq("<xml>content</xml>"));
    }

    @Test
    void testDeployProcess_Exception() throws Exception {
        DeployProcessRequest request = new DeployProcessRequest();
        request.setName("测试流程");
        request.setXml("<invalid>xml</xml>");

        doThrow(new RuntimeException("XML格式错误"))
                .when(processService).deployProcess(anyString(), anyString());

        mockMvc.perform(post("/process/deploy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("XML格式错误"));
    }

    @Test
    void testGetProcessDefinitions_Success() throws Exception {
        List<ProcessDefinitionVO> definitions = new ArrayList<>();
        ProcessDefinitionVO def1 = new ProcessDefinitionVO();
        def1.setId("def1");
        def1.setName("测试流程");
        def1.setKey("testProcess");
        definitions.add(def1);

        when(processService.getProcessDefinitions()).thenReturn(definitions);

        mockMvc.perform(get("/process/definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("def1"))
                .andExpect(jsonPath("$.data[0].name").value("测试流程"));

        verify(processService, times(1)).getProcessDefinitions();
    }

    @Test
    void testDeleteProcessDefinition_Success() throws Exception {
        doNothing().when(processService).deleteProcessDefinition(anyString());

        mockMvc.perform(delete("/process/definition/deployment1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).deleteProcessDefinition("deployment1");
    }

    @Test
    void testStartProcess_Success() throws Exception {
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instance.setProcessDefinitionId("def1");
        instance.setBusinessKey("business1");

        when(processService.startProcess(anyString(), anyString())).thenReturn(instance);

        Map<String, String> variables = new HashMap<>();
        variables.put("businessKey", "business1");

        mockMvc.perform(post("/process/start/testProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variables)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("process1"));

        verify(processService, times(1)).startProcess(eq("testProcess"), eq("business1"));
    }

    @Test
    void testGetRunningInstances_Success() throws Exception {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instances.add(instance);

        when(processService.getRunningInstances()).thenReturn(instances);

        mockMvc.perform(get("/process/running"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("process1"));

        verify(processService, times(1)).getRunningInstances();
    }

    @Test
    void testGetCompletedInstances_Success() throws Exception {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        when(processService.getCompletedInstances()).thenReturn(instances);

        mockMvc.perform(get("/process/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(processService, times(1)).getCompletedInstances();
    }

    @Test
    void testGetProcessDiagram_Success() throws Exception {
        byte[] diagramBytes = new byte[]{1, 2, 3, 4, 5};
        when(processService.generateDiagram(anyString())).thenReturn(diagramBytes);

        mockMvc.perform(get("/process/diagram/process1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(diagramBytes));

        verify(processService, times(1)).generateDiagram("process1");
    }

    @Test
    void testGetProcessDiagram_Exception() throws Exception {
        when(processService.generateDiagram(anyString()))
                .thenThrow(new RuntimeException("流程实例不存在"));

        mockMvc.perform(get("/process/diagram/invalid-process"))
                .andExpect(status().isInternalServerError());

        verify(processService, times(1)).generateDiagram("invalid-process");
    }

    @Test
    void testSaveElementExtension_Success() throws Exception {
        doNothing().when(processService).saveElementExtension(
                anyString(), anyString(), anyString(), any(com.fasterxml.jackson.databind.JsonNode.class));

        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionId", "def1");
        request.put("elementId", "element1");
        request.put("elementType", "userTask");
        request.put("extensionAttributes", Map.of("key", "value"));

        mockMvc.perform(post("/process/extension")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).saveElementExtension(
                eq("def1"), eq("element1"), eq("userTask"), any(com.fasterxml.jackson.databind.JsonNode.class));
    }

    @Test
    void testGetElementExtension_Success() throws Exception {
        ElementExtensionQueryResult result = new ElementExtensionQueryResult();
        result.setElementId("element1");
        result.setExists(true);

        when(processService.getElementExtension(anyString(), anyString())).thenReturn(result);

        mockMvc.perform(get("/process/extension/def1/element1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.elementId").value("element1"))
                .andExpect(jsonPath("$.data.exists").value(true));

        verify(processService, times(1)).getElementExtension("def1", "element1");
    }

    @Test
    void testDeleteElementExtension_Success() throws Exception {
        doNothing().when(processService).deleteElementExtension(anyString(), anyString());

        mockMvc.perform(delete("/process/extension/def1/element1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).deleteElementExtension("def1", "element1");
    }

    @Test
    void testGetAllElementExtensions_Success() throws Exception {
        List<BpmnElementExtension> extensions = new ArrayList<>();
        when(processService.getAllElementExtensions(anyString())).thenReturn(extensions);

        mockMvc.perform(get("/process/extensions/def1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(processService, times(1)).getAllElementExtensions("def1");
    }

    @Test
    void testBatchSaveElementExtensions_Success() throws Exception {
        doNothing().when(processService).batchSaveElementExtensions(
                anyString(), anyList());

        Map<String, Object> request = new HashMap<>();
        request.put("processDefinitionId", "def1");

        List<Map<String, Object>> extensions = new ArrayList<>();
        Map<String, Object> ext1 = new HashMap<>();
        ext1.put("elementId", "element1");
        ext1.put("elementType", "userTask");
        ext1.put("extensionAttributes", Map.of("key", "value"));
        extensions.add(ext1);

        request.put("extensions", extensions);

        mockMvc.perform(post("/process/extensions/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(processService, times(1)).batchSaveElementExtensions(eq("def1"), anyList());
    }
}
