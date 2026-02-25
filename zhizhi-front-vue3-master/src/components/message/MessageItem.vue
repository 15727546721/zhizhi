<template>
  <div
    class="message-item"
    :class="{
      'message-sent': isSent,
      'message-received': !isSent
    }"
  >
    <UserAvatar
      v-if="!isSent"
      :size="36"
      :src="message.senderAvatar"
      :nickname="message.senderNickname"
    />

    <div class="message-content-wrapper">
      <!-- 发送方看到的状态提示 -->
      <el-tooltip
        v-if="isSent && statusInfo.show"
        :content="statusInfo.tooltip"
        placement="top"
      >
        <span class="message-status-icon">
          <el-icon :color="statusInfo.color"><Warning /></el-icon>
        </span>
      </el-tooltip>

      <div
        class="message-bubble"
        :class="{ 
          'message-bubble--image': parsedContent.type === 'image',
          'message-bubble--pending': isSent && message.status === 2,
          'message-bubble--withdrawn': message.status === 4
        }"
        @contextmenu.prevent="handleContextMenu"
      >
        <template v-if="message.status === 4">
          <span class="message-text message-withdrawn-text">消息已撤回</span>
        </template>
        <template v-else-if="parsedContent.type === 'image'">
          <el-image
            class="message-image"
            :src="parsedContent.url"
            :preview-src-list="[parsedContent.url]"
            fit="cover"
          />
        </template>
        <template v-else>
          <span class="message-text">{{ message.content }}</span>
        </template>
      </div>
      
      <!-- 打招呼消息的底部提示 -->
      <div v-if="isSent && message.status === 2" class="message-pending-tip">
        对方回复或关注你后可继续发送
      </div>
      
      <!-- 撤回按钮（2分钟内可撤回） -->
      <div v-if="isSent && canWithdraw && message.status !== 4" class="message-actions">
        <el-button type="text" size="small" @click="handleWithdraw">撤回</el-button>
      </div>
    </div>

    <UserAvatar
      v-if="isSent"
      :size="36"
      :src="currentUserAvatar"
      :nickname="currentUserNickname"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { PrivateMessage } from '@/types'
import UserAvatar from '@/components/UserAvatar.vue'
import { withdrawMessage } from '@/api/message'

defineOptions({ name: 'MessageItem' })

interface Props {
  message: PrivateMessage
  currentUserId: number
  currentUserAvatar?: string
  currentUserNickname?: string
}

const props = withDefaults(defineProps<Props>(), {
  currentUserAvatar: '',
  currentUserNickname: ''
})

const emit = defineEmits<{
  withdrawn: [messageId: number]
}>()

const isSent = computed(() => props.message.senderId === props.currentUserId)

// 是否可以撤回（2分钟内）
const canWithdraw = computed(() => {
  if (!props.message.createTime) return false
  const createTime = new Date(props.message.createTime).getTime()
  const now = Date.now()
  return now - createTime < 2 * 60 * 1000 // 2分钟
})

// 消息状态信息
const statusInfo = computed(() => {
  const status = props.message.status
  if (status === 2) {
    return {
      show: true,
      color: '#faad14',
      tooltip: '打招呼消息，对方回复或关注你后可继续发送'
    }
  }
  if (status === 3) {
    return {
      show: true,
      color: '#ff4d4f',
      tooltip: '对方已设置消息权限，消息可能不可见'
    }
  }
  return { show: false, color: '', tooltip: '' }
})

const parsedContent = computed(() => {
  const content = props.message.content
  if (!content) return { type: 'text' as const, content: '' }
  
  // 优先尝试 JSON 格式（后端标准格式）
  if (content.trim().startsWith('{')) {
    try {
      const parsed = JSON.parse(content)
      if (parsed.type === 'image' && parsed.url) {
        return { type: 'image' as const, url: parsed.url }
      }
    } catch {
      // 解析失败，继续尝试其他格式
    }
  }
  
  // 兼容旧的 [img]url[/img] 格式
  const imageMatch = content.match(/\[img\](.*?)\[\/img\]/)
  if (imageMatch) return { type: 'image' as const, url: imageMatch[1] }
  
  return { type: 'text' as const, content }
})

// 撤回消息
const handleWithdraw = async () => {
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '撤回消息', {
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await withdrawMessage(props.message.messageId)
    if (res.code === 20000) {
      ElMessage.success('消息已撤回')
      emit('withdrawn', props.message.messageId)
    } else {
      ElMessage.error((res as any).info || '撤回失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回消息失败:', error)
    }
  }
}

// 右键菜单（预留）
const handleContextMenu = () => {
  // 可以扩展右键菜单功能
}
</script>

<style scoped>
.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 8px;
}

.message-item.message-sent {
  flex-direction: row-reverse;
}

.message-content-wrapper {
  max-width: 60%;
  position: relative;
}

.message-status-icon {
  position: absolute;
  top: -8px;
  left: -16px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  word-break: break-word;
}

.message-sent .message-bubble {
  background: #1890ff;
  color: #fff;
}

.message-bubble--image {
  padding: 4px;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

/* 打招呼消息样式 */
.message-bubble--pending {
  border: 1px dashed #faad14;
}

.message-pending-tip {
  font-size: 11px;
  color: #faad14;
  margin-top: 4px;
  text-align: right;
}

/* 已撤回消息样式 */
.message-bubble--withdrawn {
  background: #f5f5f5 !important;
  color: #999 !important;
}

.message-withdrawn-text {
  font-style: italic;
  color: #999;
}

/* 消息操作按钮 */
.message-actions {
  margin-top: 4px;
  text-align: right;
}

.message-actions .el-button {
  padding: 0;
  font-size: 12px;
  color: #909399;
}

.message-actions .el-button:hover {
  color: #409eff;
}
</style>
