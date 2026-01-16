package com.lingflow.exception;

/**
 * 流程超时异常
 */
public class ProcessTimeoutException extends BusinessException {

    private String processInstanceId;
    private Long timeoutHours;

    public ProcessTimeoutException(String processInstanceId, Long timeoutHours) {
        super(408, String.format("流程超时 [%s]: 超过 %d 小时未完成", processInstanceId, timeoutHours));
        this.processInstanceId = processInstanceId;
        this.timeoutHours = timeoutHours;
    }

    public ProcessTimeoutException(String message) {
        super(408, message);
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public Long getTimeoutHours() {
        return timeoutHours;
    }
}
