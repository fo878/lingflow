package com.lingflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程分类数据传输对象
 * 用于创建和更新分类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCategoryDTO {
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    private String code;

    /**
     * 父分类ID (NULL表示根节点)
     */
    private String parentId;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 图标
     */
    private String icon;

    // ============ 多租户字段 ============

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 上下文ID
     */
    private String contextId;

    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
