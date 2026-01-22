package com.lingflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从快照恢复请求
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreSnapshotRequest {

    /**
     * 快照ID
     */
    @NotBlank(message = "快照ID不能为空")
    private String snapshotId;

    /**
     * 新的设计态模板名称（可选，默认使用快照名称）
     */
    private String newTemplateName;
}
