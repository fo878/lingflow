package com.lingflow.repository;

import com.lingflow.entity.ProcessDefinitionExtension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程定义扩展数据访问接口
 */
@Mapper
public interface ProcessDefinitionExtensionRepository {

    /**
     * 根据流程定义ID查询扩展信息
     */
    ProcessDefinitionExtension findByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId);

    /**
     * 根据分类ID查询所有流程定义扩展
     */
    List<ProcessDefinitionExtension> findByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 根据分类ID列表查询所有流程定义扩展
     */
    List<ProcessDefinitionExtension> findByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * 搜索流程（支持模糊搜索，多租户）
     */
    List<ProcessDefinitionExtension> searchProcesses(
        @Param("keyword") String keyword,
        @Param("categoryId") String categoryId,
        @Param("tenantId") String tenantId,
        @Param("appId") String appId,
        @Param("contextId") String contextId
    );

    /**
     * 统计分类下的流程数量
     */
    int countByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 统计多个分类下的流程数量
     */
    int countByCategoryIds(@Param("categoryIds") List<String> categoryIds);

    /**
     * 保存流程定义扩展
     */
    void save(@Param("extension") ProcessDefinitionExtension extension);

    /**
     * 更新流程定义扩展
     */
    void update(@Param("extension") ProcessDefinitionExtension extension);

    /**
     * 删除流程定义扩展
     */
    void delete(@Param("processDefinitionId") String processDefinitionId);
}
