package com.lingflow.exception;

import java.util.List;

/**
 * 流程验证异常
 */
public class ProcessValidationException extends BusinessException {

    private List<String> errors;

    public ProcessValidationException(String message) {
        super(400, message);
    }

    public ProcessValidationException(List<String> errors) {
        super(400, "流程验证失败: " + String.join("; ", errors));
        this.errors = errors;
    }

    public ProcessValidationException(String message, List<String> errors) {
        super(400, message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
