<template>
  <div class="sidebar-actions">
    <div class="action-list">
      <div class="action-item">
        <el-tooltip content="点赞" placement="right" :show-after="200" :hide-after="200">
          <el-badge :value="(post && post.likeCount) || 0" :class="{ active: isLiked }">
            <el-button
                circle
                :type="isLiked ? 'primary' : 'default'"
                :class="{ active: isLiked }"
                :loading="isLiking"
                @click="handleLike"
            >
              <CustomIcon name="thumb-up" :active="isLiked" :size="16" />
            </el-button>
          </el-badge>
        </el-tooltip>
      </div>
      <div class="action-item">
        <el-tooltip content="评论" placement="right" :show-after="200" :hide-after="200">
          <el-badge :value="(post && post.commentCount) || 0">
            <el-button circle @click="scrollToComments">
              <el-icon>
                <ChatDotRound />
              </el-icon>
            </el-button>
          </el-badge>
        </el-tooltip>
      </div>
      <div class="action-item">
        <el-tooltip content="收藏" placement="right" :show-after="200" :hide-after="200">
          <el-badge
                  :value="(post && post.favoriteCount) || 0"
                  :class="{ active: isFavorited }"
              >
                <el-button
                    circle
                    :class="{ active: isFavorited }"
                    @click="handleFavorite"
                >
              <el-icon>
                <Star />
              </el-icon>
            </el-button>
          </el-badge>
        </el-tooltip>
      </div>
      <div class="action-item">
        <el-tooltip content="分享" placement="right" :show-after="200" :hide-after="200">
          <el-badge :value="(post && post.shareCount) || 0">
            <el-dropdown trigger="click" @command="handleShareCommand">
              <el-button circle>
                <el-icon>
                  <Share />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="copy">
                    <el-icon><Link /></el-icon>
                    <span>复制链接</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="weibo">
                    <span class="share-icon weibo">微</span>
                    <span>分享到微博</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="qq">
                    <span class="share-icon qq">Q</span>
                    <span>分享到QQ</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="wechat">
                    <span class="share-icon wechat">微</span>
                    <span>微信扫码分享</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-badge>
        </el-tooltip>
      </div>
      <div class="action-item">
        <el-tooltip content="举报" placement="right" :show-after="200" :hide-after="200">
          <el-button circle @click="handleReport">
            <el-icon>
              <Warning />
            </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ChatDotRound, Star, Warning, Share, Link } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CustomIcon from '@/components/CustomIcon/index.vue'
import { useUserStore } from '@/stores/module/user'
import { sharePost, SharePlatform } from '@/api/share'

// 类型定义
interface PostData {
  id?: number
  title?: string
  description?: string
  likeCount?: number
  commentCount?: number
  favoriteCount?: number
  shareCount?: number
  isLiked?: boolean
  isFavorited?: boolean
}

interface Props {
  post: PostData
  isLiking?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isLiking: false
})

const emit = defineEmits<{
  like: []
  favorite: []
  report: []
  scrollToComments: []
  share: [type: string]
  shareSuccess: []
}>()

// 直接使用props.post中的数据，移除冗余的状态管理
// 计算点赞和收藏状态
const isLiked = computed(() => Boolean(props.post?.isLiked))
const isFavorited = computed(() => Boolean(props.post?.isFavorited))

// 获取帖子分享URL
const getShareUrl = (): string => {
  const postId = props.post?.id
  if (!postId) return window.location.href
  return `${window.location.origin}/post/${postId}`
}

// 获取分享标题
const getShareTitle = (): string => {
  return props.post?.title || '知知社区 - 发现更多精彩内容'
}

// 记录分享到后端
const recordShare = async (platform: string): Promise<void> => {
  const postId = props.post?.id
  if (!postId) return

  try {
    const res = await sharePost({ postId, platform })
    // 只有当计数真正增加时才通知父组件更新
    if ((res.data as any)?.countIncreased) {
      emit('shareSuccess')
    }
  } catch (err) {
    // 分享记录失败不影响用户体验，静默处理
  }
}

// 复制链接到剪贴板
const copyLink = async (): Promise<void> => {
  const url = getShareUrl()
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
    // 记录分享
    recordShare(SharePlatform.COPY)
  } catch (err) {
    // 降级方案：使用 execCommand
    const textArea = document.createElement('textarea')
    textArea.value = url
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制到剪贴板')
      // 记录分享
      recordShare(SharePlatform.COPY)
    } catch (e) {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textArea)
  }
}

// 分享到微博
const shareToWeibo = (): void => {
  const url = getShareUrl()
  const title = getShareTitle()
  const weiboUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(url)}&title=${encodeURIComponent(title)}`
  window.open(weiboUrl, '_blank', 'width=600,height=500')
  // 记录分享
  recordShare(SharePlatform.WEIBO)
}

// 分享到QQ
const shareToQQ = (): void => {
  const url = getShareUrl()
  const title = getShareTitle()
  const desc = props.post?.description || title
  const qqUrl = `https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(url)}&title=${encodeURIComponent(title)}&desc=${encodeURIComponent(desc)}`
  window.open(qqUrl, '_blank', 'width=600,height=500')
  // 记录分享
  recordShare(SharePlatform.QQ)
}

// 分享到微信（显示二维码）
const shareToWechat = (): void => {
  emit('share', 'wechat')
  // 记录分享
  recordShare(SharePlatform.WECHAT)
}

// 处理分享命令
const handleShareCommand = (command: string): void => {
  switch (command) {
    case 'copy':
      copyLink()
      break
    case 'weibo':
      shareToWeibo()
      break
    case 'qq':
      shareToQQ()
      break
    case 'wechat':
      shareToWechat()
      break
  }
}

// 处理点赞
const handleLike = (): void => {
  const userStore = useUserStore()
  const userId = userStore.userInfo.id

  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  emit('like')
}

// 处理收藏
const handleFavorite = (): void => {
  emit('favorite')
}

// 滚动到评论区
const scrollToComments = (): void => {
  emit('scrollToComments')
}

// 处理举报 - 直接emit事件，由父组件打开举报弹窗
const handleReport = (): void => {
  emit('report')
}
</script>

<style scoped>
/* 左侧悬浮操作栏 */
.sidebar-actions {
  width: 60px;
  z-index: 2;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px;
  gap: 4px;
}

/* 按钮样式 */
.action-item :deep(.el-button) {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

.action-item :deep(.el-button:hover) {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
  transform: translateY(-2px);
}

.action-item :deep(.el-button.active) {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.action-item :deep(.el-button.is-loading) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

/* 激活状态下的primary类型按钮样式 */
.action-item :deep(.el-button.active[type="primary"]) {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

/* Badge 样式 */
.action-item :deep(.el-badge__content) {
  background-color: #8a919f;
  border: 2px solid #fff;
  font-size: 12px;
  height: 16px;
  min-width: 16px;
  padding: 0 4px;
  border-radius: 8px;
  font-weight: 500;
  transform: translate(45%, -35%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-item :deep(.el-badge.active .el-badge__content) {
  background-color: #409eff;
  border-color: #fff;
}

/* 分享下拉菜单样式 */
.action-item :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

.share-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}

.share-icon.weibo {
  background: linear-gradient(135deg, #e6162d 0%, #f5373b 100%);
}

.share-icon.qq {
  background: linear-gradient(135deg, #12b7f5 0%, #1da1f2 100%);
}

.share-icon.wechat {
  background: linear-gradient(135deg, #07c160 0%, #2aae67 100%);
}

/* 响应式布局 */
@media screen and (max-width: 1200px) {
  .sidebar-actions {
    display: none;
  }
}
</style>