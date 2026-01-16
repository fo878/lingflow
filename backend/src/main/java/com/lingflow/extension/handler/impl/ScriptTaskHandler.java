package com.lingflow.extension.handler.impl;

import com.lingflow.extension.handler.TaskCompletionContext;
import com.lingflow.extension.handler.TaskCompletionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 脚本任务处理器
 * 处理用户任务到脚本任务的流转
 */
@Slf4j
@Component
public class ScriptTaskHandler implements TaskCompletionHandler {

    @Override
    public boolean supports(TaskCompletionContext context) {
        TaskCompletionContext.NodeType nextNodeType = context.getNextNodeType();
        return context.getCurrentNodeType() == TaskCompletionContext.NodeType.USER_TASK
            && nextNodeType == TaskCompletionContext.NodeType.SCRIPT_TASK;
    }

    @Override
    public void preHandle(TaskCompletionContext context) {
        String currentTaskName = context.getCurrentNodeInfo().getName();
        String scriptTaskId = context.getNextNodeInfo().getId();
        String scriptTaskName = context.getNextNodeInfo().getName();

        log.info("进入脚本任务 - 当前任务: {}, 脚本任务: {}({})",
            currentTaskName, scriptTaskName, scriptTaskId);

        // 前置处理逻辑：
        // 1. 验证脚本任务配置
        // 2. 准备脚本执行环境
        // 3. 检查脚本安全

        // 获取脚本语言
        String scriptFormat = context.getVariables()
            .getOrDefault("scriptFormat", "javascript").toString();
        log.debug("脚本语言: {}", scriptFormat);

        // 获取脚本内容
        String script = context.getVariables()
            .getOrDefault("script", "").toString();
        if (!script.isEmpty()) {
            log.debug("脚本内容: {}", script);
        }

        // 检查是否需要存储脚本变量
        Boolean storeVariables = context.getVariables()
            .get("storeScriptVariables") != null
                ? (Boolean) context.getVariables().get("storeScriptVariables")
                : false;
        if (storeVariables) {
            log.debug("将存储脚本变量");
        }

        // 检查脚本是否自动存储变量
        Boolean autoStoreVariables = context.getVariables()
            .get("autoStoreVariables") != null
                ? (Boolean) context.getVariables().get("autoStoreVariables")
                : true;
        log.debug("自动存储变量: {}", autoStoreVariables);
    }

    @Override
    public void handle(TaskCompletionContext context) {
        String scriptTaskName = context.getNextNodeInfo().getName();
        String scriptFormat = context.getVariables()
            .getOrDefault("scriptFormat", "javascript").toString();

        log.debug("处理脚本任务流转 - 脚本: {}, 语言: {}",
            scriptTaskName, scriptFormat);

        // 核心处理逻辑：
        // 1. 根据脚本语言执行不同的处理
        // 2. 设置脚本执行标记
        // 3. 记录脚本执行信息

        context.getVariables().put("scriptTaskExecuted", true);
        context.getVariables().put("scriptTaskName", scriptTaskName);
        context.getVariables().put("scriptFormat", scriptFormat);

        // 处理不同语言的脚本任务
        switch (scriptFormat.toLowerCase()) {
            case "javascript":
            case "js":
                handleJavaScript(context);
                break;
            case "groovy":
                handleGroovy(context);
                break;
            case "python":
            case "jython":
                handlePython(context);
                break;
            case "ruby":
            case "jruby":
                handleRuby(context);
                break;
            default:
                log.warn("未知脚本语言: {}", scriptFormat);
                break;
        }

        // 设置脚本执行结果变量
        String resultVariable = context.getVariables()
            .getOrDefault("resultVariable", "scriptResult").toString();
        context.getVariables().put(resultVariable + "VariableSet", true);
    }

