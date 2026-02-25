<template>
  <div class="editor-page">
    <!-- 顶部操作栏 -->
    <div class="editor-header">
      <div class="header-left">
        <router-link to="/" class="back-home">
          <el-icon>
            <ArrowLeft />
          </el-icon>
          返回首页
        </router-link>
        <div class="divider"></div>
        <el-input
          v-model="articleData.title"
          placeholder="输入文章标题..."
          :maxlength="100"
          clearable
          class="title-input"
        />
      </div>
      <div class="header-right">
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="openPublishDialog">发布文章</el-button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-main">
      <div class="editor-container">
        <md-editor
          v-model="articleData.content"
          :toolbars="toolbars"
          @onChange="handleContentChange"
          @onUploadImg="handleUploadImg"
          :style="{ height: 'calc(100vh - 64px)' }"
        />
      </div>
    </div>

    <!-- 发布文章弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="发布文章"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="publish-form">
        <!-- 封面上传 -->
        <div class="form-item">
          <div class="form-label">封面：</div>
          <div class="cover-upload">
            <el-upload
              class="cover-uploader"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleCoverChange"
              accept="image/jpeg,image/png,image/gif"
            >
              <div class="upload-area" v-if="!articleData.coverUrl">
                <el-icon class="upload-icon">
                  <Plus />
                </el-icon>
                <div class="upload-text">上传</div>
                <div class="upload-hint">建议尺寸：213*128px (封面仅展示在首页)</div>
              </div>
              <div v-else class="preview-wrapper">
                <el-image :src="articleData.coverUrl" class="cover-preview" fit="cover">
                  <template #error>
                    <div class="image-error">
                      <el-icon>
                        <Picture />
                      </el-icon>
                      <span>加载失败</span>
                    </div>
                  </template>
                </el-image>
                <div class="preview-mask">
                  <div class="preview-actions">
                    <el-icon class="preview-icon" @click.stop="previewImage">
                      <ZoomIn />
                    </el-icon>
                    <el-icon class="preview-icon" @click.stop="removeCover">
                      <Delete />
                    </el-icon>
                  </div>
                </div>
              </div>
            </el-upload>
          </div>
        </div>

        <!-- 文章描述 -->
        <div class="form-item">
          <div class="form-label">描述：</div>
          <el-input
            v-model="articleData.description"
            type="textarea"
            :rows="3"
            placeholder="请简单介绍文章的内容"
            :maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 文章标签 -->
        <div class="form-item">
          <div class="form-label required">标签：</div>
          <el-select
            v-model="articleData.tagIds"
            multiple
            filterable
            :max-collapse-tags="3"
            placeholder="请选择标签（最多3个）"
            style="width: 100%"
            :multiple-limit="3"
          >
            <div class="tags-grid">
              <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id">
                <div class="tag-item">{{ tag.name }}</div>
              </el-option>
            </div>
          </el-select>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPublish">发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加图片预览组件 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="[articleData.coverUrl]"
      :initial-index="0"
      :hide-on-click-modal="true"
      :teleported="true"
      :z-index="2001"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { reactive, ref, onBeforeUnmount, onMounted } from 'vue'
import { ArrowLeft, Plus, Picture, Delete, ZoomIn } from '@element-plus/icons-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { ElMessage } from 'element-plus'
import { ElImageViewer } from 'element-plus'
import { getTagPage } from '@/api/tag.js'
// import { getCategoryPage } from '@/api/category.js' // 已移除，使用标签代替分类
import { useRouter } from 'vue-router'
import { createArticle, saveDraft } from '@/api/article.js'
import { uploadImage } from '@/api/file.js'
import { uploadPostCover } from '@/api/post'
import { useUserStore } from '@/stores/module/user.js'
import { validateApiResponse } from '@/utils/typeGuards'

defineOptions({
  name: 'ArticleEditor'
})

// 发布弹窗相关数据
const createDialogVisible = ref(false)

const articleData = reactive({
  id: null,
  title: '',
  description: '',
  content: '',
  coverUrl: '',
  // categoryId: null, // 已移除
  tagIds: [] // 标签ID列表
})

// 修改分类数据结构
const categories = []

// 修改标签数据结构
const allTags = [].map((tag) => ({
  id: tag.id,
  name: tag.name
}))

