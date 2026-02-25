<template>
  <div class="post-list">
    <div
        v-for="post in posts"
        :key="post.id"
        class="post-item"
        @click="navigateToDetail(post.id)"
    >
      <div class="post-main">
        <div class="post-info">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-desc">{{ post.description }}</p>
          <div class="post-footer">
            <div class="post-meta">
              <span class="author">{{ post.nickname }}</span>
              <span class="dot">·</span>
              <span class="time">{{ post.createTime }}</span>
              <span class="dot">·</span>
              <div class="post-stats">
                <span class="stat-item">
                  <el-icon><View/></el-icon>
                  {{ post.viewCount }}
                </span>
                <span class="stat-item">
                  <el-icon><Star/></el-icon>
                  {{ post.likeCount }}
                </span>
                <span class="stat-item">
                  <el-icon><ChatDotRound/></el-icon>
                  {{ post.commentCount }}
                </span>
              </div>
            </div>
            <div class="post-tags">
              <el-tag
                  v-for="tag in post.tagNameList"
                  :key="tag"
                  size="small"
                  class="post-tag"
                  :effect="'plain'"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="post-cover-wrapper" :class="{ 'no-cover': !post.coverUrl }">
          <div v-if="post.coverUrl" class="post-cover">
            <el-image :src="post.coverUrl" fit="cover"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/home'

// 类型定义
interface PostItem {
  id: number
  title: string
  description?: string
  nickname?: string
  createTime?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  coverUrl?: string
  tagNameList?: string[]
}

const router = useRouter()

const props = defineProps<{
  // categoryId 已移除，使用标签代替分类
}>()

const posts = ref<PostItem[]>([])

const navigateToDetail = (postId: number): void => {
  try {
    router.push({
      name: 'PostDetail',
      params: { id: postId }
    })
  } catch (error) {
    // 导航错误
  }
}

// categoryId watch 已移除，使用标签代替分类


</script>

<style scoped>
.post-list {
  background: #fff;
  border-radius: 4px;
}

.post-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.post-item:last-child {
  border-bottom: none;
}

.post-main {
  display: flex;
  gap: 20px;
  height: 120px;
  padding: 4px 0;
  overflow: hidden;
}

.post-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.post-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  word-wrap: break-word;
}

.post-desc {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  word-wrap: break-word;
  margin-bottom: 4px;
  min-height: 44px;
  max-height: 44px;
  white-space: normal;
  hyphens: auto;
  -webkit-hyphens: auto;
  -ms-hyphens: auto;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: auto;
  margin-top: auto;
  padding: 2px 0;
  min-height: 20px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8a919f;
  font-size: 13px;
  line-height: 1;
  flex-wrap: wrap;
}

.post-stats {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  line-height: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8a919f;
  font-size: 13px;
}

.stat-item .el-icon {
  font-size: 14px;
}

.dot {
  color: #8a919f;
}

.post-cover-wrapper {
  flex: 0 0 200px;
  height: 120px;
}

.post-cover-wrapper.no-cover {
  display: none;
}

.post-cover {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.post-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.post-cover:hover :deep(.el-image) {
  transform: scale(1.05);
}

.post-tags {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.post-tag {
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 18px;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
  border-color: transparent;
  color: #409eff;
}

.post-tag:hover {
  background: rgba(64, 158, 255, 0.2);
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .post-main {
    height: 80px;
    padding: 2px 0;
  }

  .post-cover-wrapper {
    flex: 0 0 120px;
    height: 80px;
  }

  .post-title {
    font-size: 16px;
    margin-bottom: 4px;
    -webkit-line-clamp: 1;
  }

  .post-desc {
    font-size: 13px;
    -webkit-line-clamp: 1;
    margin-bottom: 2px;
    min-height: 22px;
    max-height: 22px;
  }

  .post-footer {
    min-height: 18px;
  }

  .post-tag {
    height: 18px;
    padding: 0 4px;
    font-size: 11px;
    line-height: 16px;
  }
}

@media screen and (max-width: 640px) {
  .post-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .post-meta {
    gap: 4px;
    width: 100%;
  }

  .post-stats {
    gap: 12px;
  }

  .post-tags {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
