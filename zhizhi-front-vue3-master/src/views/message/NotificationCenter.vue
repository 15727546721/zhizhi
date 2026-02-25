<template>
  <div class="notification-center">
    <div class="notification-container">
      <!-- 左侧导航 -->
      <div class="nav-sidebar">
        <div class="nav-title">消息中心</div>
        <div class="nav-menu">
          <div
            v-for="tab in tabs"
            :key="tab.key"
            :class="['nav-item', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            <el-icon><component :is="tab.icon" /></el-icon>
            <span>{{ tab.label }}</span>
            <span v-if="unreadCounts[tab.key] > 0" class="unread-badge">
              {{ unreadCounts[tab.key] > 99 ? '99+' : unreadCounts[tab.key] }}
            </span>
          </div>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <div class="content-header">
          <h2>{{ currentTabLabel }}</h2>
          <el-button link type="primary" @click="handleMarkAllAsRead" :disabled="!hasUnread">
            全部已读
          </el-button>
        </div>

        <!-- 通知列表 -->
        <div class="notification-list" v-loading="loading">
          <template v-if="notifications.length > 0">
            <div
              v-for="item in notifications"
              :key="item.id"
              :class="['notification-item', { unread: !item.read }]"
              @click="handleItemClick(item)"
            >
              <div class="item-avatar">
                <UserAvatar
                  :src="item.senderAvatar"
                  :nickname="item.senderName || '系统'"
                  :size="44"
                />
              </div>
              <div class="item-content">
                <div class="item-header">
                  <span class="sender-name">{{ item.senderName || '系统通知' }}</span>
                  <span class="action-text">{{ getActionText(item) }}</span>
                </div>
                <div class="item-body" v-if="item.content">
                  {{ item.content }}
                </div>
                <div class="item-footer">
                  <span class="item-time">{{ formatTime(item.createdTime) }}</span>
                  <div class="item-actions">
                    <el-button
                      v-if="!item.read"
                      link
                      size="small"
                      @click.stop="handleMarkAsRead(item)"
                    >
                      标记已读
                    </el-button>
                    <el-button link size="small" type="danger" @click.stop="handleDelete(item)">
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <el-empty v-else-if="!loading" description="暂无消息" />
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Bell, Present, UserFilled, Promotion } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar.vue'
import {
  getNotifications,
  getUnreadCountByType,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  NotificationType
} from '@/api/notification'
import { formatRelativeTime } from '@/utils/time'
import { eventBus, EVENT_REFRESH_NOTIFICATION_COUNT } from '@/utils/eventBus'

const router = useRouter()

// Tab配置
const tabs = [
  { key: 'replies', label: '回复我的', types: [3, 4], icon: shallowRef(ChatDotRound) },
  { key: 'mentions', label: '@我的', types: [6], icon: shallowRef(Promotion) },
  { key: 'likes', label: '收到的赞', types: [1, 2], icon: shallowRef(Present) },
  { key: 'follows', label: '新增粉丝', types: [5], icon: shallowRef(UserFilled) },
  { key: 'system', label: '系统通知', types: [0], icon: shallowRef(Bell) }
]

// 状态
const activeTab = ref('replies')
const notifications = ref([])
// 初始化所有 tab 的未读数为 0
const unreadCounts = ref({
  replies: 0,
  mentions: 0,
  likes: 0,
  follows: 0,
  system: 0
})
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)

// 计算属性
const currentTabLabel = computed(() => {
  const tab = tabs.find((t) => t.key === activeTab.value)
  return tab ? tab.label : ''
})

const hasUnread = computed(() => {
  return unreadCounts.value[activeTab.value] > 0
})

