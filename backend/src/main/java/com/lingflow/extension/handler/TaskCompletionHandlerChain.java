package com.lingflow.extension.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 任务提交处理器链
 * 负责管理和执行任务提交处理器
 */
@Slf4j
@Component
public class TaskCompletionHandlerChain {

    @Autowired(required = false)
    private List<TaskCompletionHandler> handlers;

    /**
     * 执行提交处理
     *
     * @param context 提交上下文
     */
    public void execute(TaskCompletionContext context) {
        if (handlers == null || handlers.isEmpty()) {
            log.warn("No task completion handlers registered");
            return;
        }

        // 1. 按优先级排序处理器
        handlers.sort(Comparator.comparingInt(TaskCompletionHandler::getOrder));

        // 2. 找到支持的处理器
        List<TaskCompletionHandler> supportedHandlers = new ArrayList<>();
        for (TaskCompletionHandler handler : handlers) {
            if (handler.supports(context)) {
                supportedHandlers.add(handler);
            }
        }

        if (supportedHandlers.isEmpty()) {
            log.warn("No handler found for context: {}", context);
            return;
        }

        log.info("Found {} handlers for task completion", supportedHandlers.size());

        // 3. 前置处理
        for (TaskCompletionHandler handler : supportedHandlers) {
            try {
                handler.preHandle(context);
            } catch (Exception e) {
                log.error("Error in preHandle", e);
                throw new RuntimeException("Pre-handle failed", e);
            }
        }

        try {
            // 4. 执行处理
            for (TaskCompletionHandler handler : supportedHandlers) {
                try {
                    handler.handle(context);
                } catch (Exception e) {
                    log.error("Error in handle", e);
                    throw new RuntimeException("Handle failed", e);
                }
            }

            // 5. 后置处理
            for (TaskCompletionHandler handler : supportedHandlers) {
                try {
                    handler.postHandle(context);
                } catch (Exception e) {
                    log.error("Error in postHandle", e);
                    // 后置处理异常不影响主流程
                }
            }

        } catch (Exception e) {
            // 6. 异常回滚处理
            rollbackOnException(context, supportedHandlers);
            throw e;
        }
    }

    /**
     * 异常时回滚
     *
     * @param context 提交上下文
     * @param handlers 处理器列表
     */
    private void rollbackOnException(
        TaskCompletionContext context,
        List<TaskCompletionHandler> handlers
    ) {
        log.info("Rolling back due to exception");

        // 逆序执行回滚
        List<TaskCompletionHandler> reversed = new ArrayList<>(handlers);
        Collections.reverse(reversed);

        for (TaskCompletionHandler handler : reversed) {
            try {
                handler.rollback(context);
            } catch (Exception e) {
                // 记录错误，继续回滚
                log.error("Error during rollback", e);
            }
        }
    }
}
