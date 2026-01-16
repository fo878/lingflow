package com.lingflow.service;

import com.lingflow.dto.ProcessDefinitionVO;
import com.lingflow.extension.wrapper.FlowableServiceTemplate;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExtendedRepositoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExtendedRepositoryServiceTest {

    @Mock
    private org.flowable.engine.RepositoryService flowableRepositoryService;

    @Mock
    private FlowableServiceTemplate serviceTemplate;

    @Mock
    private ProcessDefinitionQuery processDefinitionQuery;

    @InjectMocks
    private ExtendedRepositoryService extendedRepositoryService;

    @Test
    void testDeploy_Success() {
        when(serviceTemplate.execute(eq("RepositoryService.deploy"), any()))
                .thenReturn("deployment1");

        String result = extendedRepositoryService.deploy("测试流程", "<xml>content</xml>");

        assertNotNull(result);
        assertEquals("deployment1", result);
        verify(serviceTemplate, times(1)).execute(eq("RepositoryService.deploy"), any());
    }

    @Test
    void testGetProcessDefinitions_Success() {
        List<ProcessDefinition> definitions = new ArrayList<>();
        ProcessDefinition def1 = mock(ProcessDefinition.class);
        when(def1.getId()).thenReturn("def1");
        when(def1.getKey()).thenReturn("testProcess");
        when(def1.getName()).thenReturn("测试流程");
        when(def1.getVersion()).thenReturn(1);
        definitions.add(def1);

        when(serviceTemplate.execute(eq("RepositoryService.createProcessDefinitionQuery"), any()))
                .thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.orderByProcessDefinitionVersion()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.desc()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.list()).thenReturn(definitions);

        List<ProcessDefinitionVO> result = extendedRepositoryService.getProcessDefinitions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("def1", result.get(0).getId());
        verify(serviceTemplate, times(1)).execute(eq("RepositoryService.createProcessDefinitionQuery"), any());
    }

    @Test
    void testGetProcessDefinitionByKey_Success() {
        ProcessDefinition def1 = mock(ProcessDefinition.class);
        when(def1.getId()).thenReturn("def1");
        when(def1.getKey()).thenReturn("testProcess");

        when(serviceTemplate.execute(eq("RepositoryService.createProcessDefinitionQuery"), any()))
                .thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionKey("testProcess")).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.singleResult()).thenReturn(def1);

        ProcessDefinitionVO result = extendedRepositoryService.getProcessDefinitionByKey("testProcess");

        assertNotNull(result);
        assertEquals("def1", result.getId());
    }

    @Test
    void testGetProcessDefinitionByKey_NotFound() {
        when(serviceTemplate.execute(eq("RepositoryService.createProcessDefinitionQuery"), any()))
                .thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionKey("nonexistent")).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.singleResult()).thenReturn(null);

        ProcessDefinitionVO result = extendedRepositoryService.getProcessDefinitionByKey("nonexistent");

        assertNull(result);
    }

    @Test
    void testGetProcessDefinitionXml_Success() {
        when(serviceTemplate.execute(eq("RepositoryService.getProcessDefinitionXml"), any(), eq("def1")))
                .thenReturn("<xml>content</xml>");

        String result = extendedRepositoryService.getProcessDefinitionXml("def1");

        assertNotNull(result);
        assertEquals("<xml>content</xml>", result);
        verify(serviceTemplate, times(1)).execute(eq("RepositoryService.getProcessDefinitionXml"), any(), eq("def1"));
    }

    @Test
    void testCreateProcessDefinitionQuery_Success() {
        when(serviceTemplate.execute(eq("RepositoryService.createProcessDefinitionQuery"), any()))
                .thenReturn(processDefinitionQuery);

        ProcessDefinitionQuery result = extendedRepositoryService.createProcessDefinitionQuery();

        assertNotNull(result);
        verify(serviceTemplate, times(1)).execute(eq("RepositoryService.createProcessDefinitionQuery"), any());
    }
}
