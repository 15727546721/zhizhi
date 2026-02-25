<template>
  <div class="post-content-editor">
    <div class="editor-toolbar">
      <el-segmented v-model="innerMode" :options="modeOptions" size="small" @change="handleModeChange" />
      <slot name="extra-actions" />
    </div>

    <div class="editor-pane">
      <el-input
        v-if="innerMode === 'text'"
        v-model="innerValue"
        type="textarea"
        :rows="rows"
        :maxlength="maxlength"
        :show-word-limit="Boolean(maxlength)"
        :placeholder="placeholder"
      />
      <md-editor
        v-else
        v-model="innerValue"
        :toolbars="toolbars"
        :preview="preview"
        :showCodeRowNumber="true"
        :auto-detect-code="true"
        @onUploadImg="handleUploadImg"
        :style="{ height: editorHeight, width: '100%' }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

// 类型定义
type EditorMode = 'text' | 'markdown'

interface Props {
  modelValue?: string
  mode?: EditorMode
  rows?: number
  maxlength?: number
  placeholder?: string
  preview?: boolean
  editorHeight?: string
  uploadImages?: ((files: File[]) => Promise<string[]>) | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  mode: 'text',
  rows: 15,
  maxlength: 5000,
  placeholder: '请输入内容',
  preview: false,
  editorHeight: '420px',
  uploadImages: null
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:mode': [value: EditorMode]
  change: [value: string]
}>()

const innerValue = ref(props.modelValue)
const innerMode = ref<EditorMode>(props.mode === 'markdown' ? 'markdown' : 'text')

watch(
  () => props.modelValue,
  (v) => {
    if (v !== innerValue.value) innerValue.value = v
  }
)

watch(innerValue, (v) => {
  emit('update:modelValue', v)
  emit('change', v)
})

watch(
  () => props.mode,
  (v) => {
    if (v && v !== innerMode.value) innerMode.value = v
  }
)

const modeOptions = computed(() => [
  { label: '纯文本', value: 'text' },
  { label: 'Markdown', value: 'markdown' }
])

const handleModeChange = (val: EditorMode): void => {
  emit('update:mode', val)
}

const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  'title',
  'quote',
  'unorderedList',
  'orderedList',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'preview',
  'catalog',
  'fullscreen'
]

const handleUploadImg = async (
  files: File[],
  callback: (urls: string[]) => void
): Promise<void> => {
  try {
    if (typeof props.uploadImages === 'function') {
      const urls = await props.uploadImages(files)
      callback(urls)
      return
    }
    // 默认兜底：使用 blob 预览
    const res = await Promise.all(files.map((f) => Promise.resolve(URL.createObjectURL(f))))
    callback(res)
  } catch (e) {
    ElMessage.error('图片上传失败')
  }
}
</script>

<style scoped>
.post-content-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  box-sizing: border-box;
}

.editor-pane {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.editor-pane :deep(.el-input) {
  width: 100%;
}

.editor-pane :deep(.el-textarea) {
  width: 100%;
}

.editor-pane :deep(.el-textarea__inner) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-pane :deep(.md-editor) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-pane :deep(.md-editor-wrapper) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-pane :deep(.md-editor-content) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-pane :deep(.md-editor-textarea) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-pane :deep(.md-editor-preview) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}
</style>

<style>
/* 全局样式：确保在 el-form-item 中宽度正确 */
.el-form-item__content .post-content-editor {
  width: 100%;
  max-width: 100%;
}
</style>


