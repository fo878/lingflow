package com.lingflow.extension.event;

/**
 * 流程事件监听器接口
 * 用于监听和处理流程生命周期中的各种事件
 */
public interface ProcessEventListener {

    /**
     * 事件触发前
     *
     * @param event 流程事件
     */
    default void onBefore(ProcessEvent event) {
        // 默认不做处理
    }

    /**
     * 事件触发后
     *
     * @param event 流程事件
     */
    default void onAfter(ProcessEvent event) {
        // 默认不做处理
    }

    /**
     * 判断是否支持该事件
     *
     * @param eventType 事件类型
     * @return 是否支持
     */
    boolean supports(String eventType);

    /**
     * 获取监听器优先级（数字越小优先级越高）
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
