<template>
  <div class="app-container">
    <!-- 公告通知条 -->
    <AnnouncementBar />
    
    <base-layout>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </base-layout>
    <!-- 将登录弹窗移到BaseLayout外部，确保它能正确全屏显示 -->
    <LoginDialog v-model="showLoginDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, provide, onMounted, onUnmounted, watch, type Ref } from 'vue'
import BaseLayout from '@/components/layout/BaseLayout.vue'
import LoginDialog from '@/components/auth/LoginDialog.vue'
import AnnouncementBar from '@/components/common/AnnouncementBar.vue'
import { useUserStore } from '@/stores/module/user'
import { connectWebSocket, disconnectWebSocket } from '@/utils/websocket'

interface LoginDialogControl {
  show: () => void
  hide: () => void
  value: Ref<boolean>
}

const userStore = useUserStore()

const showLoginDialog = ref(false)

const handleShowLoginDialog = () => {
  showLoginDialog.value = true
}

provide<LoginDialogControl>('showLoginDialog', {
  show: () => (showLoginDialog.value = true),
  hide: () => (showLoginDialog.value = false),
  value: showLoginDialog
})

onMounted(() => {
  window.addEventListener('show-login-dialog', handleShowLoginDialog)

  if (userStore.isAuthenticated) {
    connectWebSocket()
  }
})

onUnmounted(() => {
  window.removeEventListener('show-login-dialog', handleShowLoginDialog)
  disconnectWebSocket()
})

watch(
  () => userStore.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      connectWebSocket()
    } else {
      disconnectWebSocket()
    }
  }
)
</script>

<style>
:root {
  --header-height: 60px;
  --announcement-bar-height: 48px;
  --primary-color: #409eff;
  --text-color-primary: #303133;
  --text-color-regular: #606266;
  --text-color-secondary: #909399;
  --border-color: #dcdfe6;
  --bg-color: #f4f5f5;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell,
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  color: var(--text-color-primary);
  background-color: var(--bg-color);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  position: relative;
}

.app-container {
  width: 100%;
  min-height: 100vh;
  overflow-y: auto; /* 确保内容可以滚动 */
}

/* 防止模态框打开时页面滚动 */
body.modal-open {
  position: fixed;
  width: 100%;
}
</style>
