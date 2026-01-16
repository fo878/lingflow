package com.lingflow.extension.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 任务提交上下文
 * 包含任务提交过程中需要的所有信息
 */
@Data
@Builder
@AllArgsConstructor
public class TaskCompletionContext {

    /**
     * 当前任务ID
     */
    private String taskId;

    /**
     * 当前节点类型
     */
    private NodeType currentNodeType;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 下一个节点信息
     */
    private List<NodeInfo> nextNodes;

    /**
     * 扩展数据
     */
    private Map<String, Object> extendedData;

    /**
     * 当前节点信息
     */
    private NodeInfo currentNodeInfo;

    /**
     * 获取下一个节点类型
     * @return 下一个节点类型，如果没有下一个节点则返回 null
     */
    public NodeType getNextNodeType() {
        if (nextNodes == null || nextNodes.isEmpty()) {
            return null;
        }
        return nextNodes.get(0).getNodeType();
    }

    /**
     * 获取当前节点信息
     * @return 当前节点信息
     */
    public NodeInfo getCurrentNodeInfo() {
        return currentNodeInfo;
    }

    /**
     * 设置当前节点信息
     * @param currentNodeInfo 当前节点信息
     */
    public void setCurrentNodeInfo(NodeInfo currentNodeInfo) {
        this.currentNodeInfo = currentNodeInfo;
    }

    /**
     * 获取下一个节点信息
     * @return 下一个节点信息，如果没有下一个节点则返回 null
     */
    public NodeInfo getNextNodeInfo() {
        if (nextNodes == null || nextNodes.isEmpty()) {
            return null;
        }
        return nextNodes.get(0);
    }

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        USER_TASK("userTask", "用户任务"),
        SERVICE_TASK("serviceTask", "服务任务"),
        SCRIPT_TASK("scriptTask", "脚本任务"),
        RECEIVE_TASK("receiveTask", "接收任务"),
        EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
        PARALLEL_GATEWAY("parallelGateway", "并行网关"),
        INCLUSIVE_GATEWAY("inclusiveGateway", "包容网关"),
        CALL_ACTIVITY("callActivity", "调用活动"),
        MULTI_INSTANCE("multiInstance", "多实例");

        private final String code;
        private final String name;

        NodeType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 节点信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class NodeInfo {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 节点类型
         */
        private NodeType nodeType;

        /**
         * 节点属性
         */
        private Map<String, Object> properties;

        /**
         * 获取节点ID（别名方法，兼容旧代码）
         */
        public String getId() {
            return nodeId;
        }

        /**
         * 获取节点名称（别名方法，兼容旧代码）
         */
        public String getName() {
            return nodeName;
        }

        /**
         * 获取受理人（从 properties 中获取）
         */
        public String getAssignee() {
            if (properties != null) {
                Object assignee = properties.get("assignee");
                return assignee != null ? assignee.toString() : null;
            }
            return null;
        }
    }
}
