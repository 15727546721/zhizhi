<template>
  <div class="column-card" @click="handleClick">
    <!-- 状态标识 -->
    <div v-if="column.status === 0" class="status-badge draft">
      <el-icon><Edit /></el-icon>
      <span>草稿</span>
    </div>
    <div v-else-if="column.status === 2" class="status-badge archived">
      <el-icon><Lock /></el-icon>
      <span>已归档</span>
    </div>
    
    <!-- 推荐标识 -->
    <div v-if="column.isRecommended && column.status === 1" class="recommended-badge">
      <el-icon><Star /></el-icon>
      <span>推荐</span>
    </div>

    <!-- 封面图 -->
    <div class="column-cover">
      <img v-if="column.coverUrl" :src="column.coverUrl" :alt="column.name" />
      <div v-else class="default-cover">
        <el-icon :size="48"><Collection /></el-icon>
      </div>
    </div>

    <!-- 专栏信息 -->
    <div class="column-info">
      <h3 class="column-name" :title="column.name">{{ column.name }}</h3>
      <p v-if="column.description" class="column-desc" :title="column.description">
        {{ column.description }}
      </p>

      <!-- 作者信息 -->
      <div class="column-author">
        <img :src="column.userAvatar" :alt="column.userName" class="author-avatar" />
        <span class="author-name">{{ column.userName }}</span>
      </div>

      <!-- 统计信息 -->
      <div class="column-stats">
        <span class="stat-item">
          <el-icon><Document /></el-icon>
          {{ column.postCount }} 篇
        </span>
        <span class="stat-item">
          <el-icon><Star /></el-icon>
          {{ column.subscribeCount }} 订阅
        </span>
      </div>

      <!-- 订阅按钮 -->
      <div v-if="showSubscribeButton" class="column-actions">
        <el-button
          v-if="!column.isSubscribed"
          type="primary"
          size="small"
          @click.stop="handleSubscribe"
        >
          订阅
        </el-button>
        <el-button v-else size="small" @click.stop="handleUnsubscribe"> 已订阅 </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, Document, Star, Edit, Lock } from '@element-plus/icons-vue'
import type { ColumnVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import { useUserStore } from '@/stores/module/user'
import { requireAuth } from '@/utils/auth'

interface Props {
  column: ColumnVO
  showSubscribeButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSubscribeButton: true,
})

const router = useRouter()
const columnStore = useColumnStore()
const userStore = useUserStore()

const isOwner = computed(() => {
  return userStore.userInfo?.id === props.column.userId
})

const handleClick = () => {
  router.push(`/columns/${props.column.id}`)
}

const handleSubscribe = async () => {
  if (!requireAuth('登录后才能订阅专栏')) return
  await columnStore.subscribeColumn(props.column.id)
}

const handleUnsubscribe = async () => {
  await columnStore.unsubscribeColumn(props.column.id)
}
</script>

<style scoped lang="scss">
.column-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.recommended-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);

  .el-icon {
    font-size: 14px;
  }
}

.status-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;

  .el-icon {
    font-size: 14px;
  }

  &.draft {
    background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
    box-shadow: 0 2px 8px rgba(100, 116, 139, 0.3);
  }

  &.archived {
    background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
    box-shadow: 0 2px 8px rgba(75, 85, 99, 0.3);
  }
}

.column-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .default-cover {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
}

.column-info {
  padding: 16px;
}

.column-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.column-desc {
  font-size: 14px;
  color: #646a73;
  margin: 0 0 12px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 44px;
}

.column-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .author-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
  }

  .author-name {
    font-size: 13px;
    color: #646a73;
  }
}

.column-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #8a919f;

    .el-icon {
      font-size: 14px;
    }
  }
}

.column-actions {
  .el-button {
    width: 100%;
  }
}
</style>
