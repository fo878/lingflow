package com.lingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 流程搜索结果数据传输对象
 * 用于返回搜索到的流程模板及其分类路径信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessSearchResultDTO {
    /**
     * 流程定义ID
     */
    private String id;

    /**
     * 流程Key
     */
    private String key;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类路径ID（格式：/root/id1/id2）
     */
    private String categoryPath;

    /**
     * 分类路径名称（格式：根节点/子节点1/子节点2）
     */
    private String categoryPathNames;

    /**
     * 路径ID列表（用于前端定位到树节点）
     * 例如：["rootId", "parent1Id", "categoryId"]
     */
    private List<String> pathIds;
}
