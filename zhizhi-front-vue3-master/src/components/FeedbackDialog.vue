<template>
  <el-dialog
    v-model="visible"
    title="意见反馈"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="反馈类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio-button
            v-for="option in FeedbackTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="简要描述您的问题或建议"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="详细描述" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请详细描述您遇到的问题或建议..."
          :rows="5"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="联系方式（选填）">
        <el-input
          v-model="form.contact"
          placeholder="邮箱或微信，方便我们回复您"
          maxlength="50"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        提交反馈
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { submitFeedback, FeedbackTypeOptions, FeedbackType } from '@/api/feedback'

interface Props {
  modelValue?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance | null>(null)

interface FeedbackForm {
  type: number
  title: string
  content: string
  contact: string
}

const form = reactive<FeedbackForm>({
  type: FeedbackType.SUGGESTION,
  title: '',
  content: '',
  contact: ''
})

const rules: FormRules<FeedbackForm> = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  content: [{ required: true, message: '请填写详细描述', trigger: 'blur' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
  resetForm()
}

const resetForm = () => {
  form.type = FeedbackType.SUGGESTION
  form.title = ''
  form.content = ''
  form.contact = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    const res = await submitFeedback({
      type: form.type,
      title: form.title,
      content: form.content,
      contact: form.contact || undefined
    })

    if (res.code === 20000) {
      ElMessage.success(res.info || '反馈提交成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(res.info || '提交失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交反馈失败:', error)
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:deep(.el-radio-button__inner) {
  padding: 8px 16px;
}
</style>
