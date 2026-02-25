<template>
  <el-button
    :type="buttonType"
    :size="size"
    :loading="loading"
    :disabled="disabled || isSelf"
    :class="['follow-button', statusClass]"
    @click.stop="handleClick"
  >
    <template v-if="!loading">
      <el-icon v-if="showIcon && !isFollowing" class="follow-icon"><Plus /></el-icon>
      <el-icon v-if="showIcon && isMutual" class="follow-icon"><Check /></el-icon>
      <span>{{ buttonText }}</span>
    </template>
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import { followUser, unfollowUser, isFollowing as checkFollowing } from '@/api/follow'
import { useUserStore } from '@/stores/module/user'

interface Props {
  userId: number | string
  initialFollowing?: boolean
  initialMutual?: boolean
  size?: 'small' | 'default' | 'large'
  showIcon?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  initialFollowing: undefined,
  initialMutual: false,
  size: 'default',
  showIcon: true,
  disabled: false
})

const emit = defineEmits<{
  'update:following': [value: boolean]
  change: [value: { following: boolean; mutual: boolean }]
}>()

const userStore = useUserStore()
const loading = ref(false)
const isFollowing = ref(false)
const isMutual = ref(false)

const isSelf = computed(() => {
  const currentUserId = userStore.userInfo?.id
  return currentUserId !== undefined && String(currentUserId) === String(props.userId)
})

const buttonType = computed(() => {
  if (isSelf.value) return 'info'
  if (isMutual.value) return 'success'
  if (isFollowing.value) return 'default'
  return 'primary'
})

const statusClass = computed(() => {
  if (isSelf.value) return 'is-self'
  if (isMutual.value) return 'is-mutual'
  if (isFollowing.value) return 'is-following'
  return 'not-following'
})

const buttonText = computed(() => {
  if (isSelf.value) return '自己'
  if (isMutual.value) return '互关'
  if (isFollowing.value) return '已关注'
  return '关注'
})

const fetchFollowStatus = async () => {
  if (!props.userId || isSelf.value) return

  try {
    loading.value = true
    const res = await checkFollowing(props.userId)
    if (res.code === 20000) {
      if (typeof res.data === 'object' && res.data !== null) {
        const data = res.data as { isFollowing?: boolean; following?: boolean; isMutual?: boolean; mutual?: boolean }
        isFollowing.value = data.isFollowing || data.following || false
        isMutual.value = data.isMutual || data.mutual || false
      } else {
        isFollowing.value = !!res.data
      }
    }
  } catch (error) {
    console.error('查询关注状态失败:', error)
  } finally {
    loading.value = false
  }
}

const handleClick = async () => {
  if (loading.value || isSelf.value) return

  try {
    loading.value = true

    if (isFollowing.value) {
      const res = await unfollowUser(props.userId)
      if (res.code === 20000) {
        isFollowing.value = false
        isMutual.value = false
        ElMessage.success('已取消关注')
        emit('update:following', false)
        emit('change', { following: false, mutual: false })
      }
    } else {
      const res = await followUser(props.userId)
      if (res.code === 20000) {
        isFollowing.value = true
        const data = res.data as { isMutual?: boolean; mutual?: boolean } | null
        if (data?.isMutual || data?.mutual) {
          isMutual.value = true
          ElMessage.success('关注成功，已互相关注')
        } else {
          ElMessage.success('关注成功')
        }
        emit('update:following', true)
        emit('change', { following: true, mutual: isMutual.value })
      }
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.initialFollowing,
  (val) => {
    if (val !== undefined) {
      isFollowing.value = val
    }
  },
  { immediate: true }
)

watch(
  () => props.initialMutual,
  (val) => {
    isMutual.value = val
  },
  { immediate: true }
)

watch(
  () => props.userId,
  (newVal, oldVal) => {
    if (newVal && newVal !== oldVal && props.initialFollowing === undefined) {
      fetchFollowStatus()
    }
  }
)

onMounted(() => {
  if (props.initialFollowing === undefined && props.userId) {
    fetchFollowStatus()
  }
})

defineExpose({
  refresh: fetchFollowStatus,
  isFollowing,
  isMutual
})
</script>

<style scoped>
.follow-button {
  min-width: 72px;
  transition: all 0.2s;
}

.follow-button.is-self {
  cursor: not-allowed;
}

.follow-button.is-mutual {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.follow-button.is-mutual:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.follow-button.is-following {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  color: #606266;
}

.follow-button.is-following:hover {
  color: #f56c6c;
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.follow-button.is-following:hover::before {
  content: '取消';
}

.follow-button.is-following:hover span {
  display: none;
}

.follow-icon {
  margin-right: 4px;
}

/* 小尺寸 */
.follow-button.el-button--small {
  min-width: 60px;
  padding: 5px 10px;
  font-size: 12px;
}
</style>
