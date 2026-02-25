<template>
  <div class="post-card" @click="$emit('click')">
    <div class="post-main">
      <h4 class="post-title">{{ post.title }}</h4>
      <p class="post-desc" v-if="post.description">{{ post.description }}</p>
      <div class="post-meta">
        <div class="author">
          <el-avatar :size="20" :src="post.authorAvatar">
            {{ post.authorName?.charAt(0) }}
          </el-avatar>
          <span class="author-name">{{ post.authorName }}</span>
        </div>
        <div class="stats">
          <span><el-icon><View /></el-icon> {{ formatCount(post.viewCount) }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ formatCount(post.commentCount) }}</span>
          <span><el-icon><Star /></el-icon> {{ formatCount(post.likeCount) }}</span>
        </div>
        <span class="time">{{ post.createTime }}</span>
      </div>
      <div class="post-tags" v-if="post.tags?.length > 0">
        <el-tag 
          v-for="tag in post.tags.slice(0, 3)" 
          :key="tag" 
          size="small" 
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>
    <div class="post-cover" v-if="post.coverUrl">
      <img :src="post.coverUrl" :alt="post.title" />
    </div>
  </div>
</template>

<script setup>
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'

defineProps({
  post: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const formatCount = (count) => {
  if (!count) return 0
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}
</script>

<style scoped>
.post-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--el-border-color-light);
}

.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-5);
}

.post-main {
  flex: 1;
  min-width: 0;
}

.post-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-desc {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.author-name {
  font-weight: 500;
}

.stats {
  display: flex;
  gap: 12px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time {
  margin-left: auto;
}

.post-tags {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.post-cover {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 640px) {
  .post-cover {
    width: 80px;
    height: 60px;
  }
  
  .post-meta {
    flex-wrap: wrap;
  }
}
</style>
