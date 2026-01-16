package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.service.ProcessCommentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程评论控制器
 * 提供流程评论相关的API
 */
@RestController
@RequestMapping("/api/comments")
public class ProcessCommentController {

    @Autowired
    private ProcessCommentService commentService;

    /**
     * 添加流程实例评论
     *
     * @param request 请求参数
     * @return 评论ID
     */
    @PostMapping("/process")
    public Result<String> addProcessComment(@RequestBody AddCommentRequest request) {
        try {
            String commentId = commentService.addProcessComment(
                request.getProcessInstanceId(),
                request.getUserId(),
                request.getContent()
            );
            return Result.success(commentId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加任务评论
     *
     * @param request 请求参数
     * @return 评论ID
     */
    @PostMapping("/task")
    public Result<String> addTaskComment(@RequestBody AddCommentRequest request) {
        try {
            String commentId = commentService.addTaskComment(
                request.getTaskId(),
                request.getUserId(),
                request.getContent()
            );
            return Result.success(commentId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加系统评论
     *
     * @param request 请求参数
     * @return 评论ID
     */
    @PostMapping("/system")
    public Result<String> addSystemComment(@RequestBody AddSystemCommentRequest request) {
        try {
            String commentId = commentService.addSystemComment(
                request.getProcessInstanceId(),
                request.getContent()
            );
            return Result.success(commentId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程实例的所有评论
     *
     * @param processInstanceId 流程实例ID
     * @return 评论列表
     */
    @GetMapping("/process/{processInstanceId}")
    public Result<List<ProcessCommentService.Comment>> getProcessComments(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            List<ProcessCommentService.Comment> comments = commentService.getProcessComments(processInstanceId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取任务的所有评论
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    @GetMapping("/task/{taskId}")
    public Result<List<ProcessCommentService.Comment>> getTaskComments(
            @PathVariable("taskId") String taskId) {
        try {
            List<ProcessCommentService.Comment> comments = commentService.getTaskComments(taskId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程实例的所有评论（包括任务评论）
     *
     * @param processInstanceId 流程实例ID
     * @return 评论列表
     */
    @GetMapping("/process/{processInstanceId}/all")
    public Result<List<ProcessCommentService.Comment>> getAllCommentsForProcess(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            List<ProcessCommentService.Comment> comments = commentService.getAllCommentsForProcess(processInstanceId);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 是否成功
     */
    @DeleteMapping("/{commentId}")
    public Result<Boolean> deleteComment(
            @PathVariable("commentId") String commentId,
            @RequestParam("userId") String userId) {
        try {
            boolean success = commentService.deleteComment(commentId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新评论
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @PutMapping
    public Result<Boolean> updateComment(@RequestBody UpdateCommentRequest request) {
        try {
            boolean success = commentService.updateComment(
                request.getCommentId(),
                request.getUserId(),
                request.getNewContent()
            );
            return Result.success(success);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取评论统计
     *
     * @param processInstanceId 流程实例ID
     * @return 统计信息
     */
    @GetMapping("/process/{processInstanceId}/statistics")
    public Result<ProcessCommentService.CommentStatistics> getCommentStatistics(
            @PathVariable("processInstanceId") String processInstanceId) {
        try {
            ProcessCommentService.CommentStatistics statistics =
                commentService.getCommentStatistics(processInstanceId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== VO 类定义 ====================

    /**
     * 添加评论请求参数
     */
    @Data
    public static class AddCommentRequest {
        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 评论内容
         */
        private String content;
    }

    /**
     * 添加系统评论请求参数
     */
    @Data
    public static class AddSystemCommentRequest {
        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 评论内容
         */
        private String content;
    }

    /**
     * 更新评论请求参数
     */
    @Data
    public static class UpdateCommentRequest {
        /**
         * 评论ID
         */
        private String commentId;

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 新内容
         */
        private String newContent;
    }
}
