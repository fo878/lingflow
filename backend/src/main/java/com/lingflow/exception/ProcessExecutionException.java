package com.lingflow.exception;

/**
 * 流程执行异常
 */
public class ProcessExecutionException extends BusinessException {

    public ProcessExecutionException(String message) {
        super(500, message);
    }

    public ProcessExecutionException(String message, Throwable cause) {
        super(500, message);
        this.initCause(cause);
    }

    public ProcessExecutionException(String processInstanceId, String reason) {
        super(500, String.format("流程执行异常 [%s]: %s", processInstanceId, reason));
    }
}
