<template>
  <div class="process-comment-container">
    <!-- 添加评论表单 -->
    <el-card class="comment-form-card">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="请输入评论内容..."
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button
          type="primary"
          @click="submitComment"
          :disabled="!newComment.trim()"
          :loading="submitting"
        >
          发表评论
        </el-button>
        <el-button @click="cancelComment" v-if="newComment">取消</el-button>
      </div>
    </el-card>

    <!-- 评论统计 -->
    <div class="comment-stats" v-if="statistics.totalCommentCount > 0">
      <el-descriptions :column="4" size="small" border>
        <el-descriptions-item label="总评论数">
          {{ statistics.totalCommentCount }}
        </el-descriptions-item>
        <el-descriptions-item label="流程评论">
          {{ statistics.processCommentCount }}
        </el-descriptions-item>
        <el-descriptions-item label="任务评论">
          {{ statistics.taskCommentCount }}
        </el-descriptions-item>
        <el-descriptions-item label="参与人数">
          {{ statistics.participantCount }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="comments.length === 0" class="empty-state">
        <el-empty description="暂无评论，快来发表第一条评论吧" />
      </div>

      <div v-else>
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
          :class="{ 'system-comment': comment.type === 'SYSTEM' }"
        >
          <div class="comment-avatar">
            <el-avatar v-if="comment.type === 'SYSTEM'" :size="40" :icon="UserFilled" />
            <el-avatar v-else :size="40">
              {{ comment.userId ? comment.userId.charAt(0).toUpperCase() : '?' }}
            </el-avatar>
          </div>

          <div class="comment-content-wrapper">
            <div class="comment-header">
              <span class="comment-user">
                {{ comment.type === 'SYSTEM' ? '系统' : (comment.userId || '未知用户') }}
              </span>
              <el-tag v-if="comment.type === 'SYSTEM'" size="small" type="info">系统</el-tag>
              <el-tag v-else-if="comment.type === 'TASK'" size="small" type="warning">任务</el-tag>
              <el-tag v-else size="small">流程</el-tag>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>

            <div class="comment-text">{{ comment.content }}</div>

            <div class="comment-actions" v-if="comment.type !== 'SYSTEM' && canEdit(comment)">
              <el-button
                size="small"
                link
                @click="editComment(comment)"
                v-if="!editing[comment.id]"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                link
                type="primary"
                @click="saveEdit(comment)"
                v-else
              >
                保存
              </el-button>
              <el-button
                size="small"
                link
                @click="cancelEdit(comment)"
                v-if="editing[comment.id]"
              >
                取消
              </el-button>
              <el-button
                size="small"
                link
                type="danger"
                @click="deleteComment(comment)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import {
  getAllCommentsForProcess,
  addProcessComment,
  addSystemComment,
  updateComment,
  deleteComment as deleteCommentApi,
  getCommentStatistics
} from '@/api/extended'

interface Props {
  processInstanceId: string
  userId?: string
  readOnly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userId: 'user001',
  readOnly: false
})

const emit = defineEmits(['comment-added'])

const comments = ref<any[]>([])
const newComment = ref('')
const submitting = ref(false)
const editing = ref<Record<string, boolean>>({})
const editContent = ref<Record<string, string>>({})
const statistics = ref({
  processCommentCount: 0,
  taskCommentCount: 0,
  totalCommentCount: 0,
  participantCount: 0
})

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await getAllCommentsForProcess(props.processInstanceId)
    comments.value = response.data || []
  } catch (error) {
    ElMessage.error('获取评论失败')
    console.error(error)
  }
}

// 获取评论统计
const fetchStatistics = async () => {
  try {
    const response = await getCommentStatistics(props.processInstanceId)
    statistics.value = response.data || statistics.value
  } catch (error) {
    console.error('获取评论统计失败', error)
  }
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    return
  }

  submitting.value = true
  try {
    const response = await addProcessComment({
      processInstanceId: props.processInstanceId,
      userId: props.userId,
      content: newComment.value.trim()
    })

    ElMessage.success('评论发表成功')
    newComment.value = ''

    // 刷新列表
    await fetchComments()
    await fetchStatistics()

    // 触发事件
    emit('comment-added', response.data)
  } catch (error) {
    ElMessage.error('评论发表失败')
    console.error(error)
  } finally {
    submitting.value = false
  }
}

// 取消评论
const cancelComment = () => {
  newComment.value = ''
}

// 判断是否可以编辑
const canEdit = (comment: any) => {
  return props.readOnly === false && comment.userId === props.userId
}

// 编辑评论
const editComment = (comment: any) => {
  editing.value[comment.id] = true
  editContent.value[comment.id] = comment.content
}

// 保存编辑
const saveEdit = async (comment: any) => {
  const content = editContent.value[comment.id]
  if (!content || !content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  try {
    await updateComment({
      commentId: comment.id,
      userId: props.userId,
      newContent: content.trim()
    })

    ElMessage.success('评论更新成功')
    editing.value[comment.id] = false

    // 刷新列表
    await fetchComments()
  } catch (error) {
    ElMessage.error('评论更新失败')
    console.error(error)
  }
}

// 取消编辑
const cancelEdit = (comment: any) => {
  editing.value[comment.id] = false
  editContent.value[comment.id] = ''
}

// 删除评论
const deleteComment = async (comment: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteCommentApi(comment.id, props.userId)
    ElMessage.success('评论删除成功')

    // 刷新列表
    await fetchComments()
    await fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('评论删除失败')
      console.error(error)
    }
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

onMounted(() => {
  fetchComments()
  fetchStatistics()
})

// 暴露方法供父组件调用
defineExpose({
  refresh: fetchComments,
  addSystemComment: async (content: string) => {
    try {
      await addSystemComment({
        processInstanceId: props.processInstanceId,
        content
      })
      await fetchComments()
      await fetchStatistics()
    } catch (error) {
      console.error('添加系统评论失败', error)
    }
  }
})
</script>

<style scoped>
.process-comment-container {
  padding: 20px 0;
}

.comment-form-card {
  margin-bottom: 20px;
}

.form-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.comment-stats {
  margin-bottom: 20px;
}

.comment-list {
  min-height: 200px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.system-comment {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content-wrapper {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 500;
  color: #303133;
}

.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 8px;
}
</style>
