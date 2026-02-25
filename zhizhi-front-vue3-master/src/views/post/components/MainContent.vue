<template>
  <div class="main-content">
    <div class="post-container">
      <h1 class="post-title">{{ post.title || '无标题' }}</h1>
      <div class="post-meta">
        <div class="author-info">
          <el-avatar :size="40" :src="author.avatar || 'https://placeholder.co/100'" />
          <div class="author-detail">
            <div class="author-name">{{ author.name || '匿名用户' }}</div>
            <div class="publish-info">
              <span>{{ (post && post.createTime) || '' }}</span>
              <span class="dot">·</span>
              <span>阅读 {{ (post && post.viewCount) || 0 }}</span>
            </div>
          </div>
        </div>
        <div class="post-tags" v-if="post && post.tags && post.tags.length > 0">
          <el-tag
              v-for="tag in post.tags"
              :key="tag.id || tag.name || tag"
              size="small"
              effect="plain"
              class="tag-item"
          >
            {{ tag.name || tag }}
          </el-tag>
        </div>
      </div>
      <MdPreview :id="id" :modelValue="(post && post.content) ? (post.content || post.content.formattedContent || post.contentValue || '') : ''" />
    </div>

    <!-- 添加空白隔离区域 -->
    <div style="height: 24px;"></div>
    
    <!-- 插槽用于子组件添加特定内容 -->
    <slot name="additional-content"></slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'

// 类型定义
interface PostAuthor {
  name?: string
  avatar?: string
}

interface PostUser {
  nickname?: string
  username?: string
  avatar?: string
}

interface PostTag {
  id?: number
  name?: string
}

interface PostData {
  title?: string
  content?: string
  createTime?: string
  viewCount?: number
  author?: PostAuthor
  user?: PostUser
  tags?: (PostTag | string)[]
}

interface Props {
  post: PostData
  id?: string
}

const props = withDefaults(defineProps<Props>(), {
  id: 'preview-only'
})

// 作者信息
const author = computed(() => {
  if (!props.post) return {}

  return {
    name:
      props.post.author?.name ||
      props.post.user?.nickname ||
      props.post.user?.username ||
      '匿名用户',
    avatar: props.post.author?.avatar || props.post.user?.avatar || 'https://placeholder.co/100'
  }
})
</script>

<style scoped>
/* 中间文章内容区 */
.main-content {
  width: 800px;
  min-width: 800px;
  margin: 0 auto;
}

.post-container {
  background: #fff;
  padding: 32px;
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.post-title {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 24px;
  line-height: 1.4;
  color: #252933;
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
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.publish-info {
  font-size: 14px;
  color: #8a919f;
}

.dot {
  margin: 0 6px;
}

.post-tags {
  display: flex;
  gap: 8px;
}

.tag-item {
  border-radius: 4px;
}

/* 响应式布局 */
@media screen and (max-width: 1200px) {
  .main-content {
    width: 100%;
    min-width: 0;
    max-width: 800px;
  }
}

@media screen and (max-width: 768px) {
  .main-content {
    width: 100%;
    min-width: 0;
  }

  .post-container {
    padding: 24px 16px;
  }
}
</style>