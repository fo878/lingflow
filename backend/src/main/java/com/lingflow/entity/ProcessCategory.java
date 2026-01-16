package com.lingflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 流程分类实体类
 * 支持多租户、树形结构、路径记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCategory {
    /**
     * 主键ID (UUID)
     */
    private String id;

    /**
     * 自动生成UUID并在构造函数中设置
     */
    public ProcessCategory(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
    }

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码（用于API调用、系统引用等）
     * 同一租户、应用、上下文下唯一
     */
    private String code;

    /**
     * 父分类ID (NULL表示根节点)
     */
    private String parentId;

    /**
     * 分类路径（格式：/id1/id2/id3）
     * 用于快速查询某个节点下的所有后代
     */
    private String path;

    /**
     * 层级深度（根节点为0）
     */
    private Integer level;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号（同级分类的排序）
     */
    private Integer sortOrder;

    /**
     * 图标（Element Plus 图标名或 URL）
     */
    private String icon;

    // ============ 多租户字段 ============

    /**
     * 应用ID（可选，用于更细粒度的隔离）
     */
    private String appId;

    /**
     * 上下文ID（可选，用于特定业务场景）
     */
    private String contextId;

    /**
     * 租户ID（必需，用于多租户隔离）
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

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 软删除标记
     */
    private Boolean isDeleted;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    // ============ 非持久化字段 ============

    /**
     * 子分类列表（用于树形结构）
     * 不持久化到数据库，仅用于查询结果
     */
    private List<ProcessCategory> children;

    /**
     * 该分类下的流程数量
     * 不持久化到数据库，仅用于查询结果
     */
    private Integer processCount;

    /**
     * 是否有子分类
     * 不持久化到数据库，仅用于查询结果
     */
    private Boolean hasChildren;
}
