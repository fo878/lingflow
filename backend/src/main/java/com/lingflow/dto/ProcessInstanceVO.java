package com.lingflow.dto;

import lombok.Data;

import java.util.Date;

/**
 * 流程实例视图对象
 */
@Data
public class ProcessInstanceVO {
    private String id;
    private String processDefinitionId;
    private String businessKey;
    private Date startTime;
    private Date endTime;
}
