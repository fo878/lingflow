package com.lingflow.service;

import com.lingflow.entity.ProcessComment;
import com.lingflow.mapper.ProcessCommentMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程评论服务
 * 管理流程实例和任务的评论
 */
@Slf4j
@Service
public class ProcessCommentService {

    @Autowired
    private ExtendedTaskService extendedTaskService;

    @Autowired
    private ProcessCommentMapper processCommentMapper;

    /**
     * 添加流程实例评论
     *
     * @param processInstanceId 流程实例ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 评论ID
     */
    @Transactional
    public String addProcessComment(
        String processInstanceId,
        String userId,
        String content
    ) {
        log.info("添加流程评论 - 流程实例ID: {}, 用户: {}, 内容: {}",
            processInstanceId, userId, content);

        ProcessComment comment = new ProcessComment(CommentType.PROCESS.name(), content);
        comment.setProcessInstanceId(processInstanceId);
        comment.setUserId(userId);

        processCommentMapper.insert(comment);
        return comment.getCommentId();
    }

    /**
     * 添加任务评论
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 评论ID
     */
    @Transactional
    public String addTaskComment(
        String taskId,
        String userId,
        String content
    ) {
        log.info("添加任务评论 - 任务ID: {}, 用户: {}, 内容: {}",
            taskId, userId, content);

        ProcessComment comment = new ProcessComment(CommentType.TASK.name(), content);
        comment.setTaskId(taskId);
        comment.setUserId(userId);

        processCommentMapper.insert(comment);
        return comment.getCommentId();
    }

    /**
     * 添加系统评论
     *
     * @param processInstanceId 流程实例ID
     * @param content 评论内容
     * @return 评论ID
     */
    @Transactional
    public String addSystemComment(
        String processInstanceId,
        String content
    ) {
        log.info("添加系统评论 - 流程实例ID: {}, 内容: {}",
            processInstanceId, content);

        ProcessComment comment = new ProcessComment(CommentType.SYSTEM.name(), content);
        comment.setProcessInstanceId(processInstanceId);
        comment.setUserId("SYSTEM");

        processCommentMapper.insert(comment);
        return comment.getCommentId();
    }

    /**
     * 获取流程实例的所有评论（不包括已删除）
     *
     * @param processInstanceId 流程实例ID
     * @return 评论列表
     */
    public List<Comment> getProcessComments(String processInstanceId) {
        log.info("获取流程实例评论 - 流程实例ID: {}", processInstanceId);

        List<ProcessComment> comments =
            processCommentMapper.findByProcessInstanceIdAndIsDeleted(
                processInstanceId, false);

        return convertToCommentList(comments);
    }

    /**
     * 获取任务的所有评论（不包括已删除）
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    public List<Comment> getTaskComments(String taskId) {
        log.info("获取任务评论 - 任务ID: {}", taskId);

        List<ProcessComment> comments =
            processCommentMapper.findByTaskIdAndIsDeletedOrderByCreateTimeAsc(taskId, false);

        return convertToCommentList(comments);
    }

    /**
     * 获取流程实例的所有评论（包括任务评论，不包括已删除）
     *
     * @param processInstanceId 流程实例ID
     * @return 评论列表
     */
    public List<Comment> getAllCommentsForProcess(String processInstanceId) {
        log.info("获取流程实例的所有评论 - 流程实例ID: {}", processInstanceId);

        List<ProcessComment> comments =
            processCommentMapper.findAllByProcessInstanceId(processInstanceId);

        // 过滤已删除的评论
        return convertToCommentList(
            comments.stream()
                .filter(c -> !c.getIsDeleted())
                .collect(Collectors.toList())
        );
    }

