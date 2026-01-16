package com.lingflow.extension.handler;

/**
 * 任务提交处理器接口
 * 用于处理不同节点类型间的流转逻辑
 */
public interface TaskCompletionHandler {

    /**
     * 是否支持处理该上下文
     *
     * @param context 提交上下文
     * @return 是否支持
     */
    boolean supports(TaskCompletionContext context);

    /**
     * 前置处理
     *
     * @param context 提交上下文
     */
    void preHandle(TaskCompletionContext context);

    /**
     * 处理提交
     *
     * @param context 提交上下文
     */
    void handle(TaskCompletionContext context);

    /**
     * 后置处理
     *
     * @param context 提交上下文
     */
    void postHandle(TaskCompletionContext context);

    /**
     * 异常回滚
     *
     * @param context 提交上下文
     */
    default void rollback(TaskCompletionContext context) {
        // 默认不做处理
    }

    /**
     * 获取处理器优先级（数字越小优先级越高）
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
