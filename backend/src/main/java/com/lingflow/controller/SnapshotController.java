package com.lingflow.controller;

import com.lingflow.dto.CreateSnapshotRequest;
import com.lingflow.dto.Result;
import com.lingflow.entity.ProcessSnapshot;
import com.lingflow.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程快照控制器
 * 提供流程快照的创建、查询、回滚和删除功能
 */
@RestController
@RequestMapping("/snapshot")
@CrossOrigin(origins = "*")
public class SnapshotController {

    @Autowired
    private ProcessService processService;

    /**
     * 创建流程快照
     * @param request 创建快照请求
     * @return 操作结果
     */
    @PostMapping("/create")
    public Result<Void> createSnapshot(@RequestBody CreateSnapshotRequest request) {
        try {
            processService.createProcessSnapshot(
                request.getProcessDefinitionKey(),
                request.getSnapshotName(),
                request.getDescription(),
                request.getCreator()
            );
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程快照列表
     * @param processDefinitionKey 流程定义KEY
     * @return 快照列表
     */
    @GetMapping("/list/{processDefinitionKey}")
    public Result<List<ProcessSnapshot>> getSnapshots(@PathVariable String processDefinitionKey) {
        try {
            List<ProcessSnapshot> snapshots = processService.getProcessSnapshots(processDefinitionKey);
            return Result.success(snapshots);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 回滚到指定快照
     * @param snapshotId 快照ID
     * @return 操作结果
     */
    @PostMapping("/rollback/{snapshotId}")
    public Result<Void> rollbackToSnapshot(@PathVariable Long snapshotId) {
        try {
            processService.rollbackToSnapshot(snapshotId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除快照
     * @param snapshotId 快照ID
     * @return 操作结果
     */
    @DeleteMapping("/{snapshotId}")
    public Result<Void> deleteSnapshot(@PathVariable Long snapshotId) {
        try {
            processService.deleteSnapshot(snapshotId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
