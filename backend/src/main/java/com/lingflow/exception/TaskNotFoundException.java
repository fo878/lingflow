package com.lingflow.exception;

/**
 * 任务不存在异常
 */
public class TaskNotFoundException extends BusinessException {

    public TaskNotFoundException(String taskId) {
        super(404, String.format("任务不存在: %s", taskId));
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(404, message);
        this.initCause(cause);
    }
}
