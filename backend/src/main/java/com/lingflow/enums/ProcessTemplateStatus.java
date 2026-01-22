package com.lingflow.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 流程模板状态枚举
 *
 * <p>定义流程模板的三种状态：
 * <ul>
 *   <li>DRAFT - 设计态（草稿），未发布到Flowable</li>
 *   <li>ACTIVE - 发布激活态，已发布到Flowable且可启动流程实例</li>
 *   <li>INACTIVE - 停用态，已发布但暂停启动新流程实例</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
public enum ProcessTemplateStatus {
    /**
     * 设计态（草稿）
     * <p>模板处于设计阶段，未发布到Flowable
     * <ul>
     *   <li>可以编辑BPMN内容</li>
     *   <li>可以删除</li>
     *   <li>可以创建快照</li>
     *   <li>不能启动流程实例</li>
     * </ul>
     */
    DRAFT("DRAFT", "设计态"),

    /**
     * 发布激活态
     * <p>模板已发布到Flowable且处于激活状态
     * <ul>
     *   <li>不能编辑BPMN内容</li>
     *   <li>不能删除</li>
     *   <li>可以启动流程实例</li>
     *   <li>可以停用</li>
     *   <li>可以创建快照</li>
     * </ul>
     */
    ACTIVE("ACTIVE", "发布激活态"),

    /**
     * 停用态
     * <p>模板已发布但处于暂停状态
     * <ul>
     *   <li>不能编辑BPMN内容</li>
     *   <li>不能删除</li>
     *   <li>不能启动新流程实例</li>
     *   <li>可以重新激活</li>
     *   <li>可以创建快照</li>
     * </ul>
     */
    INACTIVE("INACTIVE", "停用态");

    /**
     * 状态代码（存储到数据库）
     */
    private final String code;

    /**
     * 状态描述（用于显示）
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code 状态代码
     * @param description 状态描述
     */
    ProcessTemplateStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取状态代码
     *
     * @return 状态代码
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据状态代码获取枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果状态代码无效
     */
    public static ProcessTemplateStatus fromCode(String code) {
        for (ProcessTemplateStatus status : ProcessTemplateStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown process template status code: " + code);
    }

    /**
     * 检查是否允许转换到目标状态
     *
     * @param targetStatus 目标状态
     * @return true如果允许转换，否则false
     */
    public boolean canTransitionTo(ProcessTemplateStatus targetStatus) {
        return switch (this) {
            case DRAFT -> targetStatus == ACTIVE; // DRAFT -> ACTIVE
            case ACTIVE -> targetStatus == INACTIVE; // ACTIVE -> INACTIVE
            case INACTIVE -> targetStatus == ACTIVE; // INACTIVE -> ACTIVE
        };
    }
}
