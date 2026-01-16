package com.lingflow.repository;

import com.lingflow.entity.ProcessCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程分类数据访问接口
 */
@Mapper
public interface ProcessCategoryRepository {

    // ============ 基础 CRUD ============

    /**
     * 根据ID查询分类
     */
    ProcessCategory findById(@Param("id") String id);

    /**
     * 根据编码查询分类（多租户）
     */
    ProcessCategory findByCode(
        @Param("code") String code,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    /**
     * 查询所有分类（多租户）
     */
    List<ProcessCategory> findAll(
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    /**
     * 根据父ID查询子分类列表（多租户）
     */
    List<ProcessCategory> findByParentId(
        @Param("parentId") String parentId,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    // ============ 树形结构 ============

    /**
     * 查询分类树（多租户）
     * 返回所有分类，包含父子关系
     */
    List<ProcessCategory> findTree(
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    // ============ 搜索相关 ============

    /**
     * 根据名称或编码搜索分类（模糊搜索，多租户）
     */
    List<ProcessCategory> searchByName(
        @Param("keyword") String keyword,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    /**
     * 根据路径前缀查询分类（用于查询某个节点下的所有后代）
     */
    List<ProcessCategory> findByPathLike(
        @Param("pathPattern") String pathPattern,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    // ============ 操作 ============

    /**
     * 保存分类
     */
    void save(ProcessCategory category);

    /**
     * 更新分类
     */
    void update(ProcessCategory category);

    /**
     * 更新分类路径
     */
    void updatePath(
        @Param("id") String id,
        @Param("path") String path,
        @Param("level") Integer level
    );

    /**
     * 更新乐观锁版本号
     * @return 影响的行数
     */
    int updateVersion(@Param("id") String id);

    /**
     * 删除分类（物理删除）
     */
    void delete(@Param("id") String id);

    /**
     * 软删除分类
     */
    void softDelete(@Param("id") String id);

    // ============ 统计 ============

    /**
     * 统计子分类数量
     */
    int countByParentId(@Param("parentId") String parentId);

    /**
     * 统计分类下的流程数量
     */
    int countProcessesByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 检查分类编码是否存在
     */
    int countByCode(
        @Param("code") String code,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    /**
     * 检查分类编码是否存在（排除指定ID）
     */
    int countByCodeExcludeId(
        @Param("code") String code,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId,
        @Param("excludeId") String excludeId
    );
}
