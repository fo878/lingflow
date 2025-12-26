<template>
  <div class="designer-container">
    <div class="toolbar">
      <el-input
        v-model="processName"
        placeholder="请输入流程名称"
        style="width: 200px; margin-right: 10px"
      />
      <el-button type="primary" @click="deploy">
        <el-icon><Upload /></el-icon>
        发布流程
      </el-button>
      <el-button @click="exportXML">
        <el-icon><Download /></el-icon>
        导出XML
      </el-button>
      <el-button @click="saveXML">
        <el-icon><DocumentCopy /></el-icon>
        保存XML
      </el-button>
    </div>
    <div class="canvas-container" ref="canvasRef"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { deployProcess } from '@/api/process'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
let modeler: any = null

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
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

onMounted(async () => {
  if (canvasRef.value) {
    modeler = new BpmnModeler({
      container: canvasRef.value,
      keyboard: {
        bindTo: window
      }
    })

    try {
      await modeler.importXML(initialXML)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
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
</script>

<style scoped>
.designer-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.toolbar {
  background: #fff;
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 10px;
  border-radius: 4px;
}

.canvas-container {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}
</style>
