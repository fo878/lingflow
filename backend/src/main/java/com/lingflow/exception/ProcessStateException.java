package com.lingflow.exception;

/**
 * 流程状态异常
 */
public class ProcessStateException extends BusinessException {

    public ProcessStateException(String message) {
        super(400, message);
    }

    public ProcessStateException(String message, Throwable cause) {
        super(400, message);
        this.initCause(cause);
    }

    public ProcessStateException(String processInstanceId, String expectedState, String actualState) {
        super(400, String.format("流程状态异常 [%s]: 期望状态 %s, 实际状态 %s",
            processInstanceId, expectedState, actualState));
    }

    public ProcessStateException(String processInstanceId, String reason) {
        super(400, String.format("流程状态异常 [%s]: %s", processInstanceId, reason));
    }
}
