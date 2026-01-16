package com.lingflow.extension.event.impl;

import com.lingflow.extension.event.ProcessEvent;
import com.lingflow.extension.event.ProcessEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程启动事件监听器
 */
@Slf4j
@Component
public class ProcessStartListener implements ProcessEventListener {

    @Override
    public void onBefore(ProcessEvent event) {
        log.info("流程即将启动 - 流程定义Key: {}, 流程实例ID: {}",
            event.getData("processDefinitionKey", String.class),
            event.getProcessInstanceId()
        );

        // 可以在这里添加前置逻辑，如：
        // - 验证启动权限
        // - 初始化业务变量
        // - 记录审计日志
    }

    @Override
    public void onAfter(ProcessEvent event) {
        log.info("流程已启动 - 流程定义Key: {}, 流程实例ID: {}, 业务Key: {}",
            event.getData("processDefinitionKey", String.class),
            event.getProcessInstanceId(),
            event.getData("businessKey", String.class)
        );

        // 可以在这里添加后置逻辑，如：
        // - 发送启动通知
        // - 触发业务规则引擎
        // - 更新业务状态
    }

    @Override
    public boolean supports(String eventType) {
        return "PROCESS_START".equals(eventType);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
