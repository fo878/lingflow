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
 * 发布态流程模板实体类
 *
 * <p>用于存储已发布到Flowable的流程模板
 *
 * <p>主要特点：
 * <ul>
 *   <li>已调用Flowable deploy API</li>
 *   <li>ACTIVE状态可以启动流程实例</li>
 *   <li>INACTIVE状态不能启动新流程实例</li>
 *   <li>不可以删除（保留历史）</li>
 *   <li>可以创建快照</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplatePublished {

    /**
     * 主键ID (UUID)
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
     * Flowable流程定义ID
     */
    private String flowableProcessDefinitionId;

    /**
     * Flowable部署ID
     */
    private String flowableDeploymentId;

    /**
     * Flowable版本号
     */
    private Integer flowableVersion;

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
     * 状态（ACTIVE/INACTIVE）
     */
    private ProcessTemplateStatus status;

    /**
     * 流程实例总数
     */
    private Integer instanceCount;

    /**
     * 运行中的流程实例数
     */
    private Integer runningInstanceCount;

    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;

    /**
     * 停用时间
     */
    private LocalDateTime suspendedTime;

    /**
     * 发布人
     */
    private String createdBy;

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
    public ProcessTemplatePublished(String id) {
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
     * <p>在创建新实体时调用，生成ID，设置默认值
     */
    public void initDefaults() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.status == null) {
            this.status = ProcessTemplateStatus.ACTIVE;
        }
        if (this.instanceCount == null) {
            this.instanceCount = 0;
        }
        if (this.runningInstanceCount == null) {
            this.runningInstanceCount = 0;
        }
        if (this.version == null) {
            this.version = 1;
        }
    }

    /**
     * 标记为已发布
     *
     * @param createdBy 发布人
     */
    public void markPublished(String createdBy) {
        this.publishedTime = LocalDateTime.now();
        this.createdBy = createdBy;
    }

    /**
     * 标记为已停用
     */
    public void markSuspended() {
        this.status = ProcessTemplateStatus.INACTIVE;
        this.suspendedTime = LocalDateTime.now();
    }

    /**
     * 标记为已激活
     */
    public void markActivated() {
        this.status = ProcessTemplateStatus.ACTIVE;
        this.suspendedTime = null;
    }

    /**
     * 增加流程实例计数
     */
    public void incrementInstanceCount() {
        if (this.instanceCount == null) {
            this.instanceCount = 0;
        }
        this.instanceCount++;
    }

    /**
     * 增加运行中的流程实例计数
     */
    public void incrementRunningInstanceCount() {
        if (this.runningInstanceCount == null) {
            this.runningInstanceCount = 0;
        }
        this.runningInstanceCount++;
    }

    /**
     * 减少运行中的流程实例计数
     */
    public void decrementRunningInstanceCount() {
        if (this.runningInstanceCount == null || this.runningInstanceCount <= 0) {
            this.runningInstanceCount = 0;
        } else {
            this.runningInstanceCount--;
        }
    }

    /**
     * 检查是否为激活状态
     *
     * @return true如果激活，否则false
     */
    public boolean isActive() {
        return ProcessTemplateStatus.ACTIVE.equals(this.status);
    }

    /**
     * 检查是否为停用状态
     *
     * @return true如果停用，否则false
     */
    public boolean isInactive() {
        return ProcessTemplateStatus.INACTIVE.equals(this.status);
    }

    /**
     * 检查是否有运行中的流程实例
     *
     * @return true如果有运行中的实例，否则false
     */
    public boolean hasRunningInstances() {
        return this.runningInstanceCount != null && this.runningInstanceCount > 0;
    }
}
