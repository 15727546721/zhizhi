<template>
  <el-dialog
    v-model="visible"
    title="微信扫码分享"
    width="380px"
    :close-on-click-modal="true"
    class="share-qr-dialog"
    @opened="generateQRCode"
  >
    <div class="qr-content">
      <div class="qr-wrapper">
        <canvas ref="qrCanvas"></canvas>
      </div>
      <p class="qr-tip">
        <el-icon class="wechat-icon"><ChatDotRound /></el-icon>
        打开微信扫一扫，分享给好友
      </p>
      <div class="share-info">
        <p class="share-title" :title="shareTitle">{{ shareTitle }}</p>
        <p class="share-url">{{ shareUrl }}</p>
      </div>
      <el-button type="primary" @click="copyUrl" class="copy-btn">
        <el-icon><Link /></el-icon>
        复制链接
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Link, ChatDotRound } from '@element-plus/icons-vue'
import QRCode from 'qrcode'

const visible = ref(false)
const qrCanvas = ref<HTMLCanvasElement | null>(null)
const shareUrl = ref('')
const shareTitle = ref('')

// 生成二维码
const generateQRCode = async () => {
  await nextTick()
  if (!qrCanvas.value || !shareUrl.value) return
  
  try {
    await QRCode.toCanvas(qrCanvas.value, shareUrl.value, {
      width: 200,
      margin: 2,
      color: {
        dark: '#333333',
        light: '#ffffff'
      }
    })
  } catch (err) {
    console.error('生成二维码失败:', err)
    ElMessage.error('生成二维码失败')
  }
}

// 复制链接
const copyUrl = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制，快去分享给好友吧')
  } catch (err) {
    const textArea = document.createElement('textarea')
    textArea.value = shareUrl.value
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制，快去分享给好友吧')
    } catch (e) {
      ElMessage.error('复制失败')
    }
    document.body.removeChild(textArea)
  }
}

// 打开弹窗
const open = (url: string, title?: string) => {
  shareUrl.value = url
  shareTitle.value = title || '知知社区'
  visible.value = true
}

// 关闭弹窗
const close = () => {
  visible.value = false
}

defineExpose({ open, close })
</script>

<style scoped>
.share-qr-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.qr-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.qr-wrapper {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.qr-wrapper canvas {
  display: block;
}

.qr-tip {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.wechat-icon {
  color: #07c160;
  font-size: 18px;
}

.share-info {
  margin-top: 16px;
  width: 100%;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.share-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.share-url {
  font-size: 12px;
  color: #999;
  margin: 0;
  word-break: break-all;
}

.copy-btn {
  margin-top: 16px;
  width: 100%;
}
</style>