// 编辑器工具栏配置
const toolbars = [
  'bold',
  'underline',
  'italic',
  '-',
  'title',
  'strikeThrough',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  '-',
  'revoke',
  'next',
  'save',
  '=',
  'pageFullscreen',
  'fullscreen',
  'preview',
  'htmlPreview',
  'catalog'
]

// 内容变化处理
const handleContentChange = (content) => {
  articleData.content = content
}

// 图片上传处理
const handleUploadImg = async (files, callback) => {
  try {
    const urls = await Promise.all(
      files.map(async (file) => {
        const response = await uploadImage(file)
        if (response.code === 20000 && response.data && response.data.length > 0) {
          return response.data[0] // 返回上传后的URL
        }
        throw new Error(response.info || '上传失败')
      })
    )
    callback(urls)
  } catch (error) {
    ElMessage.error(error.message || '图片上传失败')
    callback([]) // 上传失败时返回空数组
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!articleData.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!articleData.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  try {
    const response = await saveDraft({
      title: articleData.title,
      content: articleData.content,
    })

    if (response.code === 20000) {
      ElMessage.success('草稿保存成功')
      // 保存成功后跳转到草稿箱
      router.push('/draft')
    } else {
      ElMessage.error(response.info || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存草稿失败，请稍后重试')
  }
}

// 封面上传前的验证
const beforeCoverUpload = (file) => {
  const isImage = /^image\/(jpeg|png|gif|jpg)$/.test(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/GIF 格式的图片！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 封面图片改变处理
const handleCoverChange = async (file) => {
  if (!beforeCoverUpload(file.raw)) {
    return
  }
  
  try {
    // 上传封面图片到服务器
    const response = await uploadPostCover(file.raw)
    const data = validateApiResponse(response)
    
    if (data && Array.isArray(data) && data.length > 0) {
      // 如果返回的是数组，取第一个
      articleData.coverUrl = data[0]
      ElMessage.success('封面上传成功')
    } else if (data && typeof data === 'string') {
      // 如果直接返回字符串
      articleData.coverUrl = data
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败')
    }
  } catch (error) {
    ElMessage.error('封面上传失败，请重试')
  }
}

// 移除封面图
const removeCover = () => {
  articleData.coverUrl = ''
  ElMessage.success('封面已移除')
}

// 组件卸载时清理资源
onBeforeUnmount(() => {
  if (articleData.coverUrl && articleData.coverUrl.startsWith('blob:')) {
    URL.revokeObjectURL(articleData.coverUrl)
  }
})

// 确认发布
const confirmPublish = async () => {
  // 表单验证
  if (!articleData.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!articleData.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  // 分类验证已移除
  if (!articleData.tagIds.length) {
    ElMessage.warning('请至少选择一个标签')
    return
  }

  try {
    // 调用发布文章接口
    const response = await createArticle(articleData)

    ElMessage.success('文章发布成功')
    createDialogVisible.value = false
    // 发布成功后跳转到文章详情页
    router.push(`/article/${response.data.id}`)
  } catch (error) {
    ElMessage.error('发布文章失败，请稍后重试')
  }
}

// 在 script setup 中添加预览相关的状态
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 添加预览方法
const previewImage = () => {
  previewVisible.value = true
  previewImageUrl.value = articleData.coverUrl
}

// 添加router实例
const router = useRouter()

// 添加打开发布弹窗的方法
const openPublishDialog = () => {
  const userStore = useUserStore()

  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }

  if (!articleData.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!articleData.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  createDialogVisible.value = true
}

onMounted(() => {
  getTagPage().then((res) => {
    allTags.push(...res.data)
  }).catch(() => {
    // 标签加载失败，静默处理
  })
  getCategoryPage().then((res) => {
    categories.push(...res.data)
  }).catch(() => {
    // 分类加载失败，静默处理
  })
})
</script>

<style>
.editor-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

.editor-header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.back-home {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s;
  white-space: nowrap;
  padding: 8px 12px;
  border-radius: 4px;
}

.back-home:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}

.divider {
  width: 1px;
  height: 24px;
  background-color: #dcdfe6;
}

.title-input {
  flex: 1;
  max-width: 800px;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
}

.title-input :deep(.el-input__inner) {
  font-size: 16px;
  height: 40px;
  border: none;
}

.header-right {
  display: flex;
  gap: 12px;
  margin-left: 16px;
}

.editor-main {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: calc(100vh - 64px);
}

.editor-container {
  height: 100%;
  width: 100%;
}

/* md-editor-v3样式覆盖 */
:deep(.md-editor) {
  border: none !important;
  height: 100% !important;
  display: flex !important;
  flex-direction: column !important;
}

:deep(.md-editor-toolbar) {
  border-bottom: 1px solid #e4e7ed !important;
  flex-shrink: 0 !important;
}

:deep(.md-editor-content) {
  height: auto !important;
  flex: 1 !important;
  overflow: hidden !important;
}

:deep(.md-editor-preview) {
  height: 100% !important;
  overflow-y: auto !important;
}

:deep(.md-editor-input) {
  height: 100% !important;
  overflow-y: auto !important;
}

/* 发布弹窗样式 */
:deep(.el-dialog) {
  margin: 15vh auto 0 !important;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 20px;
  flex: 1;
  overflow: visible;
}

:deep(.el-dialog__footer) {
  margin: 0;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-overlay) {
  overflow: hidden;
}

/* 发布表单样式 */
.publish-form {
  height: 100%;
}

.form-item {
  margin-bottom: 24px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.form-label.required::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

.cover-upload {
  width: 100%;
}

.cover-uploader {
  width: 213px;
  height: 128px;
}

.upload-area {
  width: 100%;
  height: 100%;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
  background-color: #fafafa;
}

.upload-area:hover {
  border-color: var(--el-color-primary);
  background-color: #f5f7fa;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin: 8px 0;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.preview-wrapper {
  position: relative;
  width: 213px;
  height: 128px;
  border-radius: 6px;
  overflow: hidden;
}

.preview-wrapper:hover .preview-mask {
  opacity: 1;
}

.preview-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: default;
}

.preview-actions {
  display: flex;
  gap: 16px;
}

.preview-icon {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  transition: transform 0.3s;
}

.preview-icon:hover {
  transform: scale(1.1);
}

.cover-preview {
  width: 100%;
  height: 100%;
  display: block;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  background: #f5f7fa;
}

.image-error .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 标签选择器新样式 */
.tags-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 16px;
}

:deep(.el-select-dropdown__list) {
  padding: 0;
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 0 !important;
}

:deep(.el-select-dropdown__item)::after {
  display: none;
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item.selected) {
  background-color: transparent !important;
}

:deep(.el-select-dropdown__item) .tag-item {
  padding: 6px 12px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: background-color 0.2s ease;
  text-align: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  margin: 0 auto;
  width: calc(100% - 24px);
}

:deep(.el-select-dropdown__item) .tag-item:hover {
  background-color: #f0f2f5;
}

:deep(.el-select-dropdown__item.selected) .tag-item {
  background-color: #e6f4ff;
  color: #1890ff;
}

/* 输入框内已选标签的样式 */
:deep(.el-select__tags) {
  flex-wrap: wrap;
  gap: 4px;
  padding-top: 2px;
}

:deep(.el-select__tags-text) {
  font-size: 13px;
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-tag) {
  background-color: #e6f4ff !important;
  border: none !important;
  color: #1890ff !important;
  margin: 0;
}

:deep(.el-tag .el-tag__close) {
  color: #1890ff;
  background-color: transparent;
}

:deep(.el-tag .el-tag__close:hover) {
  color: #fff;
  background-color: #1890ff;
}

/* 下拉框滚动条样式 */
:deep(.el-select-dropdown__wrap) {
  max-height: none;
}

:deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

:deep(.el-scrollbar__wrap)::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-scrollbar__wrap)::-webkit-scrollbar-thumb {
  border-radius: 3px;
  background: #ddd;
}

:deep(.el-scrollbar__wrap)::-webkit-scrollbar-track {
  border-radius: 3px;
  background: #f5f7fa;
}

/* 移除不需要的样式 */
.tag-tabs,
:deep(.el-tabs__header),
:deep(.el-tabs__nav-wrap) {
  display: none;
}

/* 标签选择器样式 */
:deep(.el-select-dropdown) {
  max-height: none;
}

:deep(.el-select-dropdown__wrap) {
  max-height: none;
}

:deep(.el-select-dropdown .el-scrollbar__wrap) {
  max-height: 320px;
}

/* 确保预览图片弹窗在最上层 */
:deep(.el-image-viewer__wrapper) {
  z-index: 2001 !important;
}

:deep(.el-image-viewer__close) {
  color: #fff;
}
</style>
