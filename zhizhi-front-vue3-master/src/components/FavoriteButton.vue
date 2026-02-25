<template>
  <div class="favorite-wrapper">
    <button 
      class="favorite-btn" 
      :class="{ active: isFavorite }"
      @click="handleFavorite"
      :disabled="isLoading"
    >
      <svg class="icon" viewBox="0 0 1024 1024">
        <path 
          v-if="isFavorite" 
          d="M851.5 296.5L549.2 66.2c-12.9-12.9-34-12.9-46.9 0L172.5 296.5c-26.2 26.3-26.2 69.1 0 95.4l36.6 36.6c12.6 12.6 12.6 33 0 45.6-12.6 12.6-33 12.6-45.6 0l-36.6-36.6c-41.5-41.5-41.5-109 0-150.5 41.5-41.5 109-41.5 150.5 0L512 466.6l240-240.1c20.7-20.7 47.9-31 75.1-31s54.5 10.3 75.1 31c41.5 41.5 41.5 109 0 150.5l-36.6 36.6c-12.6 12.6-33 12.6-45.6 0-12.6-12.6-12.6-33 0-45.6l36.6-36.6c8.1-8.1 8.1-21.4 0-29.5z"
          fill="#ff4757"
        />
        <path 
          v-else 
          d="M852 296.3L549.8 66c-12.9-12.9-34-12.9-46.9 0L172 296.3c-26.2 26.3-26.2 69.1 0 95.5L208.7 428c12.6 12.6 12.6 33 0 45.6-12.6 12.6-33 12.6-45.6 0L126 419.8c-41.5-41.5-41.5-109 0-150.5 41.5-41.5 109-41.5 150.5 0L512 466.8l235-235.1c20.7-20.7 47.9-31 75.1-31s54.5 10.3 75.1 31c41.5 41.5 41.5 109 0 150.5l-37 37c-12.6 12.6-33 12.6-45.6 0-12.6-12.6-12.6-33 0-45.6l37-37c8.1-8.1 8.1-21.4 0-29.5z"
          fill="#ccc"
        />
      </svg>
      <span class="text">{{ isFavorite ? '已收藏' : '收藏' }}</span>
      <span v-if="showCount" class="count">{{ favoriteCount || 0 }}</span>
    </button>
    
    <!-- 收藏夹选择对话框 -->
    <FolderSelectDialog 
      v-model="showFolderDialog" 
      @confirm="handleFolderConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addFavorite, removeFavorite, checkFavorite, FavoriteType } from '@/api/favorites'
import { useUserStore } from '@/stores/module/user'
import FolderSelectDialog from '@/components/FolderSelectDialog.vue'

interface Props {
  targetId: number | string
  type?: string
  showCount?: boolean
  initialCount?: number
  showFolderSelect?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'post',
  showCount: true,
  initialCount: 0,
  showFolderSelect: true  // 默认显示收藏夹选择
})

const emit = defineEmits<{
  change: [value: boolean]
  'update:favoriteCount': [value: number]
}>()

const userStore = useUserStore()
const isFavorite = ref(false)
const isLoading = ref(false)
const favoriteCount = ref(props.initialCount)
const showFolderDialog = ref(false)

const getFavoriteTypeValue = () => {
  return props.type?.toUpperCase() === 'POST' ? FavoriteType.POST : FavoriteType.COMMENT
}

const fetchFavoriteStatus = async () => {
  if (!userStore.isAuthenticated) {
    isFavorite.value = false
    return
  }

  try {
    const res = await checkFavorite(Number(props.targetId), getFavoriteTypeValue())
    isFavorite.value = res?.data || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorite.value = false
  }
}

const handleFavorite = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  if (isLoading.value) return

  // 如果已收藏，直接取消
  if (isFavorite.value) {
    await doUnfavorite()
    return
  }

  // 如果启用收藏夹选择，显示对话框
  if (props.showFolderSelect) {
    showFolderDialog.value = true
    return
  }

  // 否则直接收藏到默认收藏夹
  await doFavorite()
}

const handleFolderConfirm = async (folderId: number) => {
  await doFavorite(folderId)
}

const doFavorite = async (folderId?: number) => {
  isLoading.value = true
  const prevFavoriteState = isFavorite.value
  const prevCount = favoriteCount.value

  try {
    const res = await addFavorite(Number(props.targetId), getFavoriteTypeValue(), folderId)

    if (res?.code === 20000 || res?.code === 200 || !res?.code) {
      isFavorite.value = true
      favoriteCount.value += 1
      ElMessage.success('收藏成功')
      emit('update:favoriteCount', favoriteCount.value)
      emit('change', true)
    } else if (res?.info) {
      const message = res.info
      if (message.includes('已收藏') || message.includes('请勿重复操作')) {
        isFavorite.value = true
        ElMessage.warning(message)
      } else {
        ElMessage.error(message)
      }
    } else {
      ElMessage.error('收藏失败')
      isFavorite.value = prevFavoriteState
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    isFavorite.value = prevFavoriteState
    favoriteCount.value = prevCount
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

const doUnfavorite = async () => {
  isLoading.value = true
  const prevFavoriteState = isFavorite.value
  const prevCount = favoriteCount.value

  try {
    const res = await removeFavorite(Number(props.targetId), getFavoriteTypeValue())

    if (res?.code === 20000 || res?.code === 200 || !res?.code) {
      isFavorite.value = false
      favoriteCount.value = Math.max(0, favoriteCount.value - 1)
      ElMessage.success('取消收藏成功')
      emit('update:favoriteCount', favoriteCount.value)
      emit('change', false)
    } else if (res?.info) {
      const message = res.info
      if (message.includes('尚未收藏') || message.includes('未找到收藏记录')) {
        isFavorite.value = false
        ElMessage.warning(message)
      } else {
        ElMessage.error(message)
      }
    } else {
      ElMessage.error('取消收藏失败')
      isFavorite.value = prevFavoriteState
    }
  } catch (error) {
    console.error('取消收藏操作失败:', error)
    isFavorite.value = prevFavoriteState
    favoriteCount.value = prevCount
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

watch(
  () => userStore.isAuthenticated,
  (newVal) => {
    if (newVal) {
      fetchFavoriteStatus()
    } else {
      isFavorite.value = false
    }
  }
)

watch(
  () => props.targetId,
  () => {
    fetchFavoriteStatus()
  }
)

watch(
  () => props.initialCount,
  (newVal) => {
    favoriteCount.value = newVal
  }
)

onMounted(() => {
  if (userStore.isAuthenticated) {
    fetchFavoriteStatus()
  }
})
</script>

<style scoped>
.favorite-wrapper {
  display: inline-block;
}

.favorite-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: #fff;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.favorite-btn:hover {
  border-color: #ff4757;
  color: #ff4757;
}

.favorite-btn.active {
  border-color: #ff4757;
  background-color: #fff5f6;
  color: #ff4757;
}

.favorite-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  transition: all 0.3s ease;
}

.text {
  margin-right: 4px;
}

.count {
  font-size: 12px;
  color: #999;
}

.favorite-btn.active .count {
  color: #ff4757;
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.favorite-btn.active .icon {
  animation: pulse 0.3s ease;
}
</style>