package com.lingflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingflow.dto.*;
import com.lingflow.entity.*;
import com.lingflow.enums.ProcessTemplateStatus;
import com.lingflow.exception.BusinessException;
import com.lingflow.repository.*;
import com.lingflow.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 流程模板业务逻辑类
 *
 * <p>核心业务规则：
 * <ul>
 *   <li>设计态 → 发布态：调用Flowable deploy，复制到published表，删除draft记录</li>
 *   <li>发布态 → 停用态：调用Flowable suspend，更新published状态</li>
 *   <li>停用态 → 激活态：调用Flowable activate，更新published状态</li>
 *   <li>设计态可删除，发布态不可删除</li>
 *   <li>快照只能恢复到设计态，不能直接恢复到发布态</li>
 * </ul>
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
@Slf4j
@Service
public class ProcessTemplateService {

    @Autowired
    private ProcessTemplateDraftRepository draftRepository;

    @Autowired
    private ProcessTemplatePublishedRepository publishedRepository;

    @Autowired
    private ProcessTemplateSnapshotRepository snapshotRepository;

    @Autowired
    private ProcessCategoryRepository categoryRepository;

    @Autowired
    private RepositoryService flowableRepositoryService;

    /**
     * 创建设计态模板
     *
     * @param request 创建请求
     * @param createdBy 创建人
     * @return 设计态模板VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessTemplateDraftVO createDraft(CreateDraftTemplateRequest request, String createdBy) {
        log.info("创建设计态模板: templateKey={}, createdBy={}", request.getTemplateKey(), createdBy);

        // 1. 验证模板Key唯一性
        int count = draftRepository.countByTemplateKey(
                request.getTemplateKey(),
                request.getTenantId(),
                request.getAppId(),
                request.getContextId()
        );
        if (count > 0) {
            throw new BusinessException("模板Key已存在: " + request.getTemplateKey());
        }

        // 2. 验证分类是否存在
        ProcessCategory category = categoryRepository.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在: " + request.getCategoryId());
        }

        // 3. 验证BPMN XML格式
        validateBpmnXml(request.getBpmnXml());

        // 4. 创建设计态模板
        ProcessTemplateDraft draft = ProcessTemplateDraft.builder()
                .id(UUID.randomUUID().toString())
                .templateKey(request.getTemplateKey())
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .bpmnXml(request.getBpmnXml())
                .categoryId(request.getCategoryId())
                .categoryName(category.getName())
                .categoryCode(category.getCode())
                .tags(request.getTags() != null ? JsonUtil.toJson(request.getTags()) : null)
                .formConfig(request.getFormConfig() != null ? JsonUtil.toJson(request.getFormConfig()) : null)
                .appId(request.getAppId())
                .contextId(request.getContextId())
                .tenantId(request.getTenantId())
                .status(ProcessTemplateStatus.DRAFT)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .version(1)
                .build();

        draft.markCreated(createdBy);
        draftRepository.save(draft);

        log.info("设计态模板创建成功: id={}, templateKey={}", draft.getId(), draft.getTemplateKey());
        return toDraftVO(draft);
    }

    /**
     * 更新设计态模板
     *
     * @param id 模板ID
     * @param request 更新请求
     * @param updatedBy 更新人
     * @return 设计态模板VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessTemplateDraftVO updateDraft(String id, UpdateDraftTemplateRequest request, String updatedBy) {
        log.info("更新设计态模板: id={}, updatedBy={}", id, updatedBy);

        // 1. 查询设计态模板
        ProcessTemplateDraft draft = draftRepository.findById(id);
        if (draft == null) {
            throw new BusinessException("设计态模板不存在: " + id);
        }

        // 2. 如果修改了分类，验证分类是否存在
        if (request.getCategoryId() != null && !request.getCategoryId().equals(draft.getCategoryId())) {
            ProcessCategory category = categoryRepository.findById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException("分类不存在: " + request.getCategoryId());
            }
            draft.setCategoryId(request.getCategoryId());
            draft.setCategoryName(category.getName());
            draft.setCategoryCode(category.getCode());
        }

        // 3. 验证BPMN XML格式
        validateBpmnXml(request.getBpmnXml());

        // 4. 更新设计态模板
        draft.setTemplateName(request.getTemplateName());
        draft.setDescription(request.getDescription());
        draft.setBpmnXml(request.getBpmnXml());
        if (request.getTags() != null) {
            draft.setTagList(request.getTags());
        }
        if (request.getFormConfig() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> formConfigMap = (Map<String, Object>) request.getFormConfig();
            draft.setFormConfigMap(formConfigMap);
        }
        draft.markUpdated(updatedBy);

        int rows = draftRepository.update(draft);
        if (rows == 0) {
            throw new BusinessException("更新设计态模板失败，可能已被其他用户修改");
        }

        log.info("设计态模板更新成功: id={}", id);
        return toDraftVO(draft);
    }

    /**
     * 查询设计态模板
     *
     * @param id 模板ID
     * @return 设计态模板VO
     */
    public ProcessTemplateDraftVO getDraft(String id) {
        ProcessTemplateDraft draft = draftRepository.findById(id);
        if (draft == null) {
            throw new BusinessException("设计态模板不存在: " + id);
        }
        return toDraftVO(draft);
    }