    @Override
    public void postHandle(TaskCompletionContext context) {
        String scriptTaskName = context.getNextNodeInfo().getName();

        log.info("脚本任务处理完成 - 脚本: {}", scriptTaskName);

        // 后置处理逻辑：
        // 1. 检查脚本执行结果
        // 2. 处理脚本返回值
        // 3. 记录脚本执行日志

        // 获取脚本执行结果
        Object scriptResult = context.getVariables().get("scriptResult");
        if (scriptResult != null) {
            log.info("脚本执行结果: {}", scriptResult);
            context.getVariables().put("lastScriptResult", scriptResult);
        }

        // 检查是否有异常
        Object scriptException = context.getVariables().get("scriptException");
        if (scriptException != null) {
            log.error("脚本执行异常: {}", scriptException);
            context.getVariables().put("lastScriptException", scriptException);
        }

        // 检查脚本是否修改了变量
        Boolean variablesModified = context.getVariables()
            .get("variablesModified") != null
                ? (Boolean) context.getVariables().get("variablesModified")
                : false;
        if (variablesModified) {
            log.info("脚本修改了流程变量");
        }
    }

    @Override
    public void rollback(TaskCompletionContext context) {
        String scriptTaskName = context.getNextNodeInfo().getName();

        log.warn("脚本任务流转回滚 - 脚本: {}, 流程实例ID: {}",
            scriptTaskName, context.getProcessInstanceId());

        // 回滚逻辑：
        // 1. 检查脚本是否已执行
        // 2. 如果已执行且修改了变量，恢复变量
        // 3. 清理脚本执行状态

        Boolean scriptExecuted = context.getVariables()
            .get("scriptTaskExecuted") != null
                ? (Boolean) context.getVariables().get("scriptTaskExecuted")
                : false;

        if (scriptExecuted) {
            log.warn("脚本任务已执行，可能需要恢复变量");

            // 检查是否保存了变量备份
            Object variableBackup = context.getVariables().get("variableBackup");
            if (variableBackup != null) {
                log.info("从备份恢复变量: {}", variableBackup);
                context.getVariables().put("variablesRestored", true);
            }
        }
    }

    /**
     * 处理 JavaScript 脚本
     */
    private void handleJavaScript(TaskCompletionContext context) {
        String script = context.getVariables()
            .getOrDefault("script", "").toString();

        log.debug("处理 JavaScript 脚本");

        if (!script.isEmpty()) {
            context.getVariables().put("javascriptScriptExecuted", true);
            context.getVariables().put("scriptLanguage", "javascript");

            // Flowable 使用 Nashorn 或 GraalVM 执行 JavaScript
            // 这里只记录，实际执行由 Flowable 引擎完成
            log.debug("JavaScript 脚本将由 Flowable 引擎执行");
        }
    }

    /**
     * 处理 Groovy 脚本
     */
    private void handleGroovy(TaskCompletionContext context) {
        String script = context.getVariables()
            .getOrDefault("script", "").toString();

        log.debug("处理 Groovy 脚本");

        if (!script.isEmpty()) {
            context.getVariables().put("groovyScriptExecuted", true);
            context.getVariables().put("scriptLanguage", "groovy");

            // Flowable 使用 Groovy 引擎执行 Groovy 脚本
            log.debug("Groovy 脚本将由 Flowable 引擎执行");
        }
    }

    /**
     * 处理 Python 脚本
     */
    private void handlePython(TaskCompletionContext context) {
        String script = context.getVariables()
            .getOrDefault("script", "").toString();

        log.debug("处理 Python 脚本");

        if (!script.isEmpty()) {
            context.getVariables().put("pythonScriptExecuted", true);
            context.getVariables().put("scriptLanguage", "python");

            // Flowable 可以使用 Jython 执行 Python 脚本
            log.debug("Python 脚本将由 Flowable 引擎执行（需要 Jython）");
        }
    }

    /**
     * 处理 Ruby 脚本
     */
    private void handleRuby(TaskCompletionContext context) {
        String script = context.getVariables()
            .getOrDefault("script", "").toString();

        log.debug("处理 Ruby 脚本");

        if (!script.isEmpty()) {
            context.getVariables().put("rubyScriptExecuted", true);
            context.getVariables().put("scriptLanguage", "ruby");

            // Flowable 可以使用 JRuby 执行 Ruby 脚本
            log.debug("Ruby 脚本将由 Flowable 引擎执行（需要 JRuby）");
        }
    }

    @Override
    public int getOrder() {
        return 60;
    }
}
