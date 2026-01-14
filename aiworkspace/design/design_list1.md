# 流程模板历史快照功能设计方案

## 1. 需求分析

当前系统基于Flowable工作流引擎，支持流程定义的创建、部署和管理。现在需要添加历史快照功能，允许用户：

1. 保存流程定义的任意版本作为快照
2. 查看历史快照列表
3. 快速回滚到任意历史快照

## 2. 详细设计方案

### 2.1 数据库设计

首先需要创建快照相关的数据表：

```sql
-- 流程定义快照表
CREATE TABLE process_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_definition_key VARCHAR(255) NOT NULL COMMENT '流程定义KEY',
    snapshot_name VARCHAR(255) NOT NULL COMMENT '快照名称',
    snapshot_version INT NOT NULL DEFAULT 1 COMMENT '快照版本号',
    bpmn_xml TEXT NOT NULL COMMENT 'BPMN XML内容',
    description TEXT COMMENT '快照描述',
    creator VARCHAR(255) COMMENT '创建人',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_process_key (process_definition_key),
    INDEX idx_created_time (created_time)
);
```

### 2.2 后端实现

#### 2.2.1 创建实体类

创建 `ProcessSnapshot.java` 实体类：

```java
package com.lingflow.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessSnapshot {
    private Long id;
    private String processDefinitionKey;
    private String snapshotName;
    private Integer snapshotVersion;
    private String bpmnXml;
    private String description;
    private String creator;
    private LocalDateTime createdTime;
}
```

#### 2.2.2 创建Repository接口

创建 `ProcessSnapshotRepository.java` 接口：

```java
package com.lingflow.repository;

import com.lingflow.entity.ProcessSnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessSnapshotRepository {
    void save(ProcessSnapshot snapshot);
    
    List<ProcessSnapshot> findByProcessDefinitionKey(String processDefinitionKey);
    
    ProcessSnapshot findById(Long id);
    
    ProcessSnapshot findLatestByProcessDefinitionKey(String processDefinitionKey);
    
    void deleteById(Long id);
}
```

#### 2.2.3 创建MyBatis映射文件

创建 `ProcessSnapshotMapper.xml` 映射文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.lingflow.repository.ProcessSnapshotRepository">
    <resultMap id="ProcessSnapshotResultMap" type="com.lingflow.entity.ProcessSnapshot">
        <id property="id" column="id"/>
        <result property="processDefinitionKey" column="process_definition_key"/>
        <result property="snapshotName" column="snapshot_name"/>
        <result property="snapshotVersion" column="snapshot_version"/>
        <result property="bpmnXml" column="bpmn_xml"/>
        <result property="description" column="description"/>
        <result property="creator" column="creator"/>
        <result property="createdTime" column="created_time"/>
    </resultMap>

    <insert id="save" parameterType="com.lingflow.entity.ProcessSnapshot">
        INSERT INTO process_snapshot 
        (process_definition_key, snapshot_name, snapshot_version, bpmn_xml, description, creator, created_time)
        VALUES 
        (#{processDefinitionKey}, #{snapshotName}, #{snapshotVersion}, #{bpmnXml}, #{description}, #{creator}, NOW())
    </insert>

    <select id="findByProcessDefinitionKey" parameterType="string" resultMap="ProcessSnapshotResultMap">
        SELECT * FROM process_snapshot 
        WHERE process_definition_key = #{processDefinitionKey}
        ORDER BY created_time DESC
    </select>

    <select id="findById" parameterType="long" resultMap="ProcessSnapshotResultMap">
        SELECT * FROM process_snapshot WHERE id = #{id}
    </select>

    <select id="findLatestByProcessDefinitionKey" parameterType="string" resultMap="ProcessSnapshotResultMap">
        SELECT * FROM process_snapshot 
        WHERE process_definition_key = #{processDefinitionKey}
        ORDER BY created_time DESC LIMIT 1
    </select>

    <delete id="deleteById" parameterType="long">
        DELETE FROM process_snapshot WHERE id = #{id}
    </delete>
</mapper>
```

#### 2.2.4 修改ProcessService以添加快照功能

在 `ProcessService.java` 中添加快照相关方法：

```java
/**
 * 创建流程快照
 */
@Transactional
public void createProcessSnapshot(String processDefinitionKey, String snapshotName, String description, String creator) {
    // 获取当前最新流程定义的XML
    ProcessDefinition latestDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();

    if (latestDefinition == null) {
        throw new RuntimeException("未找到流程定义: " + processDefinitionKey);
    }

    // 获取XML内容
    InputStream resourceStream = repositoryService.getResourceAsStream(
            latestDefinition.getDeploymentId(),
            latestDefinition.getResourceName());

    String bpmnXml;
    try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = resourceStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        bpmnXml = outputStream.toString(StandardCharsets.UTF_8.name());
    } catch (Exception e) {
        throw new RuntimeException("获取流程XML失败", e);
    }

    // 检查快照名称是否重复
    List<ProcessSnapshot> snapshots = processSnapshotRepository.findByProcessDefinitionKey(processDefinitionKey);
    Optional<ProcessSnapshot> existingSnapshot = snapshots.stream()
            .filter(snapshot -> snapshot.getSnapshotName().equals(snapshotName))
            .findFirst();

    if (existingSnapshot.isPresent()) {
        throw new RuntimeException("快照名称已存在: " + snapshotName);
    }

    // 计算下一个快照版本号
    int nextVersion = 1;
    if (!snapshots.isEmpty()) {
        nextVersion = snapshots.stream()
                .mapToInt(ProcessSnapshot::getSnapshotVersion)
                .max()
                .orElse(0) + 1;
    }

    // 保存快照
    ProcessSnapshot snapshot = new ProcessSnapshot();
    snapshot.setProcessDefinitionKey(processDefinitionKey);
    snapshot.setSnapshotName(snapshotName);
    snapshot.setSnapshotVersion(nextVersion);
    snapshot.setBpmnXml(bpmnXml);
    snapshot.setDescription(description);
    snapshot.setCreator(creator);

    processSnapshotRepository.save(snapshot);
}

