<template>
  <div class="floating-buttons">
    <div class="button-group">
      <!-- 意见反馈 -->
      <el-tooltip content="意见反馈" placement="left" effect="dark">
        <div class="floating-button feedback-btn" @click="handleFeedback">
          <el-icon>
            <ChatLineSquare/>
          </el-icon>
        </div>
      </el-tooltip>

      <!-- 我的反馈 -->
      <el-tooltip content="我的反馈" placement="left" effect="dark">
        <div class="floating-button" @click="handleHelp">
          <el-icon>
            <QuestionFilled/>
          </el-icon>
        </div>
      </el-tooltip>

      <!-- 回到顶部 -->
      <el-tooltip content="回到顶部" placement="left" effect="dark">
        <div class="floating-button back-to-top" @click="scrollToTop" v-show="showBackToTop">
          <el-icon>
            <Top/>
          </el-icon>
        </div>
      </el-tooltip>
    </div>
  </div>

  <!-- 反馈弹窗 -->
  <FeedbackDialog v-model="showFeedbackDialog" @success="handleFeedbackSuccess" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatLineSquare, QuestionFilled, Top } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import { ElMessage } from 'element-plus'
import FeedbackDialog from '@/components/FeedbackDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const showBackToTop = ref(false)
const scrollThreshold = 100

const handleScroll = () => {
  showBackToTop.value = window.pageYOffset > scrollThreshold
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const showFeedbackDialog = ref(false)

const handleFeedback = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再提交反馈')
    return
  }
  showFeedbackDialog.value = true
}

const handleFeedbackSuccess = () => {
  // 反馈成功后的处理
}

const handleHelp = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后查看反馈')
    return
  }
  router.push('/feedback')
}
</script>

<style scoped>
.floating-buttons {
  position: fixed;
  right: 20px;
  bottom: 100px;
  z-index: 999;
  pointer-events: none;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
  pointer-events: auto;
}

.floating-button {
  width: 44px;
  height: 44px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  transform: translateZ(0);
}

.floating-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  background: var(--el-color-primary);
  color: #fff;
}

.floating-button .el-icon {
  font-size: 20px;
  color: var(--el-text-color-regular);
}

.floating-button:hover .el-icon {
  color: #fff;
}

/* 反馈按钮特殊样式 - hover 时显示蓝色 */
.floating-button.feedback-btn:hover {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.floating-button.feedback-btn:hover .el-icon {
  color: #fff;
}

.back-to-top {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media screen and (max-width: 768px) {
  .floating-buttons {
    right: 16px;
    bottom: 80px;
  }

  .floating-button {
    width: 40px;
    height: 40px;
  }

  .floating-button .el-icon {
    font-size: 18px;
  }
}
</style>
