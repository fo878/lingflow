package com.lingflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建设计态模板请求
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDraftTemplateRequest {

    /**
     * 流程模板Key
     */
    @NotBlank(message = "模板Key不能为空")
    private String templateKey;

    /**
     * 流程模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 流程模板描述
     */
    private String description;

    /**
     * BPMN XML内容
     */
    @NotBlank(message = "BPMN XML内容不能为空")
    private String bpmnXml;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private String categoryId;

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
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
}
