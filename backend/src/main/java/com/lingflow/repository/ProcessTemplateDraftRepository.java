package com.lingflow.repository;

import com.lingflow.entity.ProcessTemplateDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设计态流程模板数据访问接口
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Mapper
public interface ProcessTemplateDraftRepository {

    // ============ 基础 CRUD ============

    /**
     * 根据ID查询设计态模板
     *
     * @param id 模板ID
     * @return 设计态模板
     */
    ProcessTemplateDraft findById(@Param("id") String id);

    /**
     * 根据模板Key查询设计态模板（多租户）
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 设计态模板
     */
    ProcessTemplateDraft findByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 查询所有设计态模板（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 设计态模板列表
     */
    List<ProcessTemplateDraft> findAll(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据分类ID查询设计态模板列表（多租户）
     *
     * @param categoryId 分类ID
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 设计态模板列表
     */
    List<ProcessTemplateDraft> findByCategoryId(
            @Param("categoryId") String categoryId,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    // ============ 搜索相关 ============

    /**
     * 根据名称或描述搜索设计态模板（模糊搜索，多租户）
     *
     * @param keyword 关键词
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 设计态模板列表
     */
    List<ProcessTemplateDraft> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据标签搜索设计态模板（多租户）
     *
     * @param tag 标签
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 设计态模板列表
     */
    List<ProcessTemplateDraft> searchByTag(
            @Param("tag") String tag,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    // ============ 分页查询 ============

    /**
     * 分页查询设计态模板（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 设计态模板列表
     */
    List<ProcessTemplateDraft> findByPage(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("categoryId") String categoryId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计设计态模板数量（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @return 数量
     */
    int countByCondition(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("categoryId") String categoryId,
            @Param("keyword") String keyword
    );

    // ============ 操作 ============

    /**
     * 保存设计态模板
     *
     * @param draft 设计态模板
     */
    void save(ProcessTemplateDraft draft);

    /**
     * 更新设计态模板
     *
     * @param draft 设计态模板
     * @return 影响的行数
     */
    int update(ProcessTemplateDraft draft);

    /**
     * 删除设计态模板
     *
     * @param id 模板ID
     */
    void delete(@Param("id") String id);

    /**
     * 检查模板Key是否存在
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 数量
     */
    int countByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 检查模板Key是否存在（排除指定ID）
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param excludeId 排除的ID
     * @return 数量
     */
    int countByTemplateKeyExcludeId(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("excludeId") String excludeId
    );
}
