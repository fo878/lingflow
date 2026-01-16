package com.lingflow.exception;

/**
 * 流程部署异常
 */
public class ProcessDeploymentException extends BusinessException {

    public ProcessDeploymentException(String message) {
        super(500, message);
    }

    public ProcessDeploymentException(String message, Throwable cause) {
        super(500, message);
        this.initCause(cause);
    }

    public ProcessDeploymentException(String processDefinitionKey, String reason) {
        super(500, String.format("流程部署异常 [%s]: %s", processDefinitionKey, reason));
    }
}