    /**
     * 删除设计态模板
     *
     * @param id 模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(String id) {
        log.info("删除设计态模板: id={}", id);

        ProcessTemplateDraft draft = draftRepository.findById(id);
        if (draft == null) {
            throw new BusinessException("设计态模板不存在: " + id);
        }

        draftRepository.delete(id);
        log.info("设计态模板删除成功: id={}", id);
    }

    /**
     * 查询设计态模板列表
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 设计态模板列表
     */
    public List<ProcessTemplateDraftVO> listDraftTemplates(
            String tenantId, String appId, String contextId,
            String categoryId, String keyword, Integer offset, Integer limit) {
        List<ProcessTemplateDraft> list = draftRepository.findByPage(
                tenantId, appId, contextId, categoryId, keyword, offset, limit
        );
        return list.stream()
                .map(this::toDraftVO)
                .collect(Collectors.toList());
    }

    /**
     * 发布模板（设计态 → 发布态）
     *
     * @param id 设计态模板ID
     * @param publishedBy 发布人
     * @return 发布态模板VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessTemplatePublishedVO publishTemplate(String id, String publishedBy) {
        log.info("发布模板: draftId={}, publishedBy={}", id, publishedBy);

        // 1. 查询设计态模板
        ProcessTemplateDraft draft = draftRepository.findById(id);
        if (draft == null) {
            throw new BusinessException("设计态模板不存在: " + id);
        }

        // 2. 部署到Flowable（需要先部署才能获取实际的流程 key）
        String flowableProcessDefinitionId;
        String flowableDeploymentId;
        String actualProcessKey;  // 从 Flowable 获取的实际流程定义 key
        try {
            Deployment deployment = flowableRepositoryService.createDeployment()
                    .name(draft.getTemplateName())
                    .addBytes(draft.getTemplateKey() + ".bpmn20.xml",
                            draft.getBpmnXml().getBytes(StandardCharsets.UTF_8))
                    .deploy();

            flowableDeploymentId = deployment.getId();

            // 查询流程定义
            ProcessDefinition processDefinition = flowableRepositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();

            if (processDefinition == null) {
                throw new BusinessException("Flowable部署失败：未找到流程定义");
            }

            flowableProcessDefinitionId = processDefinition.getId();
            // 获取实际的流程定义 key（来自 BPMN XML 中的 process id）
            actualProcessKey = processDefinition.getKey();

            log.info("Flowable部署成功: deploymentId={}, processDefinitionId={}, processKey={}",
                    flowableDeploymentId, flowableProcessDefinitionId, actualProcessKey);
        } catch (Exception e) {
            throw new BusinessException("Flowable部署失败: " + e.getMessage());
        }

        // 3. 获取下一个版本号（使用实际的流程 key）
        int nextVersion = publishedRepository.getNextTemplateVersion(
                actualProcessKey,  // 使用实际流程 key 查询版本号
                draft.getTenantId(),
                draft.getAppId(),
                draft.getContextId()
        );

        // 4. 创建发布态模板
        // 注意：使用从 Flowable 获取的实际流程定义 key，而不是 draft.getTemplateKey()
        // 这样可以确保 process_template_published.template_key 与 act_re_procdef.key_ 一致
        ProcessTemplatePublished published = ProcessTemplatePublished.builder()
                .id(UUID.randomUUID().toString())
                .templateKey(actualProcessKey)  // 使用 Flowable 返回的实际 key
                .templateName(draft.getTemplateName())
                .description(draft.getDescription())
                .bpmnXml(draft.getBpmnXml())
                .flowableProcessDefinitionId(flowableProcessDefinitionId)
                .flowableDeploymentId(flowableDeploymentId)
                .flowableVersion(nextVersion)
                .categoryId(draft.getCategoryId())
                .categoryName(draft.getCategoryName())
                .categoryCode(draft.getCategoryCode())
                .tags(draft.getTags())
                .formConfig(draft.getFormConfig())
                .appId(draft.getAppId())
                .contextId(draft.getContextId())
                .tenantId(draft.getTenantId())
                .status(ProcessTemplateStatus.ACTIVE)
                .instanceCount(0)
                .runningInstanceCount(0)
                .createdBy(publishedBy)
                .version(1)
                .build();

        published.markPublished(publishedBy);
        publishedRepository.save(published);

        // 5. 删除设计态模板
        draftRepository.delete(id);

        log.info("模板发布成功: draftId={}, publishedId={}, flowableVersion={}",
                id, published.getId(), nextVersion);
        return toPublishedVO(published);
    }

    /**
     * 停用模板（激活态 → 停用态）
     *
     * @param id 发布态模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void suspendTemplate(String id) {
        log.info("停用模板: publishedId={}", id);

        ProcessTemplatePublished published = publishedRepository.findById(id);
        if (published == null) {
            throw new BusinessException("发布态模板不存在: " + id);
        }

        if (!published.isActive()) {
            throw new BusinessException("模板不是激活状态，无法停用");
        }

        // 检查是否有运行中的流程实例
        if (published.hasRunningInstances()) {
            throw new BusinessException("模板有运行中的流程实例，无法停用");
        }

        // 调用Flowable suspend
        flowableRepositoryService.suspendProcessDefinitionById(
                published.getFlowableProcessDefinitionId()
        );

        // 更新状态
        publishedRepository.updateStatus(id, ProcessTemplateStatus.INACTIVE);
        publishedRepository.updateSuspendedTime(id, java.time.LocalDateTime.now());

        log.info("模板停用成功: publishedId={}", id);
    }

    /**
     * 激活模板（停用态 → 激活态）
     *
     * @param id 发布态模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateTemplate(String id) {
        log.info("激活模板: publishedId={}", id);

        ProcessTemplatePublished published = publishedRepository.findById(id);
        if (published == null) {
            throw new BusinessException("发布态模板不存在: " + id);
        }

        if (!published.isInactive()) {
            throw new BusinessException("模板不是停用状态，无法激活");
        }

        // 调用Flowable activate
        flowableRepositoryService.activateProcessDefinitionById(
                published.getFlowableProcessDefinitionId()
        );

        // 更新状态
        publishedRepository.updateStatus(id, ProcessTemplateStatus.ACTIVE);
        publishedRepository.updateSuspendedTime(id, null);

        log.info("模板激活成功: publishedId={}", id);
    }

    /**
     * 查询发布态模板
     *
     * @param id 模板ID
     * @return 发布态模板VO
     */
    public ProcessTemplatePublishedVO getPublished(String id) {
        ProcessTemplatePublished published = publishedRepository.findById(id);
        if (published == null) {
            throw new BusinessException("发布态模板不存在: " + id);
        }
        return toPublishedVO(published);
    }

