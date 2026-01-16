package com.lingflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 通知记录实体类
 * 用于存储流程相关的通知记录
 */
@Data
@TableName("lf_notification_record")
public class NotificationRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知UUID
     */
    @TableField("notification_id")
    private String notificationId;

    /**
     * 通知类型
     * TASK_ASSIGNED, TASK_COMPLETED, TASK_DELEGATED,
     * PROCESS_TIMEOUT, PROCESS_APPROVED, PROCESS_REJECTED,
     * PROCESS_WITHDRAWN, CUSTOM
     */
    @TableField("type")
    private String type;

    /**
     * 接收人ID
     */
    @TableField("recipient_id")
    private String recipientId;

    /**
     * 接收人名称（冗余字段，便于查询显示）
     */
    @TableField("recipient_name")
    private String recipientName;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

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
     * 流程定义Key
     */
    @TableField("process_definition_key")
    private String processDefinitionKey;

    /**
     * 流程名称（冗余字段）
     */
    @TableField("process_name")
    private String processName;

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
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead = false;

    /**
     * 读取时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

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
     * 扩展数据（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 构造函数
     */
    public NotificationRecord() {
        this.notificationId = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 构造函数
     */
    public NotificationRecord(String type, String recipientId, String title, String content) {
        this();
        this.type = type;
        this.recipientId = recipientId;
        this.title = title;
        this.content = content;
    }
}
