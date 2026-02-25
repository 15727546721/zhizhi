<template>
  <div class="post-list-item" @click="handleClick">
    <div class="post-main">
      <div class="post-info">
        <h3 class="post-title">{{ post.title }}</h3>
        <p v-if="post.description" class="post-desc">{{ post.description }}</p>
        <div class="post-footer">
          <div class="post-meta">
            <span class="time">{{ formatTime(post.createTime) }}</span>
            <span class="dot">·</span>
            <div class="post-stats">
              <span class="stat-item">
                <el-icon><View/></el-icon>
                {{ post.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><Star/></el-icon>
                {{ post.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound/></el-icon>
                {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
          <div v-if="post.tagNameList && post.tagNameList.length > 0" class="post-tags">
            <el-tag
              v-for="tag in post.tagNameList.slice(0, 3)"
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
      <div v-if="post.coverUrl" class="post-cover-wrapper">
        <div class="post-cover">
          <el-image :src="post.coverUrl" fit="cover" />
        </div>
      </div>
    </div>
    <div v-if="showActions && isOwnProfile" class="post-actions" @click.stop>
      <el-button size="small" text @click.stop="handleEdit">编辑</el-button>
      <el-button size="small" text type="danger" @click.stop="handleDelete">删除</el-button>
    </div>
  </div>
</template>

<script setup>
import { View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  isOwnProfile: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['edit', 'delete', 'click'])

const router = useRouter()

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const handleClick = () => {
  emit('click', props.post)
  router.push({
    name: 'PostDetail',
    params: { id: props.post.id }
  })
}

const handleEdit = () => {
  emit('edit', props.post)
  router.push({
    name: 'PostEdit',
    params: { id: props.post.id }
  })
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete', props.post)
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.post-list-item {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.post-list-item:hover {
  background-color: #fafafa;
}

.post-list-item:last-child {
  border-bottom: none;
}

.post-main {
  display: flex;
  gap: 20px;
  min-height: 120px;
}

.post-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.post-desc {
  margin: 0 0 12px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8a919f;
  font-size: 13px;
  flex-wrap: wrap;
}

.post-stats {
  display: inline-flex;
  align-items: center;
  gap: 16px;
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

.post-list-item:hover .post-cover :deep(.el-image) {
  transform: scale(1.05);
}

.post-tags {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
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

.post-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .post-main {
    min-height: 80px;
  }

  .post-cover-wrapper {
    flex: 0 0 120px;
    height: 80px;
  }

  .post-title {
    font-size: 16px;
    -webkit-line-clamp: 1;
  }

  .post-desc {
    font-size: 13px;
    -webkit-line-clamp: 1;
    margin-bottom: 8px;
  }
}
</style>

