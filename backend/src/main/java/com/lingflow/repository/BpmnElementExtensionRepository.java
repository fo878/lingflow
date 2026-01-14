package com.lingflow.repository;

import com.lingflow.entity.BpmnElementExtension;
import com.lingflow.entity.BpmnElementExtensionHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * BPMN元素扩展属性数据访问接口
 */
public interface BpmnElementExtensionRepository {
    
    /**
     * 保存BPMN元素扩展属性
     * @param extension 扩展属性对象
     */
    void save(BpmnElementExtension extension);
    
    /**
     * 更新BPMN元素扩展属性
     * @param extension 扩展属性对象
     */
    void update(BpmnElementExtension extension);
    
    /**
     * 根据流程定义ID和元素ID查找扩展属性
     * @param processDefinitionId 流程定义ID
     * @param elementId 元素ID
     * @return 扩展属性对象
     */
    BpmnElementExtension findByProcessAndElement(@Param("processDefinitionId") String processDefinitionId, 
                                               @Param("elementId") String elementId);
    
    /**
     * 根据流程定义ID查找所有扩展属性
     * @param processDefinitionId 流程定义ID
     * @return 扩展属性列表
     */
    List<BpmnElementExtension> findByProcessDefinitionId(@Param("processDefinitionId") String processDefinitionId);
    
    /**
     * 根据元素类型查找扩展属性
     * @param elementType 元素类型
     * @return 扩展属性列表
     */
    List<BpmnElementExtension> findByElementType(@Param("elementType") String elementType);
    
    /**
     * 删除BPMN元素扩展属性
     * @param processDefinitionId 流程定义ID
     * @param elementId 元素ID
     */
    void deleteByProcessAndElement(@Param("processDefinitionId") String processDefinitionId, 
                                 @Param("elementId") String elementId);
    
    /**
     * 保存扩展属性历史记录
     * @param history 历史记录对象
     */
    void saveHistory(BpmnElementExtensionHistory history);
    
    /**
     * 根据扩展ID查找历史记录
     * @param extensionId 扩展属性ID
     * @return 历史记录列表
     */
    List<BpmnElementExtensionHistory> findHistoryByExtensionId(@Param("extensionId") Long extensionId);
}
