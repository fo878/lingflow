package com.lingflow.extension.wrapper;

/**
 * Flowable 服务包装器基础接口
 * 用于在 Flowable API 调用前后插入扩展逻辑
 */
public interface FlowableServiceWrapper {

    /**
     * 前置处理
     *
     * @param operation 操作名称
     * @param args 方法参数
     */
    default void before(String operation, Object... args) {
        // 默认不做处理
    }

    /**
     * 后置处理
     *
     * @param operation 操作名称
     * @param result 返回结果
     * @param args 方法参数
     */
    default void after(String operation, Object result, Object... args) {
        // 默认不做处理
    }

    /**
     * 异常处理
     *
     * @param operation 操作名称
     * @param e 异常对象
     * @param args 方法参数
     */
    default void onException(String operation, Exception e, Object... args) {
        // 默认抛出异常
        throw new RuntimeException("Operation failed: " + operation, e);
    }

    /**
     * 获取包装器优先级（数字越小优先级越高）
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
