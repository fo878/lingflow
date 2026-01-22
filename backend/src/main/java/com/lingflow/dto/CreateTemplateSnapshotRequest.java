package com.lingflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建模板快照请求
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateSnapshotRequest {

    /**
     * 来源模板ID（设计态或发布态）
     */
    @NotBlank(message = "来源模板ID不能为空")
    private String sourceTemplateId;

    /**
     * 来源模板类型（DRAFT/PUBLISHED）
     */
    @NotBlank(message = "来源模板类型不能为空")
    private String sourceTemplateType;

    /**
     * 快照名称
     */
    @NotBlank(message = "快照名称不能为空")
    private String snapshotName;
}
