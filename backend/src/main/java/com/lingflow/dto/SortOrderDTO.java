package com.lingflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序数据传输对象
 * 用于批量更新排序
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortOrderDTO {
    /**
     * 分类ID
     */
    private String id;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
