package com.lingflow.exception;

/**
 * 流程定义不存在异常
 */
public class ProcessDefinitionNotFoundException extends BusinessException {
    public ProcessDefinitionNotFoundException(String processDefinitionId) {
        super(1001, "流程定义不存在: " + processDefinitionId);
    }
}
