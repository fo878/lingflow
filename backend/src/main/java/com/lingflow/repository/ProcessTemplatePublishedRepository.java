package com.lingflow.repository;

import com.lingflow.entity.ProcessTemplatePublished;
import com.lingflow.enums.ProcessTemplateStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 发布态流程模板数据访问接口
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Mapper
public interface ProcessTemplatePublishedRepository {

    // ============ 基础 CRUD ============

    /**
     * 根据ID查询发布态模板
     *
     * @param id 模板ID
     * @return 发布态模板
     */
    ProcessTemplatePublished findById(@Param("id") String id);

    /**
     * 根据模板Key查询发布态模板（多租户）
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表（包含所有版本）
     */
    List<ProcessTemplatePublished> findByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据模板Key查询激活态模板（多租户）
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 激活态模板
     */
    ProcessTemplatePublished findActiveByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据Flowable流程定义ID查询发布态模板
     *
     * @param flowableProcessDefinitionId Flowable流程定义ID
     * @return 发布态模板
     */
    ProcessTemplatePublished findByFlowableProcessDefinitionId(
            @Param("flowableProcessDefinitionId") String flowableProcessDefinitionId
    );

    /**
     * 查询所有发布态模板（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> findAll(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据分类ID查询发布态模板列表（多租户）
     *
     * @param categoryId 分类ID
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> findByCategoryId(
            @Param("categoryId") String categoryId,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据状态查询发布态模板列表（多租户）
     *
     * @param status 状态
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> findByStatus(
            @Param("status") ProcessTemplateStatus status,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    // ============ 搜索相关 ============

    /**
     * 根据名称或描述搜索发布态模板（模糊搜索，多租户）
     *
     * @param keyword 关键词
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据标签搜索发布态模板（多租户）
     *
     * @param tag 标签
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> searchByTag(
            @Param("tag") String tag,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    // ============ 分页查询 ============

    /**
     * 分页查询发布态模板（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 发布态模板列表
     */
    List<ProcessTemplatePublished> findByPage(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("categoryId") String categoryId,
            @Param("status") ProcessTemplateStatus status,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计发布态模板数量（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 数量
     */
    int countByCondition(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("categoryId") String categoryId,
            @Param("status") ProcessTemplateStatus status,
            @Param("keyword") String keyword
    );

    // ============ 操作 ============

    /**
     * 保存发布态模板
     *
     * @param published 发布态模板
     */
    void save(ProcessTemplatePublished published);

    /**
     * 更新发布态模板
     *
     * @param published 发布态模板
     * @return 影响的行数
     */
    int update(ProcessTemplatePublished published);

    /**
     * 更新状态
     *
     * @param id 模板ID
     * @param status 状态
     * @return 影响的行数
     */
    int updateStatus(
            @Param("id") String id,
            @Param("status") ProcessTemplateStatus status
    );

    /**
     * 更新停用时间
     *
     * @param id 模板ID
     * @param suspendedTime 停用时间
     */
    void updateSuspendedTime(
            @Param("id") String id,
            @Param("suspendedTime") java.time.LocalDateTime suspendedTime
    );

    /**
     * 更新流程实例计数
     *
     * @param id 模板ID
     * @param increment 增量
     */
    void updateInstanceCount(
            @Param("id") String id,
            @Param("increment") Integer increment
    );

    /**
     * 更新运行中的流程实例计数
     *
     * @param id 模板ID
     * @param increment 增量
     */
    void updateRunningInstanceCount(
            @Param("id") String id,
            @Param("increment") Integer increment
    );

    /**
     * 检查是否存在激活的模板
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 数量
     */
    int countActiveByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 获取下一个模板版本号
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 版本号
     */
    int getNextTemplateVersion(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );
}
