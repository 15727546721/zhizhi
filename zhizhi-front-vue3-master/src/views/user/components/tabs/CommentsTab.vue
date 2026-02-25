<template>
  <div class="comments-tab">
    <div v-loading="loading" class="comments-list">
      <div
        v-for="item in comments"
        :key="item.comment?.id"
        class="comment-item"
        @click="handleCommentClick(item)"
      >
        <div class="comment-header">
          <div class="comment-target">
            <el-link
              :underline="false"
              type="primary"
              @click.stop="handleTargetClick(item)"
            >
              评论了：{{ item.targetTitle || '无标题' }}
            </el-link>
          </div>
          <div class="comment-time">
            {{ formatTime(item.comment?.createTime) }}
          </div>
        </div>
        
        <div class="comment-content">
          <div class="comment-text">
            {{ item.comment?.content || '' }}
          </div>
        </div>
        
        <div class="comment-footer">
          <div class="comment-stats">
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ item.comment?.replyCount || 0 }}
            </span>
            <span class="stat-item">
              <CustomIcon name="thumb-up" :size="14" />
              {{ item.comment?.likeCount || 0 }}
            </span>
          </div>
        </div>
      </div>
      
      <EmptyState
        v-if="!loading && comments.length === 0"
        description="还没有任何评论"
      />
    </div>
    
    <div v-if="comments.length > 0" class="load-more">
      <el-button
        v-if="hasMore && !loading"
        link
        @click="loadMore"
        :loading="loading"
      >
        加载更多
      </el-button>
      <div v-else-if="!hasMore && comments.length > 0" class="no-more">
        没有更多了
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserComments } from '../../composables/useUserComments'
import EmptyState from '../list-items/EmptyState.vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import CustomIcon from '@/components/CustomIcon/index.vue'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  }
})

const router = useRouter()

const {
  comments,
  loading,
  hasMore,
  loadComments,
  loadMore,
  refresh
} = useUserComments(props.userId)

// 监听 userId 变化
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    refresh()
  }
}, { immediate: false })

// 初始化加载
onMounted(() => {
  loadComments(true)
})

const handleCommentClick = (item) => {
  if (item.targetUrl) {
    router.push(item.targetUrl)
  }
}

const handleTargetClick = (item) => {
  if (item.targetUrl) {
    router.push(item.targetUrl)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (days > 0) {
      return `${days}天前`
    } else if (hours > 0) {
      return `${hours}小时前`
    } else if (minutes > 0) {
      return `${minutes}分钟前`
    } else {
      return '刚刚'
    }
  } catch (e) {
    return time
  }
}

// 暴露刷新方法
defineExpose({
  refresh
})
</script>

<style scoped>
.comments-tab {
  padding: 20px;
  min-height: 400px;
}

.comments-list {
  min-height: 200px;
}

.comment-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.comment-item:hover {
  background-color: #fafafa;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-target {
  flex: 1;
}

.comment-time {
  color: #999;
  font-size: 12px;
  margin-left: 12px;
}

.comment-content {
  margin-bottom: 12px;
}

.comment-text {
  color: #333;
  line-height: 1.6;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

.no-more {
  color: #999;
  font-size: 14px;
  padding: 20px 0;
  text-align: center;
}
</style>

