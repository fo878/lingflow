package com.lingflow.controller;

import com.lingflow.dto.*;
import com.lingflow.enums.ProcessTemplateStatus;
import com.lingflow.service.ProcessTemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程模板控制器
 *
 * <p>提供流程模板的完整生命周期管理API，包括：
 * <ul>
 *   <li>设计态模板管理（CRUD）</li>
 *   <li>发布态模板管理（发布、停用、激活）</li>
 *   <li>快照管理（创建、查询、恢复、删除）</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@RestController
@RequestMapping("/api/process/template")
public class ProcessTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessTemplateController.class);

    @Autowired
    private ProcessTemplateService templateService;

    // ============ 设计态模板管理 ============

    /**
     * 创建设计态模板
     * POST /api/process/template/draft
     */
    @PostMapping("/draft")
    public Result<ProcessTemplateDraftVO> createDraft(
            @Valid @RequestBody CreateDraftTemplateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        try {
            logger.info("创建设计态模板: templateKey={}, userId={}", request.getTemplateKey(), userId);
            ProcessTemplateDraftVO result = templateService.createDraft(request, userId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("创建设计态模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新设计态模板
     * PUT /api/process/template/draft/{id}
     */
    @PutMapping("/draft/{id}")
    public Result<ProcessTemplateDraftVO> updateDraft(
            @PathVariable String id,
            @Valid @RequestBody UpdateDraftTemplateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        try {
            logger.info("更新设计态模板: id={}, userId={}", id, userId);
            ProcessTemplateDraftVO result = templateService.updateDraft(id, request, userId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("更新设计态模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询设计态模板
     * GET /api/process/template/draft/{id}
     */
    @GetMapping("/draft/{id}")
    public Result<ProcessTemplateDraftVO> getDraft(@PathVariable String id) {
        try {
            logger.info("查询设计态模板: id={}", id);
            ProcessTemplateDraftVO result = templateService.getDraft(id);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询设计态模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除设计态模板
     * DELETE /api/process/template/draft/{id}
     */
    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(@PathVariable String id) {
        try {
            logger.info("删除设计态模板: id={}", id);
            templateService.deleteDraft(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除设计态模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询设计态模板列表
     * GET /api/process/template/draft
     *
     * @param tenantId 租户ID（必需）
     * @param appId 应用ID（可选）
     * @param contextId 上下文ID（可选）
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量（可选）
     * @param limit 限制数量（可选）
     */
    @GetMapping("/draft")
    public Result<List<ProcessTemplateDraftVO>> listDrafts(
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        try {
            logger.info("查询设计态模板列表: tenantId={}, categoryId={}, keyword={}",
                    tenantId, categoryId, keyword);
            List<ProcessTemplateDraftVO> result = templateService.listDraftTemplates(
                    tenantId, appId, contextId, categoryId, keyword, offset, limit
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询设计态模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 发布态模板管理 ============

    /**
     * 发布模板（设计态 → 发布态）
     * POST /api/process/template/draft/{id}/publish
     */
    @PostMapping("/draft/{id}/publish")
    public Result<ProcessTemplatePublishedVO> publishTemplate(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        try {
            logger.info("发布模板: draftId={}, userId={}", id, userId);
            ProcessTemplatePublishedVO result = templateService.publishTemplate(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("发布模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停用模板（激活态 → 停用态）
     * POST /api/process/template/published/{id}/suspend
     */
    @PostMapping("/published/{id}/suspend")
    public Result<Void> suspendTemplate(@PathVariable String id) {
        try {
            logger.info("停用模板: publishedId={}", id);
            templateService.suspendTemplate(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("停用模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 激活模板（停用态 → 激活态）
     * POST /api/process/template/published/{id}/activate
     */
    @PostMapping("/published/{id}/activate")
    public Result<Void> activateTemplate(@PathVariable String id) {
        try {
            logger.info("激活模板: publishedId={}", id);
            templateService.activateTemplate(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("激活模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询发布态模板
     * GET /api/process/template/published/{id}
     */
    @GetMapping("/published/{id}")
    public Result<ProcessTemplatePublishedVO> getPublished(@PathVariable String id) {
        try {
            logger.info("查询发布态模板: id={}", id);
            ProcessTemplatePublishedVO result = templateService.getPublished(id);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询发布态模板失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询发布态模板列表
     * GET /api/process/template/published
     *
     * @param tenantId 租户ID（必需）
     * @param appId 应用ID（可选）
     * @param contextId 上下文ID（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（ACTIVE/INACTIVE，可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量（可选）
     * @param limit 限制数量（可选）
     */
    @GetMapping("/published")
    public Result<List<ProcessTemplatePublishedVO>> listPublished(
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) ProcessTemplateStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ) {
        try {
            logger.info("查询发布态模板列表: tenantId={}, categoryId={}, status={}, keyword={}",
                    tenantId, categoryId, status, keyword);
            List<ProcessTemplatePublishedVO> result = templateService.listPublished(
                    tenantId, appId, contextId, categoryId, status, keyword, offset, limit
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询发布态模板列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ============ 快照管理 ============

    /**
     * 创建快照
     * POST /api/process/template/snapshot
     */
    @PostMapping("/snapshot")
    public Result<ProcessTemplateSnapshotVO> createSnapshot(
            @Valid @RequestBody CreateTemplateSnapshotRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        try {
            logger.info("创建快照: sourceTemplateId={}, sourceTemplateType={}, snapshotName={}, userId={}",
                    request.getSourceTemplateId(), request.getSourceTemplateType(),
                    request.getSnapshotName(), userId);
            ProcessTemplateSnapshotVO result = templateService.createSnapshot(request, userId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("创建快照失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询快照列表
     * GET /api/process/template/snapshot/{templateKey}
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID（必需）
     * @param appId 应用ID（可选）
     * @param contextId 上下文ID（可选）
     */
    @GetMapping("/snapshot/{templateKey}")
    public Result<List<ProcessTemplateSnapshotVO>> listSnapshots(
            @PathVariable String templateKey,
            @RequestParam String tenantId,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String contextId
    ) {
        try {
            logger.info("查询快照列表: templateKey={}, tenantId={}", templateKey, tenantId);
            List<ProcessTemplateSnapshotVO> result = templateService.listSnapshots(
                    templateKey, tenantId, appId, contextId
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询快照列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从快照恢复到设计态
     * POST /api/process/template/snapshot/restore
     */
    @PostMapping("/snapshot/restore")
    public Result<ProcessTemplateDraftVO> restoreFromSnapshot(
            @Valid @RequestBody RestoreSnapshotRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        try {
            logger.info("从快照恢复: snapshotId={}, userId={}", request.getSnapshotId(), userId);
            ProcessTemplateDraftVO result = templateService.restoreFromSnapshot(request, userId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("从快照恢复失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除快照
     * DELETE /api/process/template/snapshot/{id}
     */
    @DeleteMapping("/snapshot/{id}")
    public Result<Void> deleteSnapshot(@PathVariable String id) {
        try {
            logger.info("删除快照: id={}", id);
            templateService.deleteSnapshot(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除快照失败", e);
            return Result.error(e.getMessage());
        }
    }
}
