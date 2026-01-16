package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 服务任务处理器
 * 处理用户任务到服务任务的流转
 */
@Slf4j
@Component
public class ServiceTaskHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && nextNodeType == TaskCompletionContext.NodeType.SERVICE_TASK;
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String serviceTaskId = context.getNextNodeInfo().getId();
        String serviceTaskName = context.getNextNodeInfo().getName();

        log.info("进入服务任务 - 当前任务: {}, 服务任务: {}({})",
            currentTaskName, serviceTaskName, serviceTaskId);

        // 前置处理逻辑：
        // 1. 验证服务任务配置
        // 2. 准备服务任务参数
        // 3. 检查服务调用权限

        // 获取服务任务类型
        String serviceType = context.getVariables()
            .getOrDefault("serviceType", "java").toString();
        log.debug("服务任务类型: {}", serviceType);

        // 验证服务任务表达式
        String expression = context.getVariables()
            .getOrDefault("serviceExpression", "").toString();
        if (!expression.isEmpty()) {
            log.debug("服务任务表达式: {}", expression);
        }

        // 检查是否需要异步执行
        Boolean async = context.getVariables()
            .get("async") != null
                ? (Boolean) context.getVariables().get("async")
                : false;
        if (async) {
            log.info("服务任务将异步执行");
        }
    }

    @Override
    public void handle(TaskCompletionContext context) {
        String serviceTaskName = context.getNextNodeInfo().getName();
        String serviceType = context.getVariables()
            .getOrDefault("serviceType", "java").toString();

        log.debug("处理服务任务流转 - 服务: {}, 类型: {}",
            serviceTaskName, serviceType);

        // 核心处理逻辑：
        // 1. 根据服务类型执行不同的处理
        // 2. 设置服务任务执行标记
        // 3. 记录服务调用信息

        context.getVariables().put("serviceTaskExecuted", true);
        context.getVariables().put("serviceTaskName", serviceTaskName);
        context.getVariables().put("serviceType", serviceType);

        // 处理不同类型的服务任务
        switch (serviceType.toLowerCase()) {
            case "java":
                handleJavaService(context);
                break;
            case "expression":
                handleExpressionService(context);
                break;
            case "delegate":
                handleDelegateExpression(context);
                break;
            case "webservice":
                handleWebService(context);
                break;
            case "external":
                handleExternalService(context);
                break;
            default:
                log.debug("未知服务类型: {}", serviceType);
                break;
        }

        // 设置服务任务超时时间
        Object timeout = context.getVariables().get("serviceTimeout");
        if (timeout != null) {
            log.debug("服务任务超时时间: {}", timeout);
            context.getVariables().put("serviceTimeoutSet", true);
        }
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String serviceTaskName = context.getNextNodeInfo().getName();

        log.info("服务任务处理完成 - 服务: {}", serviceTaskName);

        // 后置处理逻辑：
        // 1. 检查服务执行结果
        // 2. 处理服务返回值
        // 3. 记录服务执行日志

        // 获取服务执行结果
        Object serviceResult = context.getVariables().get("serviceResult");
        if (serviceResult != null) {
            log.info("服务执行结果: {}", serviceResult);
            context.getVariables().put("lastServiceResult", serviceResult);
        }

        // 检查是否有异常
        Object serviceException = context.getVariables().get("serviceException");
        if (serviceException != null) {
            log.warn("服务执行异常: {}", serviceException);
            context.getVariables().put("lastServiceException", serviceException);
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        String serviceTaskName = context.getNextNodeInfo().getName();

        log.warn("服务任务流转回滚 - 服务: {}, 流程实例ID: {}",
            serviceTaskName, context.getProcessInstanceId());

        // 回滚逻辑：
        // 1. 检查服务是否已执行
        // 2. 如果已执行，尝试补偿操作
        // 3. 清理服务执行状态

        Boolean serviceExecuted = context.getVariables()
            .get("serviceTaskExecuted") != null
                ? (Boolean) context.getVariables().get("serviceTaskExecuted")
                : false;

        if (serviceExecuted) {
            log.warn("服务任务已执行，可能需要补偿操作");

            // 这里可以添加补偿逻辑
            String compensationHandler = context.getVariables()
                .getOrDefault("compensationHandler", "").toString();

            if (!compensationHandler.isEmpty()) {
                log.info("补偿处理器: {}", compensationHandler);
                context.getVariables().put("compensationRequired", true);
            }
        }
    }

    /**
     * 处理 Java 类服务
     */
    private void handleJavaService(TaskCompletionContext context) {
        String className = context.getVariables()
            .getOrDefault("serviceClassName", "").toString();

        log.debug("处理 Java 类服务 - 类名: {}", className);

        if (!className.isEmpty()) {
            context.getVariables().put("javaServiceInvoked", true);
            context.getVariables().put("serviceClassName", className);
        }
    }

    /**
     * 处理表达式服务
     */
    private void handleExpressionService(TaskCompletionContext context) {
        String expression = context.getVariables()
            .getOrDefault("serviceExpression", "").toString();

        log.debug("处理表达式服务 - 表达式: {}", expression);

        if (!expression.isEmpty()) {
            context.getVariables().put("expressionServiceInvoked", true);
            context.getVariables().put("serviceExpression", expression);
        }
    }

    /**
     * 处理委托表达式服务
     */
    private void handleDelegateExpression(TaskCompletionContext context) {
        String delegateExpression = context.getVariables()
            .getOrDefault("delegateExpression", "").toString();

        log.debug("处理委托表达式服务 - 表达式: {}", delegateExpression);

        if (!delegateExpression.isEmpty()) {
            context.getVariables().put("delegateServiceInvoked", true);
            context.getVariables().put("delegateExpression", delegateExpression);
        }
    }

    /**
     * 处理 Web 服务
     */
    private void handleWebService(TaskCompletionContext context) {
        String wsdlUrl = context.getVariables()
            .getOrDefault("wsdlUrl", "").toString();
        String operation = context.getVariables()
            .getOrDefault("wsOperation", "").toString();

        log.debug("处理 Web 服务 - WSDL: {}, 操作: {}", wsdlUrl, operation);

        if (!wsdlUrl.isEmpty()) {
            context.getVariables().put("webServiceInvoked", true);
            context.getVariables().put("wsdlUrl", wsdlUrl);
            context.getVariables().put("wsOperation", operation);
        }
    }

    /**
     * 处理外部服务
     */
    private void handleExternalService(TaskCompletionContext context) {
        String topic = context.getVariables()
            .getOrDefault("externalTopic", "").toString();
        String type = context.getVariables()
            .getOrDefault("externalType", "").toString();

        log.debug("处理外部服务 - 主题: {}, 类型: {}", topic, type);

        if (!topic.isEmpty()) {
            context.getVariables().put("externalServiceInvoked", true);
            context.getVariables().put("externalTopic", topic);
            context.getVariables().put("externalType", type);
        }
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
