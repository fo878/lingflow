package com.lingflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * 任务视图对象
 */
@Data
public class TaskVO {
    private String id;
    private String name;
    private String processInstanceId;
    private Date createTime;
    private String assignee;
}
