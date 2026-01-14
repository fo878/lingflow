package com.lingflow.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BPMN元素扩展属性历史实体类
 * 用于记录BPMN元素扩展属性的变更历史
 */
@Data
public class BpmnElementExtensionHistory {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 扩展属性ID
     */
    private Long extensionId;
    
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
    
    /**
     * 操作类型（CREATE/UPDATE/DELETE）
     */
    private String operationType;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
}
