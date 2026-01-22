package com.lingflow.dto;

import com.lingflow.enums.ProcessTemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设计态流程模板视图对象
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplateDraftVO {

    /**
     * 模板ID
     */
    private String id;

    /**
     * 流程模板Key
     */
    private String templateKey;

    /**
     * 流程模板名称
     */
    private String templateName;

    /**
     * 流程模板描述
     */
    private String description;

    /**
     * BPMN XML内容
     */
    private String bpmnXml;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 表单配置
     */
    private Object formConfig;

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
    private String tenantId;

    /**
     * 状态
     */
    private ProcessTemplateStatus status;

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
     * 版本号
     */
    private Integer version;
}
