package com.lingflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lingflow.dto.*;
import com.lingflow.entity.BpmnElementExtension;
import com.lingflow.entity.ProcessSnapshot;
import com.lingflow.repository.BpmnElementExtensionRepository;
import com.lingflow.repository.ProcessSnapshotRepository;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProcessService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessDefinitionServiceTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @Mock
    private HistoryService historyService;

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private ProcessDiagramGenerator diagramGenerator;

    @Mock
    private ExtendedRepositoryService extendedRepositoryService;

    @Mock
    private ExtendedRuntimeService extendedRuntimeService;

    @Mock
    private ExtendedTaskService extendedTaskService;

    @Mock
    private ExtendedHistoryService extendedHistoryService;

    @Mock
    private ProcessSnapshotRepository processSnapshotRepository;

    @Mock
    private BpmnElementExtensionRepository bpmnElementExtensionRepository;

    @InjectMocks
    private ProcessDefinitionService processDefinitionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(processDefinitionService, "objectMapper", objectMapper);
    }

    @Test
    void testDeployProcess_Success() {
        doNothing().when(extendedRepositoryService).deploy(anyString(), anyString());

        processDefinitionService.deployProcess("测试流程", "<xml>content</xml>");

        verify(extendedRepositoryService, times(1)).deploy("测试流程", "<xml>content</xml>");
    }

    @Test
    void testGetProcessDefinitions_Success() {
        List<ProcessDefinitionVO> definitions = new ArrayList<>();
        ProcessDefinitionVO def1 = new ProcessDefinitionVO();
        def1.setId("def1");
        def1.setName("测试流程");
        definitions.add(def1);

        when(extendedRepositoryService.getProcessDefinitions()).thenReturn(definitions);

        List<ProcessDefinitionVO> result = processDefinitionService.getProcessDefinitions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("def1", result.get(0).getId());
        verify(extendedRepositoryService, times(1)).getProcessDefinitions();
    }

    @Test
    void testDeleteProcessDefinition_Success() {
        doNothing().when(extendedRepositoryService).deleteDeployment(anyString(), anyBoolean(), anyBoolean());

        processDefinitionService.deleteProcessDefinition("deployment1");

        verify(extendedRepositoryService, times(1)).deleteDeployment("deployment1", true, false);
    }

    @Test
    void testStartProcess_Success() {
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instance.setProcessDefinitionId("def1");

        when(extendedRuntimeService.startProcessInstanceByKey(anyString(), anyString(), isNull()))
                .thenReturn(instance);

        ProcessInstanceVO result = processDefinitionService.startProcess("testProcess", "business1");

        assertNotNull(result);
        assertEquals("process1", result.getId());
        verify(extendedRuntimeService, times(1)).startProcessInstanceByKey("testProcess", "business1", null);
    }

    @Test
    void testGetRunningInstances_Success() {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        ProcessInstanceVO instance = new ProcessInstanceVO();
        instance.setId("process1");
        instances.add(instance);

        when(extendedRuntimeService.getRunningProcessInstances()).thenReturn(instances);

        List<ProcessInstanceVO> result = processDefinitionService.getRunningInstances();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(extendedRuntimeService, times(1)).getRunningProcessInstances();
    }

    @Test
    void testGetCompletedInstances_Success() {
        List<ProcessInstanceVO> instances = new ArrayList<>();
        when(extendedHistoryService.getCompletedProcessInstances()).thenReturn(instances);

        List<ProcessInstanceVO> result = processDefinitionService.getCompletedInstances();

        assertNotNull(result);
        verify(extendedHistoryService, times(1)).getCompletedProcessInstances();
    }

    @Test
    void testGetTasks_Success() {
        List<TaskVO> tasks = new ArrayList<>();
        TaskVO task = new TaskVO();
        task.setId("task1");
        task.setName("审批任务");
        tasks.add(task);

        when(extendedTaskService.getTasks()).thenReturn(tasks);

        List<TaskVO> result = processDefinitionService.getTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getId());
        verify(extendedTaskService, times(1)).getTasks();
    }

    @Test
    void testCompleteTask_Success() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        doNothing().when(extendedTaskService).completeTask(anyString(), anyMap());

        processDefinitionService.completeTask("task1", variables);

        verify(extendedTaskService, times(1)).completeTask("task1", variables);
    }

    @Test
    void testGenerateDiagram_RunningInstance() throws Exception {
        // Mock process instance
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getProcessDefinitionId()).thenReturn("def1");

        when(runtimeService.createProcessInstanceQuery()).thenReturn(mock(org.flowable.engine.runtime.ProcessInstanceQuery.class));
        when(runtimeService.createProcessInstanceQuery().processInstanceId("process1")).thenReturn(mock(org.flowable.engine.runtime.ProcessInstanceQuery.class));
        when(runtimeService.createProcessInstanceQuery().processInstanceId("process1").singleResult()).thenReturn(processInstance);

        when(extendedRuntimeService.getActiveActivityIds("process1")).thenReturn(List.of("task1"));

        // Mock BpmnModel
        BpmnModel bpmnModel = mock(BpmnModel.class);

        when(repositoryService.getBpmnModel("def1")).thenReturn(bpmnModel);

        // Mock ProcessDiagramGenerator
        when(processEngine.getProcessEngineConfiguration()).thenReturn(mock(org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl.class));
        when(processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator()).thenReturn(diagramGenerator);

        byte[] diagramBytes = new byte[]{1, 2, 3, 4, 5};
        InputStream inputStream = new ByteArrayInputStream(diagramBytes);
        when(diagramGenerator.generateDiagram(eq(bpmnModel), eq("png"), anyList(), anyList(),
                anyString(), anyString(), anyString(), any(), anyDouble(), anyBoolean())).thenReturn(inputStream);

        byte[] result = processDefinitionService.generateDiagram("process1");

        assertNotNull(result);
        assertEquals(5, result.length);
    }

    @Test
    void testGenerateDiagram_ProcessNotFound() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mock(org.flowable.engine.runtime.ProcessInstanceQuery.class));
        when(runtimeService.createProcessInstanceQuery().processInstanceId("invalid-process")).thenReturn(mock(org.flowable.engine.runtime.ProcessInstanceQuery.class));
        when(runtimeService.createProcessInstanceQuery().processInstanceId("invalid-process").singleResult()).thenReturn(null);

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(mock(org.flowable.engine.history.HistoricProcessInstanceQuery.class));
        when(historyService.createHistoricProcessInstanceQuery().processInstanceId("invalid-process")).thenReturn(mock(org.flowable.engine.history.HistoricProcessInstanceQuery.class));
        when(historyService.createHistoricProcessInstanceQuery().processInstanceId("invalid-process").singleResult()).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processDefinitionService.generateDiagram("invalid-process");
        });

        assertEquals("流程实例不存在", exception.getMessage());
    }

    @Test
    void testSaveElementExtension_CreateNew() {
        ObjectNode extensionAttrs = objectMapper.createObjectNode();
        extensionAttrs.put("key", "value");

        when(bpmnElementExtensionRepository.findByProcessAndElement("def1", "element1")).thenReturn(null);

        processDefinitionService.saveElementExtension("def1", "element1", "userTask", extensionAttrs);

        verify(bpmnElementExtensionRepository, times(1)).save(any());
        verify(bpmnElementExtensionRepository, times(1)).saveHistory(any());
    }

    @Test
    void testSaveElementExtension_UpdateExisting() {
        ObjectNode extensionAttrs = objectMapper.createObjectNode();
        extensionAttrs.put("key", "newValue");

        BpmnElementExtension existing = new BpmnElementExtension();
        existing.setId("ext1");
        existing.setVersion(1);

        when(bpmnElementExtensionRepository.findByProcessAndElement("def1", "element1")).thenReturn(existing);

        processDefinitionService.saveElementExtension("def1", "element1", "userTask", extensionAttrs);

        verify(bpmnElementExtensionRepository, times(1)).update(any());
        verify(bpmnElementExtensionRepository, times(1)).saveHistory(any());
    }

    @Test
    void testGetElementExtension_Exists() {
        BpmnElementExtension extension = new BpmnElementExtension();
        extension.setElementId("element1");
        extension.setElementType("userTask");
        extension.setVersion(1);

        when(bpmnElementExtensionRepository.findByProcessAndElement("def1", "element1")).thenReturn(extension);

        ElementExtensionQueryResult result = processDefinitionService.getElementExtension("def1", "element1");

        assertNotNull(result);
        assertTrue(result.isExists());
        assertEquals("element1", result.getElementId());
        assertEquals("userTask", result.getElementType());
    }

    @Test
    void testGetElementExtension_NotExists() {
        when(bpmnElementExtensionRepository.findByProcessAndElement("def1", "element1")).thenReturn(null);

        ElementExtensionQueryResult result = processDefinitionService.getElementExtension("def1", "element1");

        assertNotNull(result);
        assertFalse(result.isExists());
        assertEquals("element1", result.getElementId());
    }

    @Test
    void testDeleteElementExtension_Success() {
        BpmnElementExtension existing = new BpmnElementExtension();
        existing.setId("ext1");
        existing.setElementType("userTask");

        when(bpmnElementExtensionRepository.findByProcessAndElement("def1", "element1")).thenReturn(existing);

        processDefinitionService.deleteElementExtension("def1", "element1");

        verify(bpmnElementExtensionRepository, times(1)).saveHistory(any());
        verify(bpmnElementExtensionRepository, times(1)).deleteByProcessAndElement("def1", "element1");
    }

    @Test
    void testCreateProcessSnapshot_Success() {
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getDeploymentId()).thenReturn("deployment1");
        when(definition.getResourceName()).thenReturn("test.bpmn20.xml");

        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess")).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess").latestVersion()).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess").latestVersion().singleResult()).thenReturn(definition);

        byte[] xmlBytes = "<xml>content</xml>".getBytes();
        InputStream inputStream = new ByteArrayInputStream(xmlBytes);
        when(repositoryService.getResourceAsStream("deployment1", "test.bpmn20.xml")).thenReturn(inputStream);

        when(processSnapshotRepository.findByProcessDefinitionKey("testProcess")).thenReturn(new ArrayList<>());

        processDefinitionService.createProcessSnapshot("testProcess", "v1.0", "初始版本", "admin");

        verify(processSnapshotRepository, times(1)).save(any(ProcessSnapshot.class));
    }

    @Test
    void testCreateProcessSnapshot_DuplicateName() {
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getDeploymentId()).thenReturn("deployment1");
        when(definition.getResourceName()).thenReturn("test.bpmn20.xml");

        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess")).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess").latestVersion()).thenReturn(mock(org.flowable.engine.repository.ProcessDefinitionQuery.class));
        when(repositoryService.createProcessDefinitionQuery().processDefinitionKey("testProcess").latestVersion().singleResult()).thenReturn(definition);

        byte[] xmlBytes = "<xml>content</xml>".getBytes();
        InputStream inputStream = new ByteArrayInputStream(xmlBytes);
        when(repositoryService.getResourceAsStream("deployment1", "test.bpmn20.xml")).thenReturn(inputStream);

        ProcessSnapshot existingSnapshot = new ProcessSnapshot();
        existingSnapshot.setSnapshotName("v1.0");

        when(processSnapshotRepository.findByProcessDefinitionKey("testProcess")).thenReturn(List.of(existingSnapshot));

        assertThrows(RuntimeException.class, () -> {
            processDefinitionService.createProcessSnapshot("testProcess", "v1.0", "初始版本", "admin");
        });
    }

    @Test
    void testRollbackToSnapshot_Success() {
        ProcessSnapshot snapshot = new ProcessSnapshot();
        snapshot.setId("snapshot1");
        snapshot.setSnapshotName("v1.0");
        snapshot.setBpmnXml("<xml>content</xml>");

        when(processSnapshotRepository.findById("snapshot1")).thenReturn(snapshot);

        doNothing().when(extendedRepositoryService).deploy(anyString(), anyString());

        processDefinitionService.rollbackToSnapshot("snapshot1");

        verify(extendedRepositoryService, times(1)).deploy(contains("v1.0_rollback_"), eq("<xml>content</xml>"));
    }

    @Test
    void testDeleteSnapshot_Success() {
        doNothing().when(processSnapshotRepository).deleteById(anyString());

        processDefinitionService.deleteSnapshot("snapshot1");

        verify(processSnapshotRepository, times(1)).deleteById("snapshot1");
    }
}
