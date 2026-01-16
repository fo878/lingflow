package com.lingflow.extension.wrapper.impl;

import com.lingflow.extension.wrapper.FlowableServiceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志记录包装器
 * 记录 Flowable API 调用的详细日志
 */
@Slf4j
@Component
@Order(1)
public class LoggingWrapper implements FlowableServiceWrapper {

    @Override
    public void before(String operation, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Flowable operation [{}] started with args: {}",
                operation, args.length > 0 ? Arrays.toString(args) : "[]");
        }
    }

    @Override
    public void after(String operation, Object result, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug("Flowable operation [{}] completed with result: {}",
                operation, result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    @Override
    public void onException(String operation, Exception e, Object... args) {
        log.error("Flowable operation [{}] failed", operation, e);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
