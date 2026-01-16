package com.lingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 流程分类树数据传输对象
 * 用于返回树形结构的分类数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCategoryTreeDTO {
    /**
     * 分类ID
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 父分类ID
     */
    private String parentId;

    /**
     * 完整路径（格式：/id1/id2/id3）
     */
    private String path;

    /**
     * 路径名称（格式：根节点/子节点1/子节点2）
     */
    private String pathNames;

    /**
     * 层级深度
     */
    private Integer level;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 图标
     */
    private String icon;

    /**
     * 该分类下的流程数量
     */
    private Integer processCount;

    /**
     * 是否有子分类
     */
    private Boolean hasChildren;

    /**
     * 子分类列表
     */
    private List<ProcessCategoryTreeDTO> children;
}
