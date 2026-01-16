package com.lingflow.extension.handler;

import com.lingflow.extension.handler.TaskCompletionContext.NodeType;

import java.lang.annotation.*;

/**
 * 标记任务提交处理器支持的节点类型
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SupportedNodeTypes {

    /**
     * 支持的当前节点类型
     */
    NodeType[] current() default {};

    /**
     * 支持的下一节点类型
     */
    NodeType[] next() default {};

    /**
     * 支持的节点流转组合（如果指定，则忽略 current 和 next）
     * 格式：当前节点类型 -> 下一节点类型
     */
    String[] transitions() default {};
}
