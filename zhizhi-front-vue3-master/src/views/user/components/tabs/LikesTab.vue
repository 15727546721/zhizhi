<template>
  <div class="likes-tab">
    <div v-loading="loading" class="likes-list">
      <div
        v-for="item in likes"
        :key="item.likeId"
        class="like-item"
        @click="handleLikeClick(item)"
      >
        <div class="like-header">
          <div class="like-target">
            <el-link
              :underline="false"
              type="primary"
              @click.stop="handleTargetClick(item)"
            >
              点赞了：{{ item.targetTitle || '无标题' }}
            </el-link>
          </div>
          <div class="like-time">
            {{ formatTime(item.likeTime) }}
          </div>
        </div>
        
        <div class="like-footer">
          <div class="like-type">
            <el-tag :type="getTypeTagType(item.targetType)" size="small">
              {{ item.targetTypeName || '未知类型' }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <EmptyState
        v-if="!loading && likes.length === 0"
        description="还没有任何点赞"
      />
    </div>
    
    <div v-if="likes.length > 0" class="load-more">
      <el-button
        v-if="hasMore && !loading"
        link
        @click="loadMore"
        :loading="loading"
      >
        加载更多
      </el-button>
      <div v-else-if="!hasMore && likes.length > 0" class="no-more">
        没有更多了
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserLikes } from '../../composables/useUserLikes'
import EmptyState from '../list-items/EmptyState.vue'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  }
})

const router = useRouter()

const {
  likes,
  loading,
  hasMore,
  loadLikes,
  loadMore,
  refresh
} = useUserLikes(props.userId)

// 监听 userId 变化
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    refresh()
  }
}, { immediate: false })

// 初始化加载
onMounted(() => {
  loadLikes(true)
})

const handleLikeClick = (item) => {
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

const getTypeTagType = (targetType) => {
  // 1-帖子，2-随笔，3-评论
  if (targetType === 1) {
    return 'primary'
  } else if (targetType === 2) {
    return 'success'
  } else if (targetType === 3) {
    return 'info'
  }
  return ''
}

// 暴露刷新方法
defineExpose({
  refresh
})
</script>

<style scoped>
.likes-tab {
  padding: 20px;
  min-height: 400px;
}

.likes-list {
  min-height: 200px;
}

.like-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.like-item:hover {
  background-color: #fafafa;
}

.like-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.like-target {
  flex: 1;
}

.like-time {
  color: #999;
  font-size: 12px;
  margin-left: 12px;
}

.like-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.like-type {
  display: flex;
  align-items: center;
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

