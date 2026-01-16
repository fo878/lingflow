package com.lingflow.dto;

import lombok.Data;

/**
 * 流程定义数据传输对象
 */
@Data
public class ProcessDefinitionDTO {
    /**
     * 流程定义ID（更新时需要）
     */
    private String id;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程Key
     */
    private String key;

    /**
     * 流程描述
     */
    private String description;

    /**
     * BPMN XML内容
     */
    private String xml;

    /**
     * 所属分类ID
     */
    private String categoryId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 上下文ID
     */
    private String contextId;
}
