<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="480px"
    :close-on-click-modal="false"
    @closed="resetForm"
  >
    <!-- 目标信息 -->
    <div class="target-info" v-if="targetInfo">
      <div class="target-type">
        <el-tag :type="targetTypeTag" size="small">{{ targetTypeName }}</el-tag>
      </div>
      <div class="target-content">{{ targetInfo }}</div>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <!-- 举报原因 -->
      <el-form-item label="举报原因" prop="reason">
        <el-radio-group v-model="form.reason" class="reason-group">
          <el-radio
            v-for="option in reasonOptions"
            :key="option.value"
            :label="option.value"
            class="reason-item"
          >
            {{ option.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 详细说明 -->
      <el-form-item label="详细说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述违规情况（选填，最多500字）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 截图证据 -->
      <el-form-item label="截图证据（选填）">
        <el-upload
          v-model:file-list="fileList"
          action="/api/file/upload"
          list-type="picture-card"
          :limit="3"
          :on-success="handleUploadSuccess"
          :on-remove="handleUploadRemove"
          :before-upload="beforeUpload"
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="upload-tip">最多上传3张图片，单张不超过5MB</div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, defineExpose } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadRawFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitReport, ReportReasonOptions, ReportTargetType } from '@/api/report'

// ==================== Types ====================

interface ReportForm {
  reason: number | null
  description: string
}

interface UploadResponse {
  code: number
  data?: string
  info?: string
}

// ==================== Props & Emits ====================

const emit = defineEmits<{
  success: []
}>()

// ==================== 响应式数据 ====================

const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance | null>(null)
const fileList = ref<UploadFile[]>([])

// 举报目标
const targetType = ref(1)
const targetId = ref<number | null>(null)
const targetInfo = ref('')

// 表单数据
const form = ref<ReportForm>({
  reason: null,
  description: ''
})

// 表单验证规则
const rules: FormRules<ReportForm> = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ]
}

// 举报原因选项
const reasonOptions = ReportReasonOptions

// ==================== 计算属性 ====================

const dialogTitle = computed(() => {
  const typeNames: Record<number, string> = {
    [ReportTargetType.POST]: '举报帖子',
    [ReportTargetType.COMMENT]: '举报评论',
    [ReportTargetType.USER]: '举报用户'
  }
  return typeNames[targetType.value] || '举报'
})

const targetTypeName = computed(() => {
  const names: Record<number, string> = {
    [ReportTargetType.POST]: '帖子',
    [ReportTargetType.COMMENT]: '评论',
    [ReportTargetType.USER]: '用户'
  }
  return names[targetType.value] || '内容'
})

const targetTypeTag = computed(() => {
  const types: Record<number, string> = {
    [ReportTargetType.POST]: 'primary',
    [ReportTargetType.COMMENT]: 'success',
    [ReportTargetType.USER]: 'warning'
  }
  return types[targetType.value] || 'info'
})

// ==================== 方法 ====================

/**
 * 打开举报弹窗
 * @param type - 目标类型：1-帖子 2-评论 3-用户
 * @param id - 目标ID
 * @param info - 目标信息（标题/内容/用户名）
 */
const open = (type: number, id: number, info = '') => {
  targetType.value = type
  targetId.value = id
  targetInfo.value = info
  visible.value = true
}

/**
 * 重置表单
 */
const resetForm = () => {
  form.value = {
    reason: null,
    description: ''
  }
  fileList.value = []
  formRef.value?.resetFields()
}

/**
 * 提交举报
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    if (!targetId.value || !form.value.reason) return
    
    submitting.value = true
    
    const data = {
      targetType: targetType.value,
      targetId: targetId.value,
      reason: form.value.reason,
      description: form.value.description,
      evidenceUrls: fileList.value
        .map(f => f.url || (f.response as UploadResponse)?.data)
        .filter((url): url is string => Boolean(url))
    }
    
    await submitReport(data)
    
    ElMessage.success('举报提交成功，我们会尽快处理')
    visible.value = false
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error((error as Error)?.message || '提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 上传成功
 */
const handleUploadSuccess = (response: UploadResponse, file: UploadFile) => {
  if (response.code === 20000) {
    file.url = response.data
  } else {
    ElMessage.error(response.info || '上传失败')
    // 移除失败的文件
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

/**
 * 移除文件
 */
const handleUploadRemove = (file: UploadFile) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

/**
 * 上传前校验
 */
const beforeUpload = (file: UploadRawFile) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// ==================== 暴露方法 ====================

defineExpose({
  open
})
</script>

<style scoped>
.target-info {
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.target-type {
  margin-bottom: 8px;
}

.target-content {
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.reason-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.reason-item {
  margin-right: 0 !important;
  width: calc(50% - 6px);
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
}
</style>
