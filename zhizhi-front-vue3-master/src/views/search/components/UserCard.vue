<template>
  <div class="user-card" @click="$emit('click')">
    <UserAvatar :size="48" :src="user.avatar" :username="user.username" :nickname="user.nickname" />
    <div class="user-info">
      <div class="user-name">
        <span class="nickname">{{ user.nickname || user.username }}</span>
        <el-tag v-if="user.isOfficial" size="small" type="danger">官方</el-tag>
      </div>
      <p class="user-desc" v-if="user.description">{{ user.description }}</p>
      <div class="user-stats">
        <span><strong>{{ formatCount(user.fansCount) }}</strong> 粉丝</span>
        <span><strong>{{ formatCount(user.postCount) }}</strong> 帖子</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import UserAvatar from '@/components/UserAvatar.vue'

defineProps({
  user: {
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
.user-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--el-border-color-light);
}

.user-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-5);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.user-desc {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.user-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.user-stats strong {
  color: var(--el-text-color-primary);
  margin-right: 2px;
}
</style>