    /**
     * 查询发布态模板列表
     *
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 发布态模板列表
     */
    public List<ProcessTemplatePublishedVO> listPublished(
            String tenantId, String appId, String contextId,
            String categoryId, ProcessTemplateStatus status, String keyword,
            Integer offset, Integer limit) {
        List<ProcessTemplatePublished> list = publishedRepository.findByPage(
                tenantId, appId, contextId, categoryId, status, keyword, offset, limit
        );
        return list.stream()
                .map(this::toPublishedVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建快照
     *
     * @param request 创建快照请求
     * @param createdBy 创建人
     * @return 快照VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessTemplateSnapshotVO createSnapshot(CreateTemplateSnapshotRequest request, String createdBy) {
        log.info("创建快照: sourceTemplateId={}, sourceTemplateType={}, snapshotName={}",
                request.getSourceTemplateId(), request.getSourceTemplateType(), request.getSnapshotName());

        ProcessTemplateSnapshot snapshot;
        String templateKey;
        String tenantId, appId, contextId;

        if ("DRAFT".equals(request.getSourceTemplateType())) {
            // 从设计态创建快照
            ProcessTemplateDraft draft = draftRepository.findById(request.getSourceTemplateId());
            if (draft == null) {
                throw new BusinessException("设计态模板不存在: " + request.getSourceTemplateId());
            }
            templateKey = draft.getTemplateKey();
            tenantId = draft.getTenantId();
            appId = draft.getAppId();
            contextId = draft.getContextId();

            // 获取下一个快照版本号
            int nextVersion = snapshotRepository.getNextSnapshotVersion(
                    templateKey, tenantId, appId, contextId
            );

            snapshot = ProcessTemplateSnapshot.fromDraft(draft, request.getSnapshotName(), createdBy);
            snapshot.setSnapshotVersion(nextVersion);
        } else if ("PUBLISHED".equals(request.getSourceTemplateType())) {
            // 从发布态创建快照
            ProcessTemplatePublished published = publishedRepository.findById(request.getSourceTemplateId());
            if (published == null) {
                throw new BusinessException("发布态模板不存在: " + request.getSourceTemplateId());
            }
            templateKey = published.getTemplateKey();
            tenantId = published.getTenantId();
            appId = published.getAppId();
            contextId = published.getContextId();

            // 获取下一个快照版本号
            int nextVersion = snapshotRepository.getNextSnapshotVersion(
                    templateKey, tenantId, appId, contextId
            );

            snapshot = ProcessTemplateSnapshot.fromPublished(published, request.getSnapshotName(), createdBy);
            snapshot.setSnapshotVersion(nextVersion);
        } else {
            throw new BusinessException("无效的来源模板类型: " + request.getSourceTemplateType());
        }

        snapshot.initDefaults();
        snapshot.markCreated(createdBy);
        snapshotRepository.save(snapshot);

        log.info("快照创建成功: id={}, snapshotVersion={}", snapshot.getId(), snapshot.getSnapshotVersion());
        return toSnapshotVO(snapshot);
    }

    /**
     * 查询快照列表
     *
     * @param templateKey 模板Key
     * @param tenantId 租户ID
     * @param appId 应用ID
     * @param contextId 上下文ID
     * @return 快照列表
     */
    public List<ProcessTemplateSnapshotVO> listSnapshots(String templateKey, String tenantId,
                                                         String appId, String contextId) {
        List<ProcessTemplateSnapshot> list = snapshotRepository.findByTemplateKey(
                templateKey, tenantId, appId, contextId
        );
        return list.stream()
                .map(this::toSnapshotVO)
                .collect(Collectors.toList());
    }

    /**
     * 从快照恢复到设计态
     *
     * @param request 恢复请求
     * @param restoredBy 恢复人
     * @return 设计态模板VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessTemplateDraftVO restoreFromSnapshot(RestoreSnapshotRequest request, String restoredBy) {
        log.info("从快照恢复: snapshotId={}, restoredBy={}", request.getSnapshotId(), restoredBy);

        // 1. 查询快照
        ProcessTemplateSnapshot snapshot = snapshotRepository.findById(request.getSnapshotId());
        if (snapshot == null) {
            throw new BusinessException("快照不存在: " + request.getSnapshotId());
        }

        // 2. 验证分类是否存在
        ProcessCategory category = null;
        if (snapshot.getCategoryId() != null) {
            category = categoryRepository.findById(snapshot.getCategoryId());
        }

        // 3. 生成新的模板Key（避免冲突）
        String newTemplateKey = snapshot.getTemplateKey() + "_restore_" + System.currentTimeMillis();

        // 4. 创建新的设计态模板
        ProcessTemplateDraft draft = ProcessTemplateDraft.builder()
                .id(UUID.randomUUID().toString())
                .templateKey(newTemplateKey)
                .templateName(request.getNewTemplateName() != null
                        ? request.getNewTemplateName()
                        : snapshot.getSnapshotName())
                .description("从快照恢复: " + snapshot.getSnapshotName())
                .bpmnXml(snapshot.getBpmnXml())
                .categoryId(snapshot.getCategoryId())
                .categoryName(category != null ? category.getName() : snapshot.getCategoryName())
                .categoryCode(category != null ? category.getCode() : snapshot.getCategoryCode())
                .tags(snapshot.getTags())
                .formConfig(snapshot.getFormConfig())
                .appId(snapshot.getAppId())
                .contextId(snapshot.getContextId())
                .tenantId(snapshot.getTenantId())
                .status(ProcessTemplateStatus.DRAFT)
                .createdBy(restoredBy)
                .updatedBy(restoredBy)
                .version(1)
                .build();

        draft.markCreated(restoredBy);
        draftRepository.save(draft);

        log.info("从快照恢复成功: snapshotId={}, newDraftId={}", request.getSnapshotId(), draft.getId());
        return toDraftVO(draft);
    }

    /**
     * 删除快照
     *
     * @param id 快照ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSnapshot(String id) {
        log.info("删除快照: id={}", id);

        ProcessTemplateSnapshot snapshot = snapshotRepository.findById(id);
        if (snapshot == null) {
            throw new BusinessException("快照不存在: " + id);
        }

        snapshotRepository.delete(id);
        log.info("快照删除成功: id={}", id);
    }

    // ============ 私有辅助方法 ============

    /**
     * 验证BPMN XML格式
     *
     * @param bpmnXml BPMN XML字符串
     */
    private void validateBpmnXml(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new BusinessException("BPMN XML内容不能为空");
        }

        try {
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

            // 将 String 转换为 XMLStreamReader
            javax.xml.stream.XMLInputFactory factory = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(
                    new java.io.ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))
            );

            BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(reader);

            if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
                throw new BusinessException("BPMN XML格式错误：无法解析流程定义");
            }

