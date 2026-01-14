package com.lingflow.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BPMN元素扩展属性实体类
 * 用于存储BPMN元素的自定义扩展属性
 */
@Data
public class BpmnElementExtension {
    /**
     * 主键ID (UUID)
     */
    private String id;
    
    /**
     * 自动生成UUID并在构造函数中设置
     */
    public BpmnElementExtension() {
        this.id = UUID.randomUUID().toString();
    }
    
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
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