// 获取当前Tab的类型参数
const getCurrentTypeParam = () => {
  const tab = tabs.find((t) => t.key === activeTab.value)
  return tab ? tab.types.join(',') : null
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const type = getCurrentTypeParam()
    const res = await getNotifications({
      type,
      pageNo: currentPage.value,
      pageSize
    })

    if (res.code === 20000 && res.data) {
      notifications.value = res.data.data || []
      total.value = res.data.total || 0
    } else {
      notifications.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCounts = async () => {
  try {
    const res = await getUnreadCountByType()
    if (res.code === 20000) {
      const counts = res.data || {}
      // 按Tab聚合未读数（注意：JSON返回的key是字符串）
      tabs.forEach((tab) => {
        const tabCount = tab.types.reduce((sum, t) => {
          const strKey = String(t)
          const typeCount = Number(counts[strKey]) || Number(counts[t]) || 0
          return sum + typeCount
        }, 0)
        unreadCounts.value[tab.key] = tabCount
      })
    }
  } catch (error) {
    // 加载失败
  }
}

// 切换Tab
const switchTab = (key) => {
  activeTab.value = key
  currentPage.value = 1
  loadNotifications()
}

// 分页
const handlePageChange = (page) => {
  currentPage.value = page
  loadNotifications()
}

// 标记单个已读
const handleMarkAsRead = async (item, showMessage = true) => {
  try {
    await markNotificationAsRead(item.id)
    item.read = true
    // 更新未读数
    const tabKey = activeTab.value
    if (unreadCounts.value[tabKey] > 0) {
      unreadCounts.value[tabKey]--
    }
    // 通知顶部导航栏刷新通知未读数
    eventBus.emit(EVENT_REFRESH_NOTIFICATION_COUNT)
    if (showMessage) {
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    // 忽略请求取消错误（页面跳转导致）
    if (error?.code === 'ERR_CANCELED') {
      return
    }
    if (showMessage) {
      ElMessage.error('操作失败')
    }
  }
}

// 全部已读
const handleMarkAllAsRead = async () => {
  try {
    const tab = tabs.find((t) => t.key === activeTab.value)
    // 对当前Tab的所有类型都标记为已读
    for (const type of tab.types) {
      await markAllNotificationsAsRead(type)
    }
    // 刷新列表
    await loadNotifications()
    await loadUnreadCounts()
    // 通知顶部导航栏刷新通知未读数
    eventBus.emit(EVENT_REFRESH_NOTIFICATION_COUNT)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除通知
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteNotification(item.id)
    // 从列表移除
    notifications.value = notifications.value.filter((n) => n.id !== item.id)
    total.value--
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 点击通知项
const handleItemClick = (item) => {
  // 标记已读（静默，不显示提示）
  if (!item.read) {
    handleMarkAsRead(item, false)
  }
  // 跳转到对应页面
  if (item.businessType === 1 && item.businessId) {
    // 帖子
    router.push(`/post/${item.businessId}`)
  } else if (item.businessType === 3 && item.businessId) {
    // 用户
    router.push(`/user/${item.businessId}`)
  }
}

// 获取动作文本
const getActionText = (item) => {
  const typeTexts = {
    [NotificationType.SYSTEM]: '',
    [NotificationType.LIKE]: '赞了你的内容',
    [NotificationType.FAVORITE]: '收藏了你的帖子',
    [NotificationType.COMMENT]: '评论了你的帖子',
    [NotificationType.REPLY]: '回复了你',
    [NotificationType.FOLLOW]: '关注了你',
    [NotificationType.MENTION]: '在评论中@了你'
  }
  return typeTexts[item.type] || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return formatRelativeTime(time)
}

// 初始化
onMounted(() => {
  loadNotifications()
  loadUnreadCounts()
})
</script>

<style scoped>
.notification-center {
  max-width: 1100px;
  margin: 20px auto;
  padding: 0 20px;
}

.notification-container {
  display: flex;
  gap: 20px;
  min-height: 600px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 左侧导航 */
.nav-sidebar {
  width: 180px;
  padding: 20px 0;
  border-right: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.nav-title {
  padding: 0 20px 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-menu {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #409eff;
  border-radius: 0 2px 2px 0;
}

.unread-badge {
  margin-left: auto;
  background: #ff4d4f;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.content-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 通知列表 */
.notification-list {
  flex: 1;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #fafafa;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-item.unread:hover {
  background: #e6f0fa;
}

.item-avatar {
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  margin-bottom: 6px;
}

.sender-name {
  font-weight: 500;
  color: #333;
  margin-right: 8px;
}

.action-text {
  color: #666;
}

.item-body {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-time {
  color: #999;
  font-size: 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
}
</style>
