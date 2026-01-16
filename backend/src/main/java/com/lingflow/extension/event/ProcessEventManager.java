package com.lingflow.extension.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 流程事件管理器
 * 负责触发和管理流程事件
 */
@Slf4j
@Component
public class ProcessEventManager {

    @Autowired(required = false)
    private List<ProcessEventListener> listeners;

    /**
     * 触发事件
     *
     * @param event 流程事件
     */
    public void triggerEvent(ProcessEvent event) {
        if (listeners == null || listeners.isEmpty()) {
            log.debug("No event listeners registered, skipping event: {}", event.getEventType());
            return;
        }

        // 按优先级排序
        listeners.sort(Comparator.comparingInt(ProcessEventListener::getOrder));

        // 1. 事件触发前
        for (ProcessEventListener listener : listeners) {
            if (listener.supports(event.getEventType())) {
                try {
                    listener.onBefore(event);
                } catch (Exception e) {
                    log.error("Error in onBefore for event: {}", event.getEventType(), e);
                }
            }
        }

        try {
            // 2. 执行事件业务逻辑（如有）
            doExecute(event);

            // 3. 事件触发后
            for (ProcessEventListener listener : listeners) {
                if (listener.supports(event.getEventType())) {
                    try {
                        listener.onAfter(event);
                    } catch (Exception e) {
                        log.error("Error in onAfter for event: {}", event.getEventType(), e);
                    }
                }
            }

        } catch (Exception e) {
            // 异常处理
            handleEventException(event, e);
            throw e;
        }
    }

    /**
     * 执行事件具体逻辑
     *
     * @param event 流程事件
     */
    private void doExecute(ProcessEvent event) {
        // 事件具体执行逻辑
        // 这里可以添加默认的事件处理逻辑
    }

    /**
     * 处理事件异常
     *
     * @param event 流程事件
     * @param e 异常
     */
    private void handleEventException(ProcessEvent event, Exception e) {
        log.error("Exception in event processing: {}", event.getEventType(), e);
    }
}
