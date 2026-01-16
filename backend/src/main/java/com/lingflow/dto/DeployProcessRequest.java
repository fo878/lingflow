package com.lingflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 发布流程请求
 */
@Data
public class DeployProcessRequest {

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程 XML（BPMN 2.0 格式）
     */
    @NotBlank(message = "流程XML不能为空")
    private String xml;
}
