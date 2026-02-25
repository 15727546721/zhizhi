<template>
  <div class="user-card">
    <div class="user-info" @click="handleViewProfile">
      <UserAvatar :size="50" :src="user.avatar" :username="user.username" :nickname="user.nickname" />
      <div class="user-details">
        <div class="user-name">{{ user.nickname || '匿名用户' }}</div>
        <div v-if="user.description" class="user-description">
          {{ user.description }}
        </div>
      </div>
    </div>
    <div class="user-actions">
      <UserActionButtons
        v-if="!isOwnProfile"
        :is-following="user.isFollowing"
        :follow-loading="followLoading"
        :show-message="false"
        size="small"
        @follow="handleFollow"
      />
      <el-button 
        size="small" 
        text
        @click="handleViewProfile"
      >
        查看主页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { followUser, unfollowUser } from '@/api/follow'
import UserActionButtons from '@/components/UserActionButtons.vue'
import UserAvatar from '@/components/UserAvatar.vue'

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  isOwnProfile: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['follow-change'])

const router = useRouter()
const followLoading = ref(false)

const handleViewProfile = () => {
  router.push({
    name: 'Profile',
    params: { userId: props.user.userId || props.user.id }
  })
}

const handleFollow = async () => {
  const userId = props.user.userId || props.user.id
  if (!userId) return

  try {
    followLoading.value = true
    if (props.user.isFollowing) {
      // 取消关注
      const response = await unfollowUser(userId)
      if (response.code === 20000) {
        emit('follow-change', { userId, isFollowing: false })
        ElMessage.success('取消关注成功')
      } else {
        ElMessage.error(response.info || '取消关注失败')
      }
    } else {
      // 关注
      const response = await followUser(userId)
      if (response.code === 20000) {
        emit('follow-change', { userId, isFollowing: true })
        ElMessage.success('关注成功')
      } else {
        ElMessage.error(response.info || '关注失败')
      }
    }
  } catch (error) {
    // 关注失败
  } finally {
    followLoading.value = false
  }
}
</script>

<style scoped>
.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.user-card:hover {
  background-color: #fafafa;
}

.user-card:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.user-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .user-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .user-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>

