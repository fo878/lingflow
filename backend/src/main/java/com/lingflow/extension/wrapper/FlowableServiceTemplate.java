package com.lingflow.extension.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Flowable 服务执行模板
 * 负责管理包装器链，在调用 Flowable API 前后执行扩展逻辑
 */
@Slf4j
@Component
public class FlowableServiceTemplate {

    @Autowired(required = false)
    private List<FlowableServiceWrapper> wrappers;

    /**
     * 执行服务方法
     *
     * @param operation 操作名称
     * @param serviceMethod 服务方法
     * @param args 方法参数
     * @param <T> 返回类型
     * @return 方法执行结果
     */
    public <T> T execute(
        String operation,
        ServiceSupplier<T> serviceMethod,
        Object... args
    ) {
        // 按优先级排序包装器
        if (wrappers != null && !wrappers.isEmpty()) {
            wrappers.sort(Comparator.comparingInt(FlowableServiceWrapper::getOrder));

            // 1. 前置处理
            for (FlowableServiceWrapper wrapper : wrappers) {
                wrapper.before(operation, args);
            }

            try {
                // 2. 执行原方法
                T result = serviceMethod.get();

                // 3. 后置处理
                for (FlowableServiceWrapper wrapper : wrappers) {
                    wrapper.after(operation, result, args);
                }

                return result;

            } catch (Exception e) {
                // 4. 异常处理
                for (FlowableServiceWrapper wrapper : wrappers) {
                    wrapper.onException(operation, e, args);
                }
                throw new RuntimeException("Operation failed: " + operation, e);
            }
        } else {
            // 没有包装器，直接执行
            try {
                return serviceMethod.get();
            } catch (Exception e) {
                throw new RuntimeException("Operation failed: " + operation, e);
            }
        }
    }

    /**
     * 服务方法函数式接口
     */
    @FunctionalInterface
    public interface ServiceSupplier<T> {
        T get() throws Exception;
    }
}
