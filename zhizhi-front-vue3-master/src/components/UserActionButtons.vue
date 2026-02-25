<template>
  <div class="user-action-buttons" :class="{ 'vertical': layout === 'vertical' }">
    <!-- 关注按钮 -->
    <el-button
      v-if="showFollow"
      type="primary"
      class="follow-btn"
      :class="{ 'is-followed': isFollowing }"
      :loading="followLoading"
      :size="size"
      @click="handleFollow"
    >
      <template v-if="isFollowing">
        <el-icon><Check /></el-icon>
        已关注
      </template>
      <template v-else>
        <el-icon><Plus /></el-icon>
        关注
      </template>
    </el-button>

    <!-- 私信按钮 -->
    <el-button
      v-if="showMessage"
      class="message-btn"
      :size="size"
      @click="handleMessage"
    >
      <el-icon><Message /></el-icon>
      私信
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Check, Plus, Message } from '@element-plus/icons-vue'

type LayoutType = 'horizontal' | 'vertical'
type SizeType = 'large' | 'default' | 'small'

interface Props {
  isFollowing?: boolean
  followLoading?: boolean
  showFollow?: boolean
  showMessage?: boolean
  layout?: LayoutType
  size?: SizeType
}

withDefaults(defineProps<Props>(), {
  isFollowing: false,
  followLoading: false,
  showFollow: true,
  showMessage: true,
  layout: 'horizontal',
  size: 'default'
})

const emit = defineEmits<{
  follow: []
  message: []
}>()

const handleFollow = () => {
  emit('follow')
}

const handleMessage = () => {
  emit('message')
}
</script>

<style scoped>
.user-action-buttons {
  display: flex;
  gap: 12px;
}

.user-action-buttons.vertical {
  flex-direction: column;
}

.follow-btn,
.message-btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s ease;
}

/* 小尺寸按钮 */
.user-action-buttons .el-button--small.follow-btn,
.user-action-buttons .el-button--small.message-btn {
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 16px;
}

/* 大尺寸按钮 */
.user-action-buttons .el-button--large.follow-btn,
.user-action-buttons .el-button--large.message-btn {
  height: 44px;
  padding: 0 24px;
  font-size: 16px;
  border-radius: 22px;
}

/* 关注按钮 - 未关注状态 */
.follow-btn {
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  border: none;
  color: white;
}

.follow-btn:hover {
  background: linear-gradient(135deg, #66b1ff, #409eff);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 关注按钮 - 已关注状态 */
.follow-btn.is-followed {
  background: #f0f2f5;
  border-color: #f0f2f5;
  color: #606266;
}

.follow-btn.is-followed:hover {
  background-color: #e0e3e9;
  border-color: #e0e3e9;
  transform: translateY(-1px);
}

/* 私信按钮 */
.message-btn {
  background: #f4f5f5;
  border: none;
  color: #409eff;
}

.message-btn:hover {
  background: #e8e9eb;
  color: #3071e7;
  transform: translateY(-1px);
}

.message-btn .el-icon {
  font-size: 16px;
}

/* 水平布局时，按钮宽度自适应 */
.user-action-buttons:not(.vertical) .follow-btn,
.user-action-buttons:not(.vertical) .message-btn {
  flex: 1;
  min-width: 100px;
}

/* 垂直布局时，按钮占满宽度 */
.user-action-buttons.vertical .follow-btn,
.user-action-buttons.vertical .message-btn {
  width: 100%;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .user-action-buttons {
    width: 100%;
  }

  .follow-btn,
  .message-btn {
    flex: 1;
  }
}
</style>
