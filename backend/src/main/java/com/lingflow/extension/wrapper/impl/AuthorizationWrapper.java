package com.lingflow.extension.wrapper.impl;

import com.lingflow.extension.wrapper.FlowableServiceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 权限检查包装器
 * 在执行 Flowable API 调用前检查用户权限
 */
@Slf4j
@Component
public class AuthorizationWrapper implements FlowableServiceWrapper {

    private static final String[] PUBLIC_OPERATIONS = {
        "RepositoryService.createProcessDefinitionQuery",
        "RuntimeService.createProcessInstanceQuery",
        "TaskService.createTaskQuery",
        "HistoryService.createHistoricProcessInstanceQuery",
        "HistoryService.createHistoricTaskInstanceQuery"
    };

    @Override
    public void before(String operation, Object... args) {
        // 检查是否为公开操作（不需要权限检查）
        if (isPublicOperation(operation)) {
            log.debug("公开操作，跳过权限检查: {}", operation);
            return;
        }

        // 获取当前用户
        String currentUser = getCurrentUser();
        if (currentUser == null || currentUser.isEmpty()) {
            throw new RuntimeException("用户未登录，无法执行操作: " + operation);
        }

        log.debug("权限检查 - 操作: {}, 用户: {}", operation, currentUser);

        // 根据操作类型执行不同的权限检查
        if (operation.contains("deploy")) {
            checkDeployPermission(currentUser, operation, args);
        } else if (operation.contains("startProcessInstance")) {
            checkStartProcessPermission(currentUser, operation, args);
        } else if (operation.contains("completeTask")) {
            checkCompleteTaskPermission(currentUser, operation, args);
        } else if (operation.contains("deleteDeployment")) {
            checkDeletePermission(currentUser, operation, args);
        } else if (operation.contains("suspend") || operation.contains("activate")) {
            checkSuspendActivatePermission(currentUser, operation, args);
        }
    }

    @Override
    public void after(String operation, Object result, Object... args) {
        // 后置处理：记录操作审计日志
        String currentUser = getCurrentUser();
        if (currentUser != null && !currentUser.isEmpty()) {
            log.info("操作审计 - 用户: {}, 操作: {}", currentUser, operation);
            // 这里可以集成审计服务记录操作日志
        }
    }

    @Override
    public void onException(String operation, Exception e, Object... args) {
        // 异常处理：记录权限异常
        String currentUser = getCurrentUser();
        log.error("操作异常 - 用户: {}, 操作: {}, 异常: {}",
            currentUser, operation, e.getMessage());
    }

    @Override
    public int getOrder() {
        return 0; // 权限检查应该是最先执行的
    }

    /**
     * 检查是否为公开操作
     */
    private boolean isPublicOperation(String operation) {
        for (String publicOp : PUBLIC_OPERATIONS) {
            if (operation.contains(publicOp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 从请求头获取用户信息
                String user = request.getHeader("X-User-Id");
                if (user == null || user.isEmpty()) {
                    // 从请求参数获取用户信息
                    user = request.getParameter("userId");
                }
                if (user == null || user.isEmpty()) {
                    // 从 session 获取用户信息
                    user = (String) request.getSession().getAttribute("userId");
                }
                return user;
            }
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查部署权限
     */
    private void checkDeployPermission(String user, String operation, Object... args) {
        log.debug("检查部署权限 - 用户: {}", user);

        // 这里可以集成权限服务
        // 例如：
        // if (!permissionService.hasPermission(user, "process:deploy")) {
        //     throw new RuntimeException("用户没有部署流程的权限");
        // }

        // 简化实现：只允许管理员部署
        if (!isAdmin(user)) {
            throw new RuntimeException("只有管理员才能部署流程");
        }
    }

    /**
     * 检查启动流程权限
     */
    private void checkStartProcessPermission(String user, String operation, Object... args) {
        log.debug("检查启动流程权限 - 用户: {}", user);

        // 获取流程定义Key
        if (args.length > 0 && args[0] instanceof String) {
            String processKey = (String) args[0];
            log.debug("启动流程 - 流程Key: {}", processKey);

            // 这里可以检查用户是否有启动特定流程的权限
            // 例如：
            // if (!permissionService.hasPermission(user, "process:start:" + processKey)) {
            //     throw new RuntimeException("用户没有启动该流程的权限");
            // }
        }
    }

    /**
     * 检查完成任务权限
     */
    private void checkCompleteTaskPermission(String user, String operation, Object... args) {
        log.debug("检查完成任务权限 - 用户: {}", user);

        // 获取任务ID
        if (args.length > 0 && args[0] instanceof String) {
            String taskId = (String) args[0];
            log.debug("完成任务 - 任务ID: {}", taskId);

            // 这里可以检查用户是否有完成该任务的权限
            // 例如：
            // 1. 检查任务是否分配给当前用户
            // 2. 检查用户是否是任务的候选人
            // 3. 检查用户是否有该任务的代理权限

            // 简化实现：记录日志
            log.info("用户 {} 尝试完成任务 {}", user, taskId);
        }
    }

    /**
     * 检查删除权限
     */
    private void checkDeletePermission(String user, String operation, Object... args) {
        log.debug("检查删除权限 - 用户: {}", user);

        // 删除操作通常需要管理员权限
        if (!isAdmin(user)) {
            throw new RuntimeException("只有管理员才能删除流程定义");
        }
    }

    /**
     * 检查挂起/激活权限
     */
    private void checkSuspendActivatePermission(String user, String operation, Object... args) {
        log.debug("检查挂起/激活权限 - 用户: {}", user);

        // 挂起/激活操作通常需要管理员权限
        if (!isAdmin(user)) {
            throw new RuntimeException("只有管理员才能挂起或激活流程");
        }
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(String user) {
        // 这里可以集成用户服务判断用户是否为管理员
        // 简化实现：假设admin用户是管理员
        return "admin".equals(user);
    }
}
