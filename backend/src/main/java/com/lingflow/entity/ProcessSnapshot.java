package com.lingflow.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 流程快照实体类
 * 用于存储流程定义的历史版本快照
 */
@Data
public class ProcessSnapshot {
    /**
     * 主键ID (UUID)
     */
    private String id;
    
    /**
     * 自动生成UUID并在构造函数中设置
     */
    public ProcessSnapshot() {
        this.id = UUID.randomUUID().toString();
    }
    
    /**
     * 流程定义KEY
     */
    private String processDefinitionKey;
    
    /**
     * 快照名称
     */
    private String snapshotName;
    
    /**
     * 快照版本号
     */
    private Integer snapshotVersion;
    
    /**
     * BPMN XML内容
     */
    private String bpmnXml;
    
    /**
     * 快照描述
     */
    private String description;
    
    /**
     * 创建人
     */
    private String creator;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
