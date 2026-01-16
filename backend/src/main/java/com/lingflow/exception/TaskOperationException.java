package com.lingflow.exception;

/**
 * 任务操作异常
 */
public class TaskOperationException extends BusinessException {

    public TaskOperationException(String message) {
        super(400, message);
    }

    public TaskOperationException(String message, Throwable cause) {
        super(400, message);
        this.initCause(cause);
    }

    public TaskOperationException(String taskId, String operation, String reason) {
        super(400, String.format("任务操作异常 [%s - %s]: %s", taskId, operation, reason));
    }
}