            if (bpmnModel.getMainProcess().getId() == null || bpmnModel.getMainProcess().getId().isEmpty()) {
                throw new BusinessException("BPMN XML格式错误：流程ID不能为空");
            }

        } catch (Exception e) {
            throw new BusinessException("BPMN XML格式错误: " + e.getMessage());
        }
    }

    /**
     * 转换为设计态模板VO
     */
    private ProcessTemplateDraftVO toDraftVO(ProcessTemplateDraft draft) {
        return ProcessTemplateDraftVO.builder()
                .id(draft.getId())
                .templateKey(draft.getTemplateKey())
                .templateName(draft.getTemplateName())
                .description(draft.getDescription())
                .bpmnXml(draft.getBpmnXml())
                .categoryId(draft.getCategoryId())
                .categoryName(draft.getCategoryName())
                .categoryCode(draft.getCategoryCode())
                .tags(draft.getTagList())
                .formConfig(draft.getFormConfigMap())
                .appId(draft.getAppId())
                .contextId(draft.getContextId())
                .tenantId(draft.getTenantId())
                .status(draft.getStatus())
                .createdTime(draft.getCreatedTime())
                .updatedTime(draft.getUpdatedTime())
                .createdBy(draft.getCreatedBy())
                .updatedBy(draft.getUpdatedBy())
                .version(draft.getVersion())
                .build();
    }

    /**
     * 转换为发布态模板VO
     */
    private ProcessTemplatePublishedVO toPublishedVO(ProcessTemplatePublished published) {
        return ProcessTemplatePublishedVO.builder()
                .id(published.getId())
                .templateKey(published.getTemplateKey())
                .templateName(published.getTemplateName())
                .description(published.getDescription())
                .bpmnXml(published.getBpmnXml())
                .flowableProcessDefinitionId(published.getFlowableProcessDefinitionId())
                .flowableDeploymentId(published.getFlowableDeploymentId())
                .flowableVersion(published.getFlowableVersion())
                .categoryId(published.getCategoryId())
                .categoryName(published.getCategoryName())
                .categoryCode(published.getCategoryCode())
                .tags(published.getTagList())
                .formConfig(published.getFormConfigMap())
                .appId(published.getAppId())
                .contextId(published.getContextId())
                .tenantId(published.getTenantId())
                .status(published.getStatus())
                .instanceCount(published.getInstanceCount())
                .runningInstanceCount(published.getRunningInstanceCount())
                .publishedTime(published.getPublishedTime())
                .suspendedTime(published.getSuspendedTime())
                .createdBy(published.getCreatedBy())
                .version(published.getVersion())
                .build();
    }

    /**
     * 转换为快照VO
     */
    private ProcessTemplateSnapshotVO toSnapshotVO(ProcessTemplateSnapshot snapshot) {
        return ProcessTemplateSnapshotVO.builder()
                .id(snapshot.getId())
                .templateKey(snapshot.getTemplateKey())
                .snapshotName(snapshot.getSnapshotName())
                .bpmnXml(snapshot.getBpmnXml())
                .sourceTemplateId(snapshot.getSourceTemplateId())
                .sourceTemplateStatus(snapshot.getSourceTemplateStatus())
                .sourceTemplateVersion(snapshot.getSourceTemplateVersion())
                .categoryId(snapshot.getCategoryId())
                .categoryName(snapshot.getCategoryName())
                .categoryCode(snapshot.getCategoryCode())
                .tags(snapshot.getTagList())
                .formConfig(snapshot.getFormConfigMap())
                .appId(snapshot.getAppId())
                .contextId(snapshot.getContextId())
                .tenantId(snapshot.getTenantId())
                .snapshotVersion(snapshot.getSnapshotVersion())
                .createdTime(snapshot.getCreatedTime())
                .createdBy(snapshot.getCreatedBy())
                .build();
    }
}
