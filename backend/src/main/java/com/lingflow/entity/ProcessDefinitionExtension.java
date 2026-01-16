package com.lingflow.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 流程定义扩展实体类
 * 用于存储流程定义的分类和标签等扩展信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionExtension {
    /**
     * 主键ID (UUID)
     */
    private String id;

    /**
     * 自动生成UUID并在构造函数中设置
     */
    public ProcessDefinitionExtension(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
    }

    /**
     * 关联Flowable的流程定义ID
     * 唯一标识一个流程定义
     */
    private String processDefinitionId;

    /**
     * 分类ID（外键关联到 process_category 表）
     */
    private String categoryId;

    /**
     * 标签（JSON数组格式：["tag1","tag2"]）
     */
    private JsonNode tags;

    // ============ 多租户字段（冗余，方便查询） ============

    /**
     * 应用ID（冗余，方便查询和报表）
     */
    private String appId;

    /**
     * 上下文ID（冗余，方便查询和报表）
     */
    private String contextId;

    /**
     * 租户ID（冗余，方便查询和报表）
     */
    private String tenantId;

    // ============ 审计字段 ============

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    // ============ 非持久化字段 ============

    /**
     * 关联的分类对象
     * 不持久化到数据库，仅用于查询结果
     */
    private ProcessCategory category;

    /**
     * 分类路径信息（格式：/分类1/分类2/分类3）
     * 不持久化到数据库，仅用于查询结果
     */
    private String categoryPath;
}
