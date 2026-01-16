package com.lingflow.exception;

/**
 * 权限不足异常
 */
public class PermissionDeniedException extends BusinessException {

    public PermissionDeniedException(String operation) {
        super(403, String.format("权限不足: %s", operation));
    }

    public PermissionDeniedException(String operation, String userId) {
        super(403, String.format("用户 %s 无权执行操作: %s", userId, operation));
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(403, message);
        this.initCause(cause);
    }
}
