package com.lingflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 流程评论实体类
 * 用于存储流程实例和任务的评论
 */
@Data
@TableName("lf_process_comment")
public class ProcessComment {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论UUID
     */
    @TableField("comment_id")
    private String commentId;

    /**
     * 评论类型
     * PROCESS - 流程评论
     * TASK - 任务评论
     * SYSTEM - 系统评论
     */
    @TableField("type")
    private String type;

    /**
     * 流程实例ID
     */
    @TableField("process_instance_id")
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id")
    private String processDefinitionId;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private String taskId;

    /**
     * 任务名称（冗余字段）
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户名称（冗余字段，便于查询显示）
     */
    @TableField("user_name")
    private String userName;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID（用于回复功能）
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 是否已删除（逻辑删除）
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean isDeleted = false;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除时间
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 构造函数
     */
    public ProcessComment() {
        this.commentId = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 构造函数
     */
    public ProcessComment(String type, String content) {
        this();
        this.type = type;
        this.content = content;
    }
}