/**
 * 获取流程快照列表
 */
public List<ProcessSnapshot> getProcessSnapshots(String processDefinitionKey) {
    return processSnapshotRepository.findByProcessDefinitionKey(processDefinitionKey);
}

/**
 * 回滚到指定快照
 */
@Transactional
public void rollbackToSnapshot(Long snapshotId) {
    ProcessSnapshot snapshot = processSnapshotRepository.findById(snapshotId);
    if (snapshot == null) {
        throw new RuntimeException("未找到快照: " + snapshotId);
    }

    // 重新部署快照中的BPMN XML
    String snapshotName = snapshot.getSnapshotName() + "_rollback_" + System.currentTimeMillis();
    deployProcess(snapshotName, snapshot.getBpmnXml());
}

/**
 * 删除快照
 */
public void deleteSnapshot(Long snapshotId) {
    processSnapshotRepository.deleteById(snapshotId);
}
```

#### 2.2.5 添加快照控制器

创建 `SnapshotController.java` 控制器：

```java
package com.lingflow.controller;

import com.lingflow.dto.Result;
import com.lingflow.entity.ProcessSnapshot;
import com.lingflow.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/snapshot")
public class SnapshotController {

    @Autowired
    private ProcessService processService;

    /**
     * 创建流程快照
     */
    @PostMapping("/create")
    public Result<Void> createSnapshot(@RequestParam String processDefinitionKey,
                                       @RequestParam String snapshotName,
                                       @RequestParam(required = false) String description,
                                       @RequestParam(required = false) String creator) {
        try {
            processService.createProcessSnapshot(processDefinitionKey, snapshotName, description, creator);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取流程快照列表
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
```

### 2.3 前端实现

#### 2.3.1 修改API文件

在 `process.ts` 中添加快照相关API：

```typescript
// 快照相关接口
export const createProcessSnapshot = (data: { 
  processDefinitionKey: string; 
  snapshotName: string; 
  description?: string; 
  creator?: string 
}) => {
  return request.post('/snapshot/create', data)
}

export const getProcessSnapshots = (processDefinitionKey: string) => {
  return request.get(`/snapshot/list/${processDefinitionKey}`)
}

export const rollbackToSnapshot = (snapshotId: number) => {
  return request.post(`/snapshot/rollback/${snapshotId}`)
}

export const deleteSnapshot = (snapshotId: number) => {
  return request.delete(`/snapshot/${snapshotId}`)
}
```

#### 2.3.2 修改流程设计器页面

在 `Designer.vue` 中添加快照功能：

```vue
<template>
  <div class="designer-container">
    <!-- 头部导航栏 -->
    <div class="designer-header">
      <div class="header-left">
        <h2 class="logo">📄 流程设计器</h2>
        <el-input
          v-model="processName"
          placeholder="请输入流程名称"
          class="process-name-input"
        />
      </div>
      <div class="header-right">
        <div class="zoom-controls">
          <el-button size="small" @click="zoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button size="small" @click="zoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button size="small" @click="resetZoom" title="重置缩放">
            <el-icon><Refresh /></el-icon>
          </el-button>
          <span class="zoom-display">{{ Math.round(zoomLevel * 100) }}%</span>
        </div>
        <div class="operation-buttons">
          <!-- 快照按钮组 -->
          <el-dropdown split-button type="default" @click="showSnapshotDialog" @command="handleSnapshotCommand">
            <el-icon><Document /></el-icon>
            快照
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="listSnapshots" icon="Document">
                  查看快照
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-button @click="exportXML" plain>
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button type="primary" @click="deploy" class="publish-btn">
            <el-icon><Upload /></el-icon>
            发布流程
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 主体内容区域 -->
    <div class="main-content">

      
      <!-- 中间画布区域 -->
      <div class="canvas-wrapper">
        <div class="canvas-container" ref="canvasRef"></div>
      </div>
      
      <!-- 右侧属性面板 -->
      <div class="right-panel">
        <h3>属性面板</h3>
        <el-form v-if="selectedElement" label-position="top">
          <el-form-item label="元素类型">
            <el-input v-model="selectedElement.type" readonly></el-input>
          </el-form-item>
          <el-form-item label="元素ID">
            <el-input v-model="selectedElement.id" readonly></el-input>
          </el-form-item>
          <el-form-item label="元素名称">
            <el-input v-model="selectedElement.name" @input="updateElementName"></el-input>
          </el-form-item>
          <el-form-item label="分配人">
            <el-select v-model="selectedElement.assignee" placeholder="请选择分配人">
              <el-option label="张三" value="zhangsan"></el-option>
              <el-option label="李四" value="lisi"></el-option>
              <el-option label="王五" value="wangwu"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="selectedElement.priority" placeholder="请选择优先级">
              <el-option label="高" value="high"></el-option>
              <el-option label="中" value="medium"></el-option>
              <el-option label="低" value="low"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input
              v-model="selectedElement.description"
              type="textarea"
              :rows="4"
              placeholder="请输入描述信息"
            ></el-input>
          </el-form-item>
        </el-form>
        <div v-else class="no-selection">
          请选择一个元素
        </div>
      </div>
    </div>
    
    <!-- 快照对话框 -->
    <el-dialog v-model="snapshotDialogVisible" title="流程快照管理" width="80%" top="5vh">
      <div class="snapshot-toolbar">
        <el-button type="primary" @click="showCreateSnapshotDialog">
          <el-icon><Plus /></el-icon>
          创建快照
        </el-button>
      </div>
      
      <el-table 
        :data="snapshots" 
        stripe 
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="snapshotName" label="快照名称" width="200"></el-table-column>
        <el-table-column prop="snapshotVersion" label="版本" width="100"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="creator" label="创建人" width="120"></el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="rollbackToSnapshot(row.id)">
              回滚
            </el-button>
            <el-button size="small" type="danger" @click="deleteSnapshot(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 创建快照对话框 -->
    <el-dialog v-model="createSnapshotDialogVisible" title="创建快照" width="500px">
      <el-form :model="snapshotForm" label-width="100px">
        <el-form-item label="快照名称">
          <el-input v-model="snapshotForm.snapshotName" placeholder="请输入快照名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="snapshotForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入快照描述（可选）"
          ></el-input>
        </el-form-item>
        <el-form-item label="创建人">
          <el-input v-model="snapshotForm.creator" placeholder="请输入创建人姓名"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createSnapshotDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createSnapshot">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { ZoomIn, ZoomOut, Download, Upload, Refresh, Document, Plus } from '@element-plus/icons-vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { deployProcess, getProcessDefinitionXml, createProcessSnapshot, getProcessSnapshots, rollbackToSnapshot, deleteSnapshot } from '@/api/process'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
let modeler: any = null

const route = useRoute()

// 获取URL参数
const queryParams = route.query
if (queryParams.name) {
  processName.value = decodeURIComponent(queryParams.name as string)
}

// 如果是编辑模式且提供了流程定义ID，可以加载对应的流程图
if (queryParams.id) {
  // 在编辑模式下，加载现有的流程定义
  loadExistingProcess(queryParams.id as string)
}

// 加载现有流程定义
const loadExistingProcess = async (processDefinitionId: string) => {
  try {
    const response = await getProcessDefinitionXml(processDefinitionId)
    const xml = response.data.data.bpmnXml
    
    if (modeler) {
      await modeler.importXML(xml)
      
      // 设置流程名称
      if (response.data.data.name) {
        processName.value = response.data.data.name
      }
      
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      
      // 更新缩放级别
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    }
  } catch (error) {
    console.error('Failed to load existing process:', error)
    ElMessage.error('加载现有流程失败')
    
    // 如果加载失败，仍使用初始XML
    if (modeler) {
      await modeler.importXML(initialXML)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    }
  }
}

// 缩放级别
const zoomLevel = ref(1)

// 选中的元素
const selectedElement = ref<any>(null)

// 初始空白流程XML
const initialXML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_1"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:task id="Task_1" name="任务">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:task>
    <bpmn:endEvent id="EndEvent_1" name="结束">
      <bpmn:incoming>Flow_2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_1" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="EndEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_1" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="79" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_Task_1" bpmnElement="Task_1">
        <dc:Bounds x="270" y="60" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="_BPMNShape_EndEvent_1" bpmnElement="EndEvent_1">
        <dc:Bounds x="430" y="79" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="215" y="97" />
        <di:waypoint x="270" y="97" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
        <di:waypoint x="370" y="97" />
        <di:waypoint x="430" y="97" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

// 快照相关
const snapshotDialogVisible = ref(false)
const createSnapshotDialogVisible = ref(false)
const snapshots = ref([])
const snapshotForm = ref({
  snapshotName: '',
  description: '',
  creator: ''
})

onMounted(async () => {
  if (canvasRef.value) {
    modeler = new BpmnModeler({
      container: canvasRef.value,
      keyboard: {
        bindTo: window
      }
    })

    // 监听元素选择事件
    const eventBus = modeler.get('eventBus')
    const selection = modeler.get('selection')
    
    eventBus.on('selection.changed', ({ newSelection }) => {
      if (newSelection && newSelection.length > 0) {
        const element = newSelection[0]
        selectedElement.value = {
          id: element.id,
          type: element.type,
          name: element.businessObject.name || '',
          assignee: '',
          priority: '',
          description: ''
        }
      } else {
        selectedElement.value = null
      }
    })

    try {
      await modeler.importXML(initialXML)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      
      // 更新缩放级别
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    } catch (error) {
      ElMessage.error('初始化流程图失败')
      console.error(error)
    }
  }
})

onBeforeUnmount(() => {
  if (modeler) {
    modeler.destroy()
  }
})

// 缩放功能
const zoomIn = () => {
  const canvas = modeler.get('canvas')
  const currentViewbox = canvas.viewbox()
  const newScale = currentViewbox.scale * 1.1
  canvas.zoom(newScale)
  zoomLevel.value = newScale
}

const zoomOut = () => {
  const canvas = modeler.get('canvas')
  const currentViewbox = canvas.viewbox()
  const newScale = currentViewbox.scale * 0.9
  canvas.zoom(newScale)
  zoomLevel.value = newScale
}

const resetZoom = () => {
  const canvas = modeler.get('canvas')
  canvas.zoom('fit-viewport')
  const currentViewbox = canvas.viewbox()
  zoomLevel.value = currentViewbox.scale
}


// 更新元素名称
const updateElementName = () => {
  if (selectedElement.value && modeler) {
    const modeling = modeler.get('modeling')
    const elementRegistry = modeler.get('elementRegistry')
    const element = elementRegistry.get(selectedElement.value.id)
    
    if (element) {
      modeling.updateProperties(element, {
        name: selectedElement.value.name
      })
    }
  }
}

const deploy = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }

  try {
    const { xml } = await modeler.saveXML({ format: true })
    await deployProcess({
      name: processName.value,
      xml: xml as string
    })
    ElMessage.success('流程发布成功')
  } catch (error) {
    ElMessage.error('流程发布失败')
    console.error(error)
  }
}

const exportXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${processName.value || 'process'}.bpmn`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  }
}

const saveXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true })
    console.log('Saved XML:', xml)
    ElMessage.success('XML已保存到控制台')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  }
}

// 显示快照对话框
const showSnapshotDialog = async () => {
  if (!processName.value) {
    ElMessage.warning('请先设置流程名称');
    return;
  }
  
  try {
    const response = await getProcessSnapshots(processName.value);
    snapshots.value = response.data.data;
    snapshotDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取快照列表失败');
    console.error(error);
  }
}

// 显示创建快照对话框
const showCreateSnapshotDialog = () => {
  snapshotForm.value = {
    snapshotName: '',
    description: '',
    creator: ''
  };
  createSnapshotDialogVisible.value = true;
}

// 创建快照
const createSnapshot = async () => {
  if (!snapshotForm.value.snapshotName) {
    ElMessage.warning('请输入快照名称');
    return;
  }
  
  if (!processName.value) {
    ElMessage.warning('请先设置流程名称');
    return;
  }

  try {
    await createProcessSnapshot({
      processDefinitionKey: processName.value,
      snapshotName: snapshotForm.value.snapshotName,
      description: snapshotForm.value.description,
      creator: snapshotForm.value.creator
    });
    
    ElMessage.success('快照创建成功');
    createSnapshotDialogVisible.value = false;
    
    // 刷新快照列表
    const response = await getProcessSnapshots(processName.value);
    snapshots.value = response.data.data;
  } catch (error) {
    ElMessage.error(`创建快照失败: ${error}`);
    console.error(error);
  }
}

// 回滚到指定快照
const rollbackToSnapshot = async (snapshotId: number) => {
  try {
    await ElMessageBox.confirm(
      '确认要回滚到此快照吗？此操作不可逆！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await rollbackToSnapshot(snapshotId);
    ElMessage.success('回滚成功');
    snapshotDialogVisible.value = false;
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`回滚失败: ${error}`);
      console.error(error);
    }
  }
}

// 删除快照
const deleteSnapshot = async (snapshotId: number) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除此快照吗？此操作不可逆！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await deleteSnapshot(snapshotId);
    ElMessage.success('删除成功');
    
    // 刷新快照列表
    if (processName.value) {
      const response = await getProcessSnapshots(processName.value);
      snapshots.value = response.data.data;
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`删除失败: ${error}`);
      console.error(error);
    }
  }
}

// 处理快照命令
const handleSnapshotCommand = async (command: string) => {
  if (command === 'listSnapshots') {
    await showSnapshotDialog();
  }
}
</script>

<style scoped>
.designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.designer-header {
  background: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  animation: pulse 2s infinite;
}

.process-name-input {
  width: 300px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-right: 15px;
}

.zoom-display {
  margin-left: 5px;
  font-size: 14px;
  color: #606266;
}

.operation-buttons {
  display: flex;
  gap: 10px;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.canvas-wrapper {
  flex: 2;
  display: flex;
  flex-direction: column;
  background-image: radial-gradient(circle, #cbd5e0 1px, transparent 1px);
  background-size: 20px 20px;
}

.right-panel {
  width: 300px;
  background: white;
  padding: 20px;
  border-left: 1px solid #e4e7ed;
  overflow-y: auto;
  animation: slideInRight 0.3s ease;
}

.canvas-container {
  flex: 1;
  background: white;
  border-radius: 8px;
  margin: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.right-panel {
  width: 300px;
  background: white;
  padding: 20px;
  border-left: 1px solid #e4e7ed;
  overflow-y: auto;
  animation: slideInRight 0.3s ease;
}

.right-panel h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.no-selection {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-style: italic;
}

/* 快照对话框样式 */
.snapshot-toolbar {
  margin-bottom: 20px;
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
```

## 3. 方案优缺点分析

### 3.1 优点

1. **数据完整性**：快照保存了完整的BPMN XML，确保流程定义在任何时间点都能完整恢复
2. **易于实现**：采用独立的快照表存储，不影响现有流程引擎的正常运行
3. **灵活的回滚机制**：支持回滚到任意历史版本，满足不同场景需求
4. **用户友好**：提供直观的快照管理和操作界面
5. **版本追踪**：每个快照都有版本号、创建时间和描述信息，方便管理

### 3.2 缺点

1. **存储开销**：每次快照都会保存完整的XML，对于频繁修改的流程可能占用较多存储空间
2. **性能影响**：大量快照可能导致查询变慢，需要定期清理旧快照
3. **并发问题**：多个用户同时操作可能存在冲突，需要加锁机制保证一致性

### 3.3 扩展性考虑

1. **存储优化**：可以考虑只保存差异部分，减少存储空间占用
2. **自动化策略**：可增加自动创建快照的功能，如每次发布自动保存快照
3. **权限控制**：可以增加快照访问权限控制，不同角色只能访问特定快照
4. **压缩存储**：对快照XML进行压缩存储，节省空间

### 3.4 性能优化建议

1. **索引优化**：为关键字段建立适当索引，提升查询性能
2. **分页查询**：当快照数量较多时，采用分页方式展示
3. **缓存机制**：对常用快照数据进行缓存，减少数据库访问
4. **定期清理**：提供快照清理功能，删除过期或无用的快照

## 4. 实施计划

1. 创建数据库表和相关实体类
2. 实现后端服务和控制器
3. 修改前端API和界面
4. 测试功能完整性
5. 部署和上线