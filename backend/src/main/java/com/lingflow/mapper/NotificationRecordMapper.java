package com.lingflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingflow.entity.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录Mapper接口
 */
@Mapper
public interface NotificationRecordMapper extends BaseMapper<NotificationRecord> {

    /**
     * 根据notificationId查找通知记录
     * @param notificationId 通知UUID
     * @return 通知记录
     */
    default NotificationRecord findByNotificationId(String notificationId) {
        return selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getNotificationId, notificationId)
        );
    }

    /**
     * 查询用户的通知列表（限制数量）
     * @param recipientId 接收人ID
     * @param limit 限制数量
     * @return 通知记录列表
     */
    default List<NotificationRecord> findByRecipientIdWithLimit(String recipientId, int limit) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getRecipientId, recipientId)
                .orderByDesc(NotificationRecord::getCreateTime)
                .last("LIMIT " + limit)
        );
    }

    /**
     * 统计用户未读通知数量
     * @param recipientId 接收人ID
     * @param isRead 是否已读
     * @return 未读通知数量
     */
    default Long countByRecipientIdAndIsRead(String recipientId, Boolean isRead) {
        return selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getRecipientId, recipientId)
                .eq(NotificationRecord::getIsRead, isRead)
        );
    }

    /**
     * 标记通知为已读
     * @param notificationId 通知UUID
     * @param readTime 读取时间
     * @return 更新行数
     */
    @Update("UPDATE lf_notification_record SET is_read = true, read_time = #{readTime}, update_time = #{readTime} WHERE notification_id = #{notificationId}")
    int markAsRead(@Param("notificationId") String notificationId, @Param("readTime") LocalDateTime readTime);

    /**
     * 批量标记用户所有通知为已读
     * @param recipientId 接收人ID
     * @param readTime 读取时间
     * @return 更新行数
     */
    @Update("UPDATE lf_notification_record SET is_read = true, read_time = #{readTime}, update_time = #{readTime} WHERE recipient_id = #{recipientId} AND is_read = false")
    int markAllAsReadForUser(@Param("recipientId") String recipientId, @Param("readTime") LocalDateTime readTime);

    /**
     * 根据流程实例ID查询通知列表
     * @param processInstanceId 流程实例ID
     * @return 通知记录列表
     */
    default List<NotificationRecord> findByProcessInstanceId(String processInstanceId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getProcessInstanceId, processInstanceId)
                .orderByDesc(NotificationRecord::getCreateTime)
        );
    }

    /**
     * 根据任务ID查询通知列表
     * @param taskId 任务ID
     * @return 通知记录列表
     */
    default List<NotificationRecord> findByTaskId(String taskId) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getTaskId, taskId)
                .orderByDesc(NotificationRecord::getCreateTime)
        );
    }

    /**
     * 根据类型查询通知列表
     * @param type 通知类型
     * @param limit 限制数量
     * @return 通知记录列表
     */
    default List<NotificationRecord> findByTypeWithLimit(String type, int limit) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NotificationRecord>()
                .eq(NotificationRecord::getType, type)
                .orderByDesc(NotificationRecord::getCreateTime)
                .last("LIMIT " + limit)
        );
    }
}
