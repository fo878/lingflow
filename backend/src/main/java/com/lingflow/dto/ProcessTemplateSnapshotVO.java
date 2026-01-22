package com.lingflow.dto;

import com.lingflow.enums.ProcessTemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程模板快照视图对象
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplateSnapshotVO {

    /**
     * 快照ID
     */
    private String id;

    /**
     * 流程模板Key
     */
    private String templateKey;

    /**
     * 快照名称
     */
    private String snapshotName;

    /**
     * BPMN XML内容
     */
    private String bpmnXml;

    /**
     * 来源模板ID
     */
    private String sourceTemplateId;

    /**
     * 来源模板状态
     */
    private ProcessTemplateStatus sourceTemplateStatus;

    /**
     * 来源模板版本号
     */
    private Integer sourceTemplateVersion;

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
     * 快照版本号
     */
    private Integer snapshotVersion;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 创建人
     */
    private String createdBy;
}
