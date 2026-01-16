package com.lingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程定义视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionVO {
    private String key;
    private String name;
    private Integer version;
    private String deploymentId;
    private String id;
    private String resource;
    private String description;
    private Boolean suspended;
}
