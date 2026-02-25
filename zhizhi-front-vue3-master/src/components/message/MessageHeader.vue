<template>
  <div class="message-header">
    <div class="message-header-left" @click="$emit('goProfile')">
      <UserAvatar :size="32" :src="avatar" :nickname="userName" />
      <span class="message-header-name">{{ userName || '用户' }}</span>
    </div>
    <div class="message-header-right">
      <FollowButton v-if="userId && showFollowButton" :user-id="userId" size="small" />
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button :icon="More" circle size="small" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="!hasBlocked" command="block">屏蔽用户</el-dropdown-item>
            <el-dropdown-item v-else command="unblock">取消屏蔽</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { More } from '@element-plus/icons-vue'
import FollowButton from '@/components/FollowButton/index.vue'
import UserAvatar from '@/components/UserAvatar.vue'

defineOptions({ name: 'MessageHeader' })

interface Props {
  userId: number | null
  userName?: string
  avatar?: string
  hasBlocked?: boolean
  showFollowButton?: boolean
}

withDefaults(defineProps<Props>(), {
  userName: '',
  avatar: '',
  hasBlocked: false,
  showFollowButton: true
})

const emit = defineEmits<{
  goProfile: []
  block: []
  unblock: []
}>()

const handleCommand = (command: string) => {
  if (command === 'block') {
    emit('block')
  } else if (command === 'unblock') {
    emit('unblock')
  }
}
</script>

<style scoped>
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.message-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.message-header-name {
  font-size: 14px;
  font-weight: 500;
}

.message-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
