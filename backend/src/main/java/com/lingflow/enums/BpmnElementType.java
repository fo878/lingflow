package com.lingflow.enums;

/**
 * BPMN元素类型枚举
 * 定义所有支持的BPMN元素类型
 */
public enum BpmnElementType {
    USER_TASK("userTask", "用户任务"),
    SERVICE_TASK("serviceTask", "服务任务"),
    SCRIPT_TASK("scriptTask", "脚本任务"),
    BUSINESS_RULE_TASK("businessRuleTask", "业务规则任务"),
    RECEIVE_TASK("receiveTask", "接收任务"),
    MANUAL_TASK("manualTask", "手动任务"),
    EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
    INCLUSIVE_GATEWAY("inclusiveGateway", "包容网关"),
    PARALLEL_GATEWAY("parallelGateway", "并行网关"),
    EVENT_GATEWAY("eventGateway", "事件网关"),
    START_EVENT("startEvent", "开始事件"),
    END_EVENT("endEvent", "结束事件"),
    INTERMEDIATE_CATCH_EVENT("intermediateCatchEvent", "中间捕获事件"),
    INTERMEDIATE_THROW_EVENT("intermediateThrowEvent", "中间抛出事件"),
    BOUNDARY_EVENT("boundaryEvent", "边界事件"),
    SUB_PROCESS("subProcess", "子流程"),
    CALL_ACTIVITY("callActivity", "调用活动"),
    SEQUENCE_FLOW("sequenceFlow", "序列流");

    private final String value;
    private final String description;

    BpmnElementType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据值获取枚举
     * @param value 元素类型值
     * @return 对应的枚举，如果不存在返回null
     */
    public static BpmnElementType fromValue(String value) {
        for (BpmnElementType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
