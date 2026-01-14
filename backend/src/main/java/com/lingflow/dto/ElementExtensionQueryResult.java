package com.lingflow.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * BPMN元素扩展属性查询结果对象
 */
@Data
public class ElementExtensionQueryResult {
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
     * 是否存在扩展属性
     */
    private boolean exists;
}
