package com.lingflow.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * BPMN元素扩展属性数据传输对象
 */
@Data
public class BpmnElementExtensionDTO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    
    /**
     * BPMN元素ID
     */
    private String elementId;
    
    /**
     * BPMN元素类型
     */
    private String elementType;
    
    /**
     * 扩展属性JSON
     */
    private JsonNode extensionAttributes;
    
    /**
     * 版本号
     */
    private Integer version;
}
