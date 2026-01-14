package com.lingflow.repository;

import com.lingflow.entity.ProcessSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程快照数据访问接口
 */
public interface ProcessSnapshotRepository {
    
    /**
     * 保存流程快照
     * @param snapshot 流程快照对象
     */
    void save(ProcessSnapshot snapshot);
    
    /**
     * 根据流程定义KEY查询快照列表
     * @param processDefinitionKey 流程定义KEY
     * @return 快照列表
     */
    List<ProcessSnapshot> findByProcessDefinitionKey(@Param("processDefinitionKey") String processDefinitionKey);
    
    /**
     * 根据ID查询快照
     * @param id 快照ID
     * @return 流程快照对象
     */
    ProcessSnapshot findById(@Param("id") Long id);
    
    /**
     * 查询流程定义的最新快照
     * @param processDefinitionKey 流程定义KEY
     * @return 最新快照
     */
    ProcessSnapshot findLatestByProcessDefinitionKey(@Param("processDefinitionKey") String processDefinitionKey);
    
    /**
     * 根据ID删除快照
     * @param id 快照ID
     */
    void deleteById(@Param("id") Long id);
}
