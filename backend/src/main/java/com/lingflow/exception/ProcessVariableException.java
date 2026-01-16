package com.lingflow.exception;

/**
 * 流程变量异常
 */
public class ProcessVariableException extends BusinessException {

    public ProcessVariableException(String variableName) {
        super(400, String.format("流程变量异常: %s", variableName));
    }

    public ProcessVariableException(String message, Throwable cause) {
        super(400, message);
        this.initCause(cause);
    }

    public ProcessVariableException(String variableName, String reason) {
        super(400, String.format("流程变量异常 [%s]: %s", variableName, reason));
    }
}
