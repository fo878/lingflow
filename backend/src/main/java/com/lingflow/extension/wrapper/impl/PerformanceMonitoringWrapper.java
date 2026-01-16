package com.lingflow.extension.wrapper.impl;

import com.lingflow.extension.wrapper.FlowableServiceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 性能监控包装器
 * 监控 Flowable API 调用的性能，记录耗时
 */
@Slf4j
@Component
@Order(2)
public class PerformanceMonitoringWrapper implements FlowableServiceWrapper {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public void before(String operation, Object... args) {
        startTime.set(System.currentTimeMillis());
    }

    @Override
    public void after(String operation, Object result, Object... args) {
        Long start = startTime.get();
        if (start != null) {
            long duration = System.currentTimeMillis() - start;
            log.info("Operation [{}] took {} ms", operation, duration);

            // 如果耗时超过阈值，记录警告
            if (duration > 1000) {
                log.warn("Operation [{}] took too long: {} ms", operation, duration);
            }
        }
        startTime.remove();
    }

    @Override
    public void onException(String operation, Exception e, Object... args) {
        startTime.remove();
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
