package com.lingflow.controller;

import com.lingflow.dto.ProcessCategoryDTO;
import com.lingflow.dto.ProcessCategoryTreeDTO;
import com.lingflow.dto.ProcessSearchResultDTO;
import com.lingflow.dto.Result;
import com.lingflow.dto.SortOrderDTO;
import com.lingflow.entity.ProcessCategory;
import com.lingflow.service.ProcessCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程分类控制器
 */
@RestController
@RequestMapping("/api/process-category")
public class ProcessCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessCategoryController.class);

    @Autowired
    private ProcessCategoryService categoryService;

    // ============ 查询相关 ============

    /**
     * 获取分类树
     * GET /api/process-category/tree?tenantId=xxx&appId=xxx&contextId=xxx
     */
    @GetMapping("/tree")
    public Result<List<ProcessCategoryTreeDTO>> getCategoryTree(
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId
    ) {
        try {
            logger.info("获取分类树: tenantId={}, appId={}, contextId={}", tenantId, appId, contextId);
            List<ProcessCategoryTreeDTO> tree = categoryService.getCategoryTree(tenantId, appId, contextId);
            return Result.success(tree);
        } catch (Exception e) {
            logger.error("获取分类树失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索分类
     * GET /api/process-category/search?keyword=xxx&tenantId=xxx
     */
    @GetMapping("/search")
    public Result<List<ProcessCategoryTreeDTO>> searchCategories(
            @RequestParam String keyword,
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId
    ) {
        try {
            logger.info("搜索分类: keyword={}, tenantId={}", keyword, tenantId);
            List<ProcessCategoryTreeDTO> categories = categoryService.searchCategories(
                    keyword, tenantId, appId, contextId);
            return Result.success(categories);
        } catch (Exception e) {
            logger.error("搜索分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 创建相关 ============

    /**
     * 创建分类
     * POST /api/process-category
     */
    @PostMapping
    public Result<ProcessCategory> createCategory(@RequestBody ProcessCategoryDTO dto) {
        try {
            logger.info("创建分类: name={}, code={}", dto.getName(), dto.getCode());
            ProcessCategory category = categoryService.createCategory(dto);
            return Result.success(category);
        } catch (Exception e) {
            logger.error("创建分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 更新相关 ============

    /**
     * 更新分类
     * PUT /api/process-category/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable String id,
            @RequestBody ProcessCategoryDTO dto
    ) {
        try {
            logger.info("更新分类: id={}", id);
            categoryService.updateCategory(id, dto);
            return Result.success();
        } catch (Exception e) {
            logger.error("更新分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移动分类
     * PUT /api/process-category/{id}/move
     */
    @PutMapping("/{id}/move")
    public Result<Void> moveCategory(
            @PathVariable String id,
            @RequestParam String newParentId
    ) {
        try {
            logger.info("移动分类: id={}, newParentId={}", id, newParentId);
            categoryService.moveCategory(id, newParentId);
            return Result.success();
        } catch (Exception e) {
            logger.error("移动分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新排序
     * PUT /api/process-category/{id}/sort
     */
    @PutMapping("/{id}/sort")
    public Result<Void> updateSortOrder(
            @PathVariable String id,
            @RequestParam Integer sortOrder
    ) {
        try {
            logger.info("更新排序: id={}, sortOrder={}", id, sortOrder);
            categoryService.updateSortOrder(id, sortOrder);
            return Result.success();
        } catch (Exception e) {
            logger.error("更新排序失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新排序（拖拽排序）
     * PUT /api/process-category/sort/batch
     */
    @PutMapping("/sort/batch")
    public Result<Void> batchUpdateSortOrder(@RequestBody List<SortOrderDTO> sortOrderList) {
        try {
            logger.info("批量更新排序: count={}", sortOrderList.size());
            categoryService.batchUpdateSortOrder(sortOrderList);
            return Result.success();
        } catch (Exception e) {
            logger.error("批量更新排序失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 删除相关 ============

    /**
     * 删除分类
     * DELETE /api/process-category/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id) {
        try {
            logger.info("删除分类: id={}", id);
            categoryService.deleteCategory(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 流程相关 ============

    /**
     * 设置流程分类
     * PUT /api/process-category/process/{processDefinitionId}
     */
    @PutMapping("/process/{processDefinitionId}")
    public Result<Void> setProcessCategory(
            @PathVariable String processDefinitionId,
            @RequestParam String categoryId,
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId
    ) {
        try {
            logger.info("设置流程分类: processDefinitionId={}, categoryId={}", processDefinitionId, categoryId);
            categoryService.setProcessCategory(processDefinitionId, categoryId, tenantId, appId, contextId);
            return Result.success();
        } catch (Exception e) {
            logger.error("设置流程分类失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索流程模板（支持模糊搜索，返回分类路径）
     * GET /api/process-category/processes/search?keyword=xxx&tenantId=xxx
     */
    @GetMapping("/processes/search")
    public Result<List<ProcessSearchResultDTO>> searchProcesses(
            @RequestParam String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId
    ) {
        try {
            logger.info("搜索流程: keyword={}, categoryId={}, tenantId={}", keyword, categoryId, tenantId);
            List<ProcessSearchResultDTO> results = categoryService.searchProcesses(
                    keyword, categoryId, tenantId, appId, contextId);
            return Result.success(results);
        } catch (Exception e) {
            logger.error("搜索流程失败", e);
            return Result.error(e.getMessage());
        }
    }
}