    /**
     * 删除评论（逻辑删除）
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteComment(String commentId, String userId) {
        log.info("删除评论 - 评论ID: {}, 用户: {}", commentId, userId);

        try {
            // 查询评论
            ProcessComment comment = processCommentMapper.findByCommentId(commentId);
            if (comment == null) {
                log.warn("评论不存在: {}", commentId);
                return false;
            }

            // 检查权限：只有评论创建者或管理员可以删除
            if (!comment.getUserId().equals(userId) && !isAdmin(userId)) {
                log.warn("用户 {} 无权删除评论 {}", userId, commentId);
                return false;
            }

            // 逻辑删除
            int updated = processCommentMapper.logicalDelete(commentId, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return false;
        }
    }

    /**
     * 更新评论
     *
     * @param commentId  评论ID
     * @param userId     用户ID
     * @param newContent 新内容
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateComment(
        String commentId,
        String userId,
        String newContent
    ) {
        log.info("更新评论 - 评论ID: {}, 用户: {}, 新内容: {}",
            commentId, userId, newContent);

        try {
            // 查询评论
            ProcessComment comment = processCommentMapper.findByCommentId(commentId);
            if (comment == null) {
                log.warn("评论不存在: {}", commentId);
                return false;
            }

            // 检查权限：只有评论创建者可以修改
            if (!comment.getUserId().equals(userId)) {
                log.warn("用户 {} 无权修改评论 {}", userId, commentId);
                return false;
            }

            // 更新评论
            int updated = processCommentMapper.updateContent(commentId, newContent, LocalDateTime.now());
            return updated > 0;
        } catch (Exception e) {
            log.error("更新评论失败", e);
            return false;
        }
    }

    /**
     * 获取评论统计
     *
     * @param processInstanceId 流程实例ID
     * @return 统计信息
     */
    public CommentStatistics getCommentStatistics(String processInstanceId) {
        log.info("获取评论统计 - 流程实例ID: {}", processInstanceId);

        CommentStatistics stats = new CommentStatistics();
        stats.setProcessInstanceId(processInstanceId);

        // 统计流程评论数
        Long processCommentCount = processCommentMapper.countByProcessInstanceIdAndTypeAndIsDeleted(
            processInstanceId, CommentType.PROCESS.name(), false);
        stats.setProcessCommentCount(processCommentCount != null ? processCommentCount.intValue() : 0);

        // 统计任务评论数
        List<String> taskIds = processCommentMapper.findTaskIdsByProcessInstanceId(processInstanceId);
        int taskCommentCount = 0;
        for (String taskId : taskIds) {
            Long count = processCommentMapper.countByTaskIdAndIsDeleted(taskId, false);
            taskCommentCount += count != null ? count.intValue() : 0;
        }
        stats.setTaskCommentCount(taskCommentCount);
        stats.setTotalCommentCount(stats.getProcessCommentCount() + taskCommentCount);

        // 统计参与评论的用户数
        Long participantCount = processCommentMapper.countParticipantUsers(processInstanceId);
        stats.setParticipantCount(participantCount != null ? participantCount.intValue() : 0);

        return stats;
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(String userId) {
        // 这里可以集成实际的用户权限系统
        return "admin".equals(userId);
    }

    /**
     * 转换为Comment列表
     */
    private List<Comment> convertToCommentList(List<ProcessComment> entities) {
        List<Comment> comments = new ArrayList<>();
        for (ProcessComment entity : entities) {
            Comment comment = new Comment();
            comment.setId(entity.getCommentId());
            comment.setType(CommentType.valueOf(entity.getType()));
            comment.setProcessInstanceId(entity.getProcessInstanceId());
            comment.setTaskId(entity.getTaskId());
            comment.setUserId(entity.getUserId());
            comment.setContent(entity.getContent());
            comment.setCreateTime(entity.getCreateTime());
            comment.setUpdateTime(entity.getUpdateTime());
            comments.add(comment);
        }
        return comments;
    }

    /**
     * 评论
     */
    @Data
    public static class Comment {
        private String id;
        private CommentType type;
        private String processInstanceId;
        private String taskId;
        private String userId;
        private String content;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    /**
     * 评论统计
     */
    @Data
    public static class CommentStatistics {
        private String processInstanceId;
        private Integer processCommentCount;
        private Integer taskCommentCount;
        private Integer totalCommentCount;
        private Integer participantCount;
    }

    /**
     * 评论类型
     */
    public enum CommentType {
        PROCESS,   // 流程评论
        TASK,      // 任务评论
        SYSTEM     // 系统评论
    }
}
