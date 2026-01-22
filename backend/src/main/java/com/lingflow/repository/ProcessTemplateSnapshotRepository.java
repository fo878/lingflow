package com.lingflow.repository;

import com.lingflow.entity.ProcessTemplateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程模板快照数据访问接口
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Mapper
public interface ProcessTemplateSnapshotRepository {

    // ============ 基础 CRUD ============

    /**
     * 根据ID查询快照
     *
     * @param id 快照ID
     * @return 快照
     */
    ProcessTemplateSnapshot findById(@Param("id") String id);

    /**
     * 根据模板Key查询快照列表（多租户）
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 快照列表
     */
    List<ProcessTemplateSnapshot> findByTemplateKey(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 查询所有快照（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 快照列表
     */
    List<ProcessTemplateSnapshot> findAll(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    /**
     * 根据来源模板ID查询快照列表
     *
     * @param sourceTemplateId 来源模板ID
     * @return 快照列表
     */
    List<ProcessTemplateSnapshot> findBySourceTemplateId(
            @Param("sourceTemplateId") String sourceTemplateId
    );

    // ============ 搜索相关 ============

    /**
     * 根据快照名称搜索快照（模糊搜索，多租户）
     *
     * @param snapshotName 快照名称
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 快照列表
     */
    List<ProcessTemplateSnapshot> searchByName(
            @Param("snapshotName") String snapshotName,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );

    // ============ 分页查询 ============

    /**
     * 分页查询快照（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param templateKey 模板Key（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 快照列表
     */
    List<ProcessTemplateSnapshot> findByPage(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("templateKey") String templateKey,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计快照数量（多租户）
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param templateKey 模板Key（可选）
     * @return 数量
     */
    int countByCondition(
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId,
            @Param("templateKey") String templateKey
    );

    // ============ 操作 ============

    /**
     * 保存快照
     *
     * @param snapshot 快照
     */
    void save(ProcessTemplateSnapshot snapshot);

    /**
     * 删除快照
     *
     * @param id 快照ID
     */
    void delete(@Param("id") String id);

    /**
     * 获取下一个快照版本号
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 版本号
     */
    int getNextSnapshotVersion(
            @Param("templateKey") String templateKey,
            @Param("tenantId") String tenantId,
            @Param("appId") String appId,
            @Param("contextId") String contextId
    );
}
