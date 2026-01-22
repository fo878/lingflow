package com.lingflow.entity;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.lingflow.enums.ProcessTemplateStatus;
import com.lingflow.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 设计态流程模板实体类
 *
 * <p>用于存储流程设计阶段的草稿，未发布到Flowable
 *
 * <p>主要特点：
 * <ul>
 *   <li>未调用Flowable deploy API</li>
 *   <li>可以编辑和删除</li>
 *   <li>可以创建快照</li>
 *   <li>不能启动流程实例</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplateDraft {

    /**
     * 主键ID (UUID)
     */
    private String id;

    /**
     * 流程模板Key（同一租户下唯一）
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
     * 所属分类ID（外键）
     */
    private String categoryId;

    /**
     * 标签（JSONB格式）
     * <p>存储格式：["tag1", "tag2"]
     */
    private String tags;

    /**
     * 表单配置（JSONB格式）
     * <p>存储格式：JSON对象，包含表单字段、验证规则等
     */
    private String formConfig;

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
     * 状态（固定为DRAFT）
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
     * 乐观锁版本号
     */
    private Integer version;

    // ============ 非持久化字段 ============

    /**
     * 分类名称（不持久化，用于查询结果）
     */
    private String categoryName;

    /**
     * 分类编码（不持久化，用于查询结果）
     */
    private String categoryCode;

    /**
     * 标签列表（不持久化，从tags字段解析）
     */
    private transient List<String> tagList;

    /**
     * 表单配置对象（不持久化，从formConfig字段解析）
     */
    private transient Map<String, Object> formConfigMap;

    // ============ 辅助方法 ============

    /**
     * 自动生成UUID并在构造函数中设置
     */
    public ProcessTemplateDraft(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
    }

    /**
     * 获取标签列表
     * <p>如果tags字段为空，返回空列表
     *
     * @return 标签列表
     */
    public List<String> getTagList() {
        if (tagList != null) {
            return tagList;
        }
        if (tags == null || tags.trim().isEmpty()) {
            tagList = List.of();
        } else {
            try {
                tagList = JsonUtil.fromJson(tags, List.class);
            } catch (Exception e) {
                tagList = List.of();
            }
        }
        return tagList;
    }

    /**
     * 设置标签列表
     * <p>将List序列化为JSON字符串存储到tags字段
     *
     * @param tagList 标签列表
     */
    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = JsonUtil.toJson(tagList);
        }
    }

    /**
     * 获取表单配置对象
     * <p>如果formConfig字段为空，返回空Map
     *
     * @return 表单配置对象
     */
    public Map<String, Object> getFormConfigMap() {
        if (formConfigMap != null) {
            return formConfigMap;
        }
        if (formConfig == null || formConfig.trim().isEmpty()) {
            formConfigMap = new HashMap<>();
        } else {
            try {
                formConfigMap = JsonUtil.fromJson(formConfig, Map.class);
            } catch (Exception e) {
                formConfigMap = new HashMap<>();
            }
        }
        return formConfigMap;
    }

    /**
     * 设置表单配置对象
     * <p>将Map序列化为JSON字符串存储到formConfig字段
     *
     * @param formConfigMap 表单配置对象
     */
    public void setFormConfigMap(Map<String, Object> formConfigMap) {
        this.formConfigMap = formConfigMap;
        if (formConfigMap == null || formConfigMap.isEmpty()) {
            this.formConfig = null;
        } else {
            this.formConfig = JsonUtil.toJson(formConfigMap);
        }
    }

    /**
     * 初始化默认值
     * <p>在创建新实体时调用，设置状态为DRAFT，生成ID
     */
    public void initDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = ProcessTemplateStatus.DRAFT;
        }
        if (this.version == null) {
            this.version = 1;
        }
    }

    /**
     * 更新时间戳和更新人
     *
     * @param updatedBy 更新人
     */
    public void markUpdated(String updatedBy) {
        this.updatedTime = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 标记为已创建
     *
     * @param createdBy 创建人
     */
    public void markCreated(String createdBy) {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }
}
