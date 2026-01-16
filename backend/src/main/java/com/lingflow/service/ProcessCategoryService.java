package com.lingflow.service;

import com.lingflow.dto.*;
import com.lingflow.entity.ProcessCategory;
import com.lingflow.entity.ProcessDefinitionExtension;
import com.lingflow.repository.ProcessCategoryRepository;
import com.lingflow.repository.ProcessDefinitionExtensionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程分类服务类
 */
@Service
public class ProcessCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessCategoryService.class);

    @Autowired
    private ProcessCategoryRepository categoryRepository;

    @Autowired
    private ProcessDefinitionExtensionRepository extensionRepository;

    // ============ 查询相关 ============

    /**
     * 获取分类树
     */
    public List<ProcessCategoryTreeDTO> getCategoryTree(String tenantId, String appId, String contextId) {
        logger.info("获取分类树: tenantId={}, appId={}, contextId={}", tenantId, appId, contextId);

        // 查询所有分类
        List<ProcessCategory> categories = categoryRepository.findTree(tenantId, appId, contextId);

        // 构建树形结构
        return buildTree(categories);
    }

    /**
     * 构建树形结构
     */
    private List<ProcessCategoryTreeDTO> buildTree(List<ProcessCategory> categories) {
        // 创建ID到节点的映射
        Map<String, ProcessCategoryTreeDTO> nodeMap = new HashMap<>();
        List<ProcessCategoryTreeDTO> roots = new ArrayList<>();

        // 第一遍：创建所有节点
        for (ProcessCategory category : categories) {
            ProcessCategoryTreeDTO node = convertToTreeDTO(category);
            nodeMap.put(category.getId(), node);
        }

        // 第二遍：构建父子关系
        for (ProcessCategory category : categories) {
            ProcessCategoryTreeDTO node = nodeMap.get(category.getId());

            if (category.getParentId() == null) {
                // 根节点
                roots.add(node);
            } else {
                // 添加到父节点
                ProcessCategoryTreeDTO parent = nodeMap.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        // 第三遍：设置子分类标识和流程数量
        for (ProcessCategoryTreeDTO node : nodeMap.values()) {
            // 设置是否有子分类
            node.setHasChildren(node.getChildren() != null && !node.getChildren().isEmpty());

            // 设置流程数量
            if (node.getChildren() == null || node.getChildren().isEmpty()) {
                // 叶子节点，直接统计
                int count = extensionRepository.countByCategoryId(node.getId());
                node.setProcessCount(count);
            } else {
                // 非叶子节点，递归统计
                node.setProcessCount(countProcessesRecursively(node));
            }
        }

        // 对根节点按排序
        roots.sort(Comparator.comparing(ProcessCategoryTreeDTO::getSortOrder)
                .thenComparing(ProcessCategoryTreeDTO::getId));

        return roots;
    }

    /**
     * 递归统计流程数量
     */
    private int countProcessesRecursively(ProcessCategoryTreeDTO node) {
        int count = 0;

        // 统计当前分类下的流程
        count += extensionRepository.countByCategoryId(node.getId());

        // 递归统计子分类
        if (node.getChildren() != null) {
            for (ProcessCategoryTreeDTO child : node.getChildren()) {
                count += countProcessesRecursively(child);
            }
        }

        return count;
    }

    /**
     * 转换为树形DTO
     */
    private ProcessCategoryTreeDTO convertToTreeDTO(ProcessCategory category) {
        return ProcessCategoryTreeDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .parentId(category.getParentId())
                .path(category.getPath())
                .pathNames(buildPathNames(category.getPath()))
                .level(category.getLevel())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .icon(category.getIcon())
                .build();
    }

    /**
     * 从路径ID构建路径名称
     */
    private String buildPathNames(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String[] ids = path.split("/");
        List<String> names = new ArrayList<>();

        for (String id : ids) {
            if (!id.isEmpty()) {
                ProcessCategory category = categoryRepository.findById(id);
                if (category != null) {
                    names.add(category.getName());
                }
            }
        }

        return String.join("/", names);
    }

    // ============ 创建相关 ============

    /**
     * 创建分类（自动计算路径和层级）
     */
    @Transactional
    public ProcessCategory createCategory(ProcessCategoryDTO dto) {
        logger.info("创建分类: name={}, code={}, parentId={}", dto.getName(), dto.getCode(), dto.getParentId());

        // 1. 检查编码是否已存在
        int count = categoryRepository.countByCode(dto.getCode(), dto.getTenantId(), dto.getAppId(), dto.getContextId());
        if (count > 0) {
            throw new RuntimeException("分类编码已存在: " + dto.getCode());
        }

        // 2. 创建分类实体
        ProcessCategory category = new ProcessCategory();
        category.setId(UUID.randomUUID().toString());
        category.setName(dto.getName());
        category.setCode(dto.getCode());
        category.setParentId(dto.getParentId());
        category.setTenantId(dto.getTenantId());
        category.setAppId(dto.getAppId());
        category.setContextId(dto.getContextId());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        category.setIcon(dto.getIcon());
        category.setCreatedBy("system"); // TODO: 从安全上下文获取当前用户
        category.setUpdatedBy("system");
        category.setIsDeleted(false);
        category.setVersion(1);
        category.setCreatedTime(LocalDateTime.now());
        category.setUpdatedTime(LocalDateTime.now());

        // 3. 计算路径和层级
        if (dto.getParentId() != null) {
            ProcessCategory parent = categoryRepository.findById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在: " + dto.getParentId());
            }
            category.setPath(parent.getPath() + "/" + category.getId());
            category.setLevel(parent.getLevel() + 1);
        } else {
            category.setPath("/" + category.getId());
            category.setLevel(0);
        }

        // 4. 保存分类
        categoryRepository.save(category);

        logger.info("分类创建成功: id={}, path={}", category.getId(), category.getPath());
        return category;
    }

    // ============ 更新相关 ============

    /**
     * 更新分类
     */
    @Transactional
    public void updateCategory(String id, ProcessCategoryDTO dto) {
        logger.info("更新分类: id={}", id);

        ProcessCategory category = categoryRepository.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在: " + id);
        }

        // 检查编码是否被其他分类使用
        int count = categoryRepository.countByCodeExcludeId(
                dto.getCode(), dto.getTenantId(), dto.getAppId(), dto.getContextId(), id);
        if (count > 0) {
            throw new RuntimeException("分类编码已被使用: " + dto.getCode());
        }

        // 更新字段
        category.setName(dto.getName());
        category.setCode(dto.getCode());
        category.setDescription(dto.getDescription());
        category.setSortOrder(dto.getSortOrder());
        category.setIcon(dto.getIcon());
        category.setUpdatedBy("system");

        categoryRepository.update(category);
        logger.info("分类更新成功: id={}", id);
    }

    /**
     * 移动分类（修改父节点，自动更新路径和层级）
     */
    @Transactional
    public void moveCategory(String id, String newParentId) {
        logger.info("移动分类: id={}, newParentId={}", id, newParentId);

        ProcessCategory category = categoryRepository.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在: " + id);
        }

        String oldPath = category.getPath();

        // 计算新路径
        String newPath;
        Integer newLevel;
        if (newParentId != null) {
            ProcessCategory newParent = categoryRepository.findById(newParentId);
            if (newParent == null) {
                throw new RuntimeException("目标父分类不存在: " + newParentId);
            }
            // 检查是否会形成循环引用
            if (newParent.getPath().startsWith(oldPath)) {
                throw new RuntimeException("不能将分类移动到其子节点下");
            }
            newPath = newParent.getPath() + "/" + id;
            newLevel = newParent.getLevel() + 1;
        } else {
            newPath = "/" + id;
            newLevel = 0;
        }

        // 更新当前节点
        category.setParentId(newParentId);
        category.setPath(newPath);
        category.setLevel(newLevel);
        categoryRepository.update(category);

        // 递归更新所有子节点的路径
        updateChildrenPath(id, oldPath, newPath);

        logger.info("分类移动成功: id={}, oldPath={}, newPath={}", id, oldPath, newPath);
    }

    /**
     * 递归更新子节点路径
     */
    private void updateChildrenPath(String parentId, String oldParentPath, String newParentPath) {
        List<ProcessCategory> children = categoryRepository.findByParentId(parentId, null, null, null);

        for (ProcessCategory child : children) {
            String oldPath = child.getPath();
            String newPath = newParentPath + "/" + child.getId();
            Integer newLevel = calculateLevel(newPath);

            child.setPath(newPath);
            child.setLevel(newLevel);
            categoryRepository.update(child);

            // 递归更新子节点
            updateChildrenPath(child.getId(), oldPath, newPath);
        }
    }

    /**
     * 从路径计算层级
     */
    private Integer calculateLevel(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        String[] parts = path.split("/");
        return parts.length - 1;
    }

    /**
     * 更新分类排序
     */
    @Transactional
    public void updateSortOrder(String id, Integer sortOrder) {
        logger.info("更新分类排序: id={}, sortOrder={}", id, sortOrder);

        ProcessCategory category = categoryRepository.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在: " + id);
        }

        category.setSortOrder(sortOrder);
        categoryRepository.update(category);

        logger.info("分类排序更新成功: id={}", id);
    }

    /**
     * 批量更新排序（拖拽排序）
     */
    @Transactional
    public void batchUpdateSortOrder(List<SortOrderDTO> sortOrderList) {
        logger.info("批量更新排序: count={}", sortOrderList.size());

        for (SortOrderDTO dto : sortOrderList) {
            ProcessCategory category = categoryRepository.findById(dto.getId());
            if (category != null) {
                category.setSortOrder(dto.getSortOrder());
                categoryRepository.update(category);
            }
        }

        logger.info("批量排序更新成功");
    }

    // ============ 删除相关 ============

    /**
     * 删除分类（软删除）
     */
    @Transactional
    public void deleteCategory(String id) {
        logger.info("删除分类: id={}", id);

        ProcessCategory category = categoryRepository.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在: " + id);
        }

        // 检查是否有子分类
        int childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new RuntimeException("该分类下还有 " + childCount + " 个子分类，请先删除或移动子分类");
        }

        // 检查是否有流程
        int processCount = categoryRepository.countProcessesByCategoryId(id);
        if (processCount > 0) {
            throw new RuntimeException("该分类下还有 " + processCount + " 个流程，请先移动或删除流程");
        }

        // 软删除
        categoryRepository.softDelete(id);

        logger.info("分类删除成功: id={}", id);
    }

    // ============ 搜索相关 ============

    /**
     * 搜索分类（支持名称和编码模糊搜索）
     */
    public List<ProcessCategoryTreeDTO> searchCategories(String keyword, String tenantId, String appId, String contextId) {
        logger.info("搜索分类: keyword={}, tenantId={}", keyword, tenantId);

        List<ProcessCategory> categories = categoryRepository.searchByName(keyword, tenantId, appId, contextId);

        return categories.stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());
    }

    /**
     * 为流程定义设置分类
     */
    @Transactional
    public void setProcessCategory(
            String processDefinitionId,
            String categoryId,
            String tenantId,
            String appId,
            String contextId
    ) {
        logger.info("设置流程分类: processDefinitionId={}, categoryId={}", processDefinitionId, categoryId);

        ProcessDefinitionExtension extension = extensionRepository.findByProcessDefinitionId(processDefinitionId);

        if (extension == null) {
            // 创建新的扩展记录
            extension = new ProcessDefinitionExtension();
            extension.setId(UUID.randomUUID().toString());
            extension.setProcessDefinitionId(processDefinitionId);
            extension.setCategoryId(categoryId);
            extension.setTenantId(tenantId);
            extension.setAppId(appId);
            extension.setContextId(contextId);
            extension.setCreatedTime(LocalDateTime.now());
            extension.setUpdatedTime(LocalDateTime.now());
            extensionRepository.save(extension);
        } else {
            // 更新现有记录
            extension.setCategoryId(categoryId);
            extension.setUpdatedTime(LocalDateTime.now());
            extensionRepository.update(extension);
        }

        logger.info("流程分类设置成功: processDefinitionId={}, categoryId={}", processDefinitionId, categoryId);
    }

    /**
     * 搜索流程模板（支持模糊搜索，返回分类路径）
     */
    public List<ProcessSearchResultDTO> searchProcesses(
            String keyword,
            String categoryId,
            String tenantId,
            String appId,
            String contextId
    ) {
        logger.info("搜索流程: keyword={}, categoryId={}, tenantId={}", keyword, categoryId, tenantId);

        // 1. 搜索流程定义扩展
        List<ProcessDefinitionExtension> extensions = extensionRepository.searchProcesses(
                keyword, categoryId, tenantId, appId, contextId);

        // 2. 查询流程定义详细信息（从Flowable）
        // TODO: 集成Flowable的ProcessDefinition查询

        // 3. 构建结果并包含分类路径
        List<ProcessSearchResultDTO> results = new ArrayList<>();
        for (ProcessDefinitionExtension ext : extensions) {
            if (ext.getCategoryId() != null) {
                ProcessCategory category = categoryRepository.findById(ext.getCategoryId());
                if (category != null) {
                    ProcessSearchResultDTO result = ProcessSearchResultDTO.builder()
                            .id(ext.getProcessDefinitionId())
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .categoryPath(category.getPath())
                            .categoryPathNames(buildPathNames(category.getPath()))
                            .pathIds(buildPathIds(category.getPath()))
                            .build();
                    results.add(result);
                }
            }
        }

        return results;
    }

    /**
     * 构建路径ID列表
     */
    private List<String> buildPathIds(String path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(path.split("/"))
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
    }
}
