<template>
  <div class="announcement-bar" v-if="showAnnouncement">
    <div class="announcement-content">
      <el-icon class="announcement-icon"><Bell /></el-icon>
      <span class="announcement-text">{{ announcementText }}</span>
      <a class="announcement-link" @click="handleAnnouncementClick">
        <span>查看详情</span>
        <el-icon><ArrowRight /></el-icon>
      </a>
      <el-icon class="close-icon" @click="closeAnnouncement"><Close /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Bell, Close, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const announcementText = ref('🎆 知之平台全新上线，体验丰富的技术内容与精彩互动！')
const announcementId = '2023-10-26-v1.11'
const showAnnouncement = ref(true)

onMounted(() => {
  const closedAnnouncements: string[] = JSON.parse(
    localStorage.getItem('closedAnnouncements') || '[]'
  )
  if (closedAnnouncements.includes(announcementId)) {
    showAnnouncement.value = false
    document.documentElement.style.setProperty('--announcement-bar-height', '0px')
    const appContainer = document.querySelector('.app-container')
    if (appContainer) {
      appContainer.classList.add('announcement-closed')
    }
  }
})

const handleAnnouncementClick = () => {
  router.push('/announcements')
}

const closeAnnouncement = () => {
  showAnnouncement.value = false

  const closedAnnouncements: string[] = JSON.parse(
    localStorage.getItem('closedAnnouncements') || '[]'
  )
  if (!closedAnnouncements.includes(announcementId)) {
    closedAnnouncements.push(announcementId)
    localStorage.setItem('closedAnnouncements', JSON.stringify(closedAnnouncements))
  }

  document.documentElement.style.setProperty('--announcement-bar-height', '0px')
  const appContainer = document.querySelector('.app-container')
  if (appContainer) {
    appContainer.classList.add('announcement-closed')
  }
}
</script>

<style scoped>
.announcement-bar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(230, 235, 241, 0.8);
  color: #515767;
  height: 48px; /* 调整高度，更精致 */
  display: flex;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  animation: slideDown 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transition: all 0.3s ease;
}

/* 关闭公告条后调整header和main-wrapper位置 */
:global(.app-container.announcement-closed .header) {
  top: 0 !important;
}

:global(.app-container.announcement-closed .main-wrapper) {
  padding-top: var(--header-height) !important;
}

.announcement-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  font-weight: 500;
  width: 100%;
}

.announcement-icon {
  font-size: 16px;
  color: #1e80ff;
  flex-shrink: 0;
  animation: bellRing 3s ease-in-out infinite;
}

.announcement-text {
  flex: 1;
  color: #1d2129;
  font-weight: 500;
  letter-spacing: 0.3px;
  animation: textSlide 0.6s ease-out;
}

.announcement-link {
  color: #1e80ff;
  background: linear-gradient(135deg, #e8f3ff, #f0f8ff);
  border: 1px solid rgba(30, 128, 255, 0.2);
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.announcement-link:hover {
  background: linear-gradient(135deg, #1e80ff, #409eff);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 128, 255, 0.25);
  border-color: #1e80ff;
}

.close-icon {
  font-size: 16px;
  color: #86909c;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  padding: 6px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon:hover {
  color: #515767;
  background: rgba(134, 144, 156, 0.1);
  transform: rotate(90deg);
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes bellRing {
  0%, 100% {
    transform: rotate(0deg);
  }
  5%, 15%, 25% {
    transform: rotate(-8deg);
  }
  10%, 20% {
    transform: rotate(8deg);
  }
}

@keyframes textSlide {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .announcement-bar {
    height: 44px;
  }
  
  .announcement-content {
    padding: 0 16px;
    gap: 8px;
    font-size: 13px;
  }
  
  .announcement-text {
    font-size: 13px;
  }
  
  .announcement-link {
    padding: 4px 12px;
    font-size: 12px;
  }
  
  .announcement-icon {
    font-size: 14px;
  }
  
  .close-icon {
    font-size: 14px;
    padding: 4px;
  }
}

@media screen and (max-width: 480px) {
  .announcement-text {
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .announcement-link {
    padding: 3px 10px;
    font-size: 11px;
  }
}
</style>