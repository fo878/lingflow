package com.lingflow.entity;

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
 * 流程模板快照实体类
 *
 * <p>用于存储流程模板的历史版本快照
 *
 * <p>主要特点：
 * <ul>
 *   <li>快照版本号是独立序列，与模板版本号完全独立</li>
 *   <li>可以恢复到设计态</li>
 *   <li>不能直接恢复到发布态</li>
 *   <li>快照本身不会被删除，可以多次恢复</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplateSnapshot {

    /**
     * 主键ID (UUID)
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
     * BPMN XML内容（快照时的完整内容）
     */
    private String bpmnXml;

    /**
     * 来源模板ID
     */
    private String sourceTemplateId;

    /**
     * 来源模板状态（DRAFT/ACTIVE/INACTIVE）
     */
    private ProcessTemplateStatus sourceTemplateStatus;

    /**
     * 来源模板版本号
     */
    private Integer sourceTemplateVersion;

    /**
     * 分类ID（冗余，方便查询）
     */
    private String categoryId;

    /**
     * 分类名称（冗余，快照时的分类名）
     */
    private String categoryName;

    /**
     * 分类编码（冗余）
     */
    private String categoryCode;

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
     * 快照版本号（独立序列）
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

    // ============ 非持久化字段 ============

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
    public ProcessTemplateSnapshot(String id) {
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
     * <p>在创建新实体时调用，生成ID
     */
    public void initDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.snapshotVersion == null) {
            this.snapshotVersion = 1;
        }
    }

    /**
     * 标记为已创建
     *
     * @param createdBy 创建人
     */
    public void markCreated(String createdBy) {
        this.createdTime = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    /**
     * 从设计态模板创建快照
     *
     * @param draft 设计态模板
     * @param snapshotName 快照名称
     * @param createdBy 创建人
     * @return 快照实例
     */
    public static ProcessTemplateSnapshot fromDraft(
            ProcessTemplateDraft draft,
            String snapshotName,
            String createdBy) {
        return ProcessTemplateSnapshot.builder()
                .id(UUID.randomUUID().toString())
                .templateKey(draft.getTemplateKey())
                .snapshotName(snapshotName)
                .bpmnXml(draft.getBpmnXml())
                .sourceTemplateId(draft.getId())
                .sourceTemplateStatus(ProcessTemplateStatus.DRAFT)
                .sourceTemplateVersion(draft.getVersion())
                .categoryId(draft.getCategoryId())
                .categoryName(draft.getCategoryName())
                .categoryCode(draft.getCategoryCode())
                .tags(draft.getTags())
                .formConfig(draft.getFormConfig())
                .appId(draft.getAppId())
                .contextId(draft.getContextId())
                .tenantId(draft.getTenantId())
                .createdTime(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }

    /**
     * 从发布态模板创建快照
     *
     * @param published 发布态模板
     * @param snapshotName 快照名称
     * @param createdBy 创建人
     * @return 快照实例
     */
    public static ProcessTemplateSnapshot fromPublished(
            ProcessTemplatePublished published,
            String snapshotName,
            String createdBy) {
        return ProcessTemplateSnapshot.builder()
                .id(UUID.randomUUID().toString())
                .templateKey(published.getTemplateKey())
                .snapshotName(snapshotName)
                .bpmnXml(published.getBpmnXml())
                .sourceTemplateId(published.getId())
                .sourceTemplateStatus(published.getStatus())
                .sourceTemplateVersion(published.getFlowableVersion())
                .categoryId(published.getCategoryId())
                .categoryName(published.getCategoryName())
                .categoryCode(published.getCategoryCode())
                .tags(published.getTags())
                .formConfig(published.getFormConfig())
                .appId(published.getAppId())
                .contextId(published.getContextId())
                .tenantId(published.getTenantId())
                .createdTime(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }
}
