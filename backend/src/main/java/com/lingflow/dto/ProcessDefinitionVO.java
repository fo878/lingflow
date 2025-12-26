package com.lingflow.dto;

import lombok.Data;

/**
 * 流程定义视图对象
 */
@Data
public class ProcessDefinitionVO {
    private String key;
    private String name;
    private Integer version;
    private String deploymentId;
    private String id;
    private String resource;
}
