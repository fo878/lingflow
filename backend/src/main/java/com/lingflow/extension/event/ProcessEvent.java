package com.lingflow.extension.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程事件
 * 表示流程生命周期中的各种事件
 */
@Data
@Builder
@AllArgsConstructor
public class ProcessEvent {

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 任务ID（如果适用）
     */
    private String taskId;

    /**
     * 活动ID（如果适用）
     */
    private String activityId;

    /**
     * 事件数据
     */
    private Map<String, Object> data;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 获取数据
     *
     * @param key 键
     * @param type 类型
     * @param <T> 泛型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        if (data == null) {
            return null;
        }
        Object value = data.get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 获取数据（不带类型参数）
     *
     * @param key 键
     * @return 值
     */
    public Object getData(String key) {
        return data != null ? data.get(key) : null;
    }
}
