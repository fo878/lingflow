package com.lingflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingflow.entity.ProcessComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程评论Mapper接口
 */
@Mapper
public interface ProcessCommentMapper extends BaseMapper<ProcessComment> {

    /**
     * 根据commentId查找评论
     * @param commentId 评论UUID
     * @return 评论记录
     */
    default ProcessComment findByCommentId(String commentId) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getCommentId, commentId)
        );
    }

    /**
     * 查询流程实例的所有评论（包括流程级、任务级、系统评论）
     * @param processInstanceId 流程实例ID
     * @return 评论记录列表
     */
    default List<ProcessComment> findByProcessInstanceId(String processInstanceId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getProcessInstanceId, processInstanceId)
                .orderByAsc(ProcessComment::getCreateTime)
        );
    }

    /**
     * 查询流程实例的未删除评论
     * @param processInstanceId 流程实例ID
     * @return 评论记录列表
     */
    default List<ProcessComment> findByProcessInstanceIdAndIsDeleted(String processInstanceId, Boolean isDeleted) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getProcessInstanceId, processInstanceId)
                .eq(ProcessComment::getIsDeleted, isDeleted)
                .orderByAsc(ProcessComment::getCreateTime)
        );
    }

    /**
     * 查询任务的所有评论
     * @param taskId 任务ID
     * @return 评论记录列表
     */
    default List<ProcessComment> findByTaskId(String taskId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getTaskId, taskId)
                .eq(ProcessComment::getIsDeleted, false)
                .orderByAsc(ProcessComment::getCreateTime)
        );
    }

    /**
     * 查询任务的所有评论（带删除标记）
     * @param taskId 任务ID
     * @param isDeleted 是否删除
     * @return 评论记录列表
     */
    default List<ProcessComment> findByTaskIdAndIsDeletedOrderByCreateTimeAsc(String taskId, Boolean isDeleted) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getTaskId, taskId)
                .eq(ProcessComment::getIsDeleted, isDeleted)
                .orderByAsc(ProcessComment::getCreateTime)
        );
    }

    /**
     * 查询流程实例的所有评论（包括已删除）
     * @param processInstanceId 流程实例ID
     * @return 评论记录列表
     */
    @Select("SELECT * FROM lf_process_comment WHERE process_instance_id = #{processInstanceId} ORDER BY create_time ASC")
    List<ProcessComment> findAllByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 统计流程实例的评论数量（按类型）
     * @param processInstanceId 流程实例ID
     * @param type 评论类型
     * @return 评论数量
     */
    default Long countByProcessInstanceIdAndType(String processInstanceId, String type) {
        return selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getProcessInstanceId, processInstanceId)
                .eq(ProcessComment::getType, type)
                .eq(ProcessComment::getIsDeleted, false)
        );
    }

    /**
     * 统计流程实例的所有未删除评论数量
     * @param processInstanceId 流程实例ID
     * @param type 评论类型
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    default Long countByProcessInstanceIdAndTypeAndIsDeleted(String processInstanceId, String type, Boolean isDeleted) {
        return selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getProcessInstanceId, processInstanceId)
                .eq(ProcessComment::getType, type)
                .eq(ProcessComment::getIsDeleted, isDeleted)
        );
    }

    /**
     * 更新评论内容
     * @param commentId 评论UUID
     * @param newContent 新内容
     * @param updateTime 更新时间
     * @return 更新行数
     */
    @Update("UPDATE lf_process_comment SET content = #{newContent}, update_time = #{updateTime} WHERE comment_id = #{commentId}")
    int updateContent(@Param("commentId") String commentId, @Param("newContent") String newContent, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 逻辑删除评论
     * @param commentId 评论UUID
     * @param deleteTime 删除时间
     * @return 更新行数
     */
    @Update("UPDATE lf_process_comment SET is_deleted = true, delete_time = #{deleteTime}, update_time = #{deleteTime} WHERE comment_id = #{commentId}")
    int logicalDelete(@Param("commentId") String commentId, @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 统计参与评论的不同用户数量
     * @param processInstanceId 流程实例ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM lf_process_comment WHERE process_instance_id = #{processInstanceId} AND user_id IS NOT NULL AND is_deleted = false")
    Long countParticipantUsers(@Param("processInstanceId") String processInstanceId);

    /**
     * 统计任务的评论数量
     * @param taskId 任务ID
     * @param isDeleted 是否删除
     * @return 评论数量
     */
    default Long countByTaskIdAndIsDeleted(String taskId, Boolean isDeleted) {
        return selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getTaskId, taskId)
                .eq(ProcessComment::getIsDeleted, isDeleted)
        );
    }

    /**
     * 查询流程实例的所有任务ID
     * @param processInstanceId 流程实例ID
     * @return 任务ID列表
     */
    @Select("SELECT DISTINCT task_id FROM lf_process_comment WHERE process_instance_id = #{processInstanceId} AND task_id IS NOT NULL AND is_deleted = false")
    List<String> findTaskIdsByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 查询用户的所有评论
     * @param userId 用户ID
     * @return 评论记录列表
     */
    default List<ProcessComment> findByUserId(String userId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProcessComment>()
                .eq(ProcessComment::getUserId, userId)
                .eq(ProcessComment::getIsDeleted, false)
                .orderByDesc(ProcessComment::getCreateTime)
        );
    }
}
