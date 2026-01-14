package com.lingflow.dto;

/**
 * 创建快照请求 DTO
 */
public class CreateSnapshotRequest {
    
    /**
     * 流程定义KEY
     */
    private String processDefinitionKey;
    
    /**
     * 快照名称
     */
    private String snapshotName;
    
    /**
     * 快照描述（可选）
     */
    private String description;
    
    /**
     * 创建人（可选）
     */
    private String creator;

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
