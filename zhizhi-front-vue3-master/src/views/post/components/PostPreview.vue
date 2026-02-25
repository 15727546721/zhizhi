<template>
  <div class="post-preview">
    <div class="preview-header">
      <h3>帖子预览</h3>
      <el-button @click="closePreview" size="small" type="danger" plain>
        <el-icon><Close /></el-icon>
        关闭预览
      </el-button>
    </div>
    
    <div class="preview-content">
      <!-- 帖子标题 -->
      <h1 class="post-title">{{ postData.title || '请输入标题' }}</h1>
      
      <!-- 帖子元信息 -->
      <div class="post-meta">
        <div class="author-info">
          <el-avatar :size="40" :src="currentUser.avatar || ''">
            {{ currentUser.name ? currentUser.name.charAt(0) : 'U' }}
          </el-avatar>
          <div class="author-detail">
            <div class="author-name">{{ currentUser.name || '未知用户' }}</div>
            <div class="publish-info">
              <span>{{ displayTime }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 帖子描述 -->
      <div v-if="postData.description" class="post-description">
        {{ postData.description }}
      </div>
      
      <!-- 帖子内容 -->
      <div class="post-content">
        <div 
          v-if="mode === 'markdown'" 
          class="markdown-content"
          v-html="renderMarkdown(postData.content)"
        ></div>
        <div 
          v-else 
          class="plain-content"
          v-html="formatContent(postData.content)"
        ></div>
      </div>
      
      <!-- 帖子标签 -->
      <div v-if="postData.tags && postData.tags.length > 0" class="post-tags">
        <el-tag 
          v-for="tag in postData.tags" 
          :key="tag.id || tag.name || tag"
          size="small"
          effect="plain"
          class="tag-item"
        >
          #{{ tag.name || tag }}
        </el-tag>
      </div>
      
      <!-- 帖子封面 -->
      <div v-if="postData.coverUrl" class="post-cover">
        <img :src="postData.coverUrl" :alt="postData.title" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { useUserStore } from '@/stores/module/user'

// 类型定义
interface PostTag {
  id?: number
  name?: string
}

interface PostData {
  title?: string
  content?: string
  description?: string
  createTime?: string
  coverUrl?: string
  tags?: (PostTag | string)[]
}

interface Props {
  postData: PostData
  mode?: 'text' | 'markdown'
}

defineOptions({
  name: 'PostPreview'
})

const props = withDefaults(defineProps<Props>(), {
  mode: 'text'
})

const emit = defineEmits<{
  close: []
}>()

const userStore = useUserStore()

// 当前用户信息
const currentUser = computed(() => {
  const userInfo = userStore.userInfo || {}
  return {
    id: userInfo.id || 0,
    name: userInfo.nickname || userInfo.username || '未登录用户',
    avatar: userInfo.avatar || ''
  }
})

// 显示时间（如果是预览则显示当前时间，否则显示帖子创建时间）
const displayTime = computed(() => {
  if (props.postData.createTime) {
    return props.postData.createTime
  }
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
})

// 格式化内容（处理换行符）
const formatContent = (content: string | undefined): string => {
  if (!content) return '<p style="color: #999;">请输入内容...</p>'
  // 将换行符转换为HTML的<br>标签
  return content.replace(/\n/g, '<br>')
}

// 渲染Markdown内容
const renderMarkdown = (content: string | undefined): string => {
  if (!content) return '<p style="color: #999;">请输入内容...</p>'
  try {
    // 使用marked库渲染Markdown
    return marked.parse(content) as string
  } catch (error) {
    // 如果渲染失败，返回格式化后的纯文本
    return formatContent(content)
  }
}

// 关闭预览
const closePreview = (): void => {
  emit('close')
}
</script>

<style scoped>
.post-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.preview-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.preview-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

.post-type-badge {
  position: absolute;
  top: 24px;
  right: 24px;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
}

.article-badge {
  background: #ffeaa7;
  color: #d35400;
}

.discussion-badge {
  background: #d6eaf8;
  color: #2980b9;
}

.question-badge {
  background: #e8f8f5;
  color: #16a085;
}

.post-badge {
  background: #f8f9fa;
  color: #606266;
}

.post-title {
  font-size: 32px;
  font-weight: 600;
  color: #252933;
  margin: 0 0 24px;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: #303133;
}

.publish-info {
  font-size: 13px;
  color: #909399;
}

.post-description {
  font-size: 18px;
  color: #606266;
  margin-bottom: 24px;
  line-height: 1.6;
  font-weight: 500;
}

.post-content {
  margin-bottom: 24px;
}

.markdown-content {
  font-size: 16px;
  line-height: 1.7;
  color: #303133;
}

.markdown-content :deep(h1) {
  font-size: 2em;
  margin: 0.67em 0;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  margin: 0.83em 0;
}

.markdown-content :deep(h3) {
  font-size: 1.17em;
  margin: 1em 0;
}

.markdown-content :deep(p) {
  margin: 1em 0;
}

.markdown-content :deep(ul), .markdown-content :deep(ol) {
  margin: 1em 0;
  padding-left: 40px;
}

.markdown-content :deep(li) {
  margin: 0.5em 0;
}

.markdown-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0 1em;
  border-left: 4px solid #dcdfe6;
  color: #606266;
}

.markdown-content :deep(code) {
  background-color: #f5f7fa;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: monospace;
}

.markdown-content :deep(pre) {
  background-color: #f5f7fa;
  padding: 1em;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.plain-content {
  font-size: 16px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 20px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin: 20px 0;
}

.tag-item {
  cursor: pointer;
}

.post-cover {
  margin: 24px 0;
}

.post-cover img {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>