package com.lingflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LingFlow 流程管理系统主应用类
 */
@SpringBootApplication
public class LingflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingflowApplication.class, args);
        System.out.println("========================================");
        System.out.println("LingFlow 流程管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
