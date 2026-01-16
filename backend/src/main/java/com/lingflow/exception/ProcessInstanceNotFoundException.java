package com.lingflow.exception;

/**
 * 流程实例不存在异常
 */
public class ProcessInstanceNotFoundException extends BusinessException {

    public ProcessInstanceNotFoundException(String processInstanceId) {
        super(404, String.format("流程实例不存在: %s", processInstanceId));
    }

    public ProcessInstanceNotFoundException(String message, Throwable cause) {
        super(404, message);
        this.initCause(cause);
    }
}
