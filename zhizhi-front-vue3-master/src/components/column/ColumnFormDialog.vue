<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑专栏' : '创建专栏'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <el-form-item label="专栏名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入专栏名称(2-50字符)"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="专栏描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入专栏描述(最多200字符)"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="封面图" prop="coverUrl">
        <div class="cover-upload">
          <el-upload
            class="cover-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            name="files"
          >
            <img v-if="formData.coverUrl" :src="formData.coverUrl" class="cover-image" />
            <div v-else class="cover-placeholder">
              <el-icon :size="40"><Plus /></el-icon>
              <div class="upload-text">上传封面</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x400px, 支持JPG/PNG, 最大2MB</div>
        </div>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">草稿</el-radio>
          <el-radio :label="1">发布</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import type { ColumnCreateDTO, ColumnUpdateDTO, ColumnDetailVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import { useUserStore } from '@/stores/module/user'

interface Props {
  modelValue: boolean
  column?: ColumnDetailVO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const columnStore = useColumnStore()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!props.column)

const formData = reactive<ColumnCreateDTO>({
  name: '',
  description: '',
  coverUrl: '',
  status: 1,
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入专栏名称', trigger: 'blur' },
    { min: 2, max: 50, message: '专栏名称长度在2-50字符之间', trigger: 'blur' },
  ],
  description: [{ max: 200, message: '描述最多200字符', trigger: 'blur' }],
}

const uploadAction = computed(() => {
  return '/api/file/upload'
})

const uploadHeaders = computed(() => {
  const token = userStore.token
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 监听column变化,填充表单
watch(
  () => props.column,
  (newColumn) => {
    if (newColumn) {
      formData.name = newColumn.name
      formData.description = newColumn.description || ''
      formData.coverUrl = newColumn.coverUrl || ''
      formData.status = newColumn.status
    } else {
      // 重置为默认值
      formData.name = ''
      formData.description = ''
      formData.coverUrl = ''
      formData.status = 1
    }
  },
  { immediate: true }
)

// 监听对话框打开/关闭
watch(
  () => props.modelValue,
  (newValue) => {
    if (!newValue) {
      // 对话框关闭时重置表单
      formRef.value?.resetFields()
      if (!props.column) {
        formData.name = ''
        formData.description = ''
        formData.coverUrl = ''
        formData.status = 1
      }
    }
  }
)

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 20000 && response.data && response.data.length > 0) {
    formData.coverUrl = response.data[0] // 后端返回的是数组，取第一个
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.info || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败,请重试')
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }

  try {
    const valid = await formRef.value.validate()
    
    if (!valid) {
      return
    }

    loading.value = true

    if (isEdit.value && props.column) {
      const updateData: ColumnUpdateDTO = {
        name: formData.name,
        description: formData.description,
        coverUrl: formData.coverUrl,
        status: formData.status,
      }
      await columnStore.updateColumn(props.column.id, updateData)
    } else {
      await columnStore.createColumn(formData)
    }

    emit('success')
    handleClose()
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
.cover-upload {
  width: 100%;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.cover-image {
  width: 400px;
  height: 200px;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 400px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  background: #fafafa;

  .upload-text {
    margin-top: 8px;
    font-size: 14px;
  }
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
