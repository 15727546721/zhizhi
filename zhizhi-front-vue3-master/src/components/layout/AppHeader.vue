<template>
  <div class="app-header" :class="{ 'is-hidden': isHidden }">
    <div class="header-container">
      <!-- Logo区域 -->
      <div class="logo-section">
        <router-link to="/" class="logo">
          <div class="logo-icon">知</div>
          <span class="logo-text">知之</span>
        </router-link>
      </div>

      <!-- 主导航区域 -->
      <nav class="main-nav">
        <div class="nav-tabs">
          <div 
            v-if="isLoggedIn"
            class="nav-tab-item" 
            :class="{ 'is-active': currentTab === 'following' }"
            @click="handleNavClick('following')"
          >
            关注
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': currentTab === 'latest' }"
            @click="handleNavClick('latest')"
          >
            最新
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': currentTab === 'hot' }"
            @click="handleNavClick('hot')"
          >
            最热
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': currentTab === 'featured' }"
            @click="handleNavClick('featured')"
          >
            精选
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': isActive('/ranking') }"
            @click="router.push('/ranking')"
          >
            排行榜
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': isActive('/tags') }"
            @click="router.push('/tags')"
          >
            标签
          </div>
          <div 
            class="nav-tab-item" 
            :class="{ 'is-active': isActive('/columns') }"
            @click="router.push('/columns')"
          >
            专栏
          </div>
        </div>
      </nav>

      <!-- 右侧功能区 -->
      <div class="header-right">
        <!-- 搜索框：在搜索页面时隐藏，避免与搜索页面的搜索框重复 -->
        <div class="search-box" v-if="!isSearchPage">
          <el-popover
            :visible="showSearchPopover"
            placement="bottom-end"
            :width="320"
            trigger="click"
            :show-arrow="false"
            popper-class="search-popover"
            :offset="4"
          >
            <template #reference>
              <el-input
                v-model="searchText"
                placeholder="搜索帖子"
                :prefix-icon="Search"
                @keyup.enter="handleSearch"
                @focus="handleSearchFocus"
                @blur="handleSearchBlur"
              />
            </template>
            
            <!-- 搜索历史 -->
            <div class="search-dropdown" v-if="searchHistory.length > 0 || hotWords.length > 0">
              <div class="dropdown-section" v-if="searchHistory.length > 0">
                <div class="section-header">
                  <span>搜索历史</span>
                  <el-button text size="small" @click.stop="handleClearHistory">清空</el-button>
                </div>
                <div class="keyword-tags">
                  <span 
                    v-for="item in searchHistory.slice(0, 8)" 
                    :key="item" 
                    class="keyword-tag"
                    @click="handleQuickSearch(item)"
                  >
                    {{ item }}
                  </span>
                </div>
              </div>
              
              <div class="dropdown-section" v-if="hotWords.length > 0">
                <div class="section-header">
                  <span>🔥 热门搜索</span>
                </div>
                <div class="hot-list">
                  <div 
                    v-for="(item, index) in hotWords.slice(0, 5)" 
                    :key="item" 
                    class="hot-item"
                    @click="handleQuickSearch(item)"
                  >
                    <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                    <span class="hot-text">{{ item }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-tips">暂无搜索历史</div>
          </el-popover>
        </div>

        <div class="user-section">
          <template v-if="isLoggedIn">
            <!-- 发布按钮 -->
            <el-dropdown trigger="hover" class="publish-dropdown" @command="handlePublishCommand">
              <el-button type="primary" class="publish-btn">
                <el-icon class="publish-icon">
                  <Plus />
                </el-icon>
                发布
              </el-button>

              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown">
                  <el-dropdown-item command="post">
                    <div class="dropdown-item">
                      <el-icon size="20">
                        <Document />
                      </el-icon>
                      <div class="item-content">
                        <div class="title">发帖子</div>
                        <div class="desc">分享你的想法和经验</div>
                      </div>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="draft">
                    <div class="dropdown-item">
                      <el-icon size="20">
                        <Document />
                      </el-icon>
                      <div class="item-content">
                        <div class="title">草稿箱</div>
                        <div class="desc">查看所有草稿</div>
                      </div>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 通知 -->
            <router-link to="/message" class="message-icon" title="通知">
              <el-badge :value="notificationUnread" :max="99" :hidden="notificationUnread === 0" class="message-badge">
                <el-icon :size="24"><Bell /></el-icon>
              </el-badge>
            </router-link>

            <!-- 私信 -->
            <router-link to="/private-messages" class="message-icon" title="私信">
              <el-badge :value="messageUnread" :max="99" :hidden="messageUnread === 0" class="message-badge">
                <el-icon :size="24"><Message /></el-icon>
              </el-badge>
            </router-link>

            <!-- 用户头像 -->
            <el-dropdown trigger="hover" @command="handleUserCommand">
              <UserAvatar 
                :src="userAvatar" 
                :username="userStore.getUserInfo?.username"
                :nickname="userStore.getUserInfo?.nickname"
                :size="32" 
              />
              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown user-dropdown">
                  <el-dropdown-item command="profile">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <User />
                      </el-icon>
                      <span>个人主页</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="posts">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <Collection />
                      </el-icon>
                      <span>我的帖子</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="columns">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <Folder />
                      </el-icon>
                      <span>我的专栏</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <Star />
                      </el-icon>
                      <span>我的收藏</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <Setting />
                      </el-icon>
                      <span>设置</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <div class="dropdown-item">
                      <el-icon size="16">
                        <SwitchButton />
                      </el-icon>
                      <span>退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" class="login-btn" round @click="showLoginDialog()">
              登录
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted, inject, type Ref } from 'vue'
import { useUserStore } from '@/stores/module/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import {
  Search,
  Plus,
  Document,
  User,
  Setting,
  SwitchButton,
  Collection,
  Message,
  Bell,
  Star,
  Folder
} from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { getUnreadNotificationCount } from '@/api/notification'
import { getTotalUnreadCount as getMessageUnreadCount } from '@/api/message'
import { getSearchHistory, getHotWords, clearSearchHistory } from '@/api/search'
import {
  onUnreadCount,
  offUnreadCount,
  onPrivateMessage,
  offPrivateMessage
} from '@/utils/websocket'
import {
  eventBus,
  EVENT_REFRESH_UNREAD_COUNT,
  EVENT_REFRESH_NOTIFICATION_COUNT,
  EVENT_REFRESH_MESSAGE_COUNT
} from '@/utils/eventBus'

interface LoginDialogControl {
  show: () => void
  hide: () => void
  value: Ref<boolean>
}

interface PrivateMessageData {
  type: string
  [key: string]: unknown
}

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const currentTab = computed(() => {
  if (route.path === '/' || route.path === '') {
    return (route.query.tab as string) || 'latest'
  }
  return ''
})

const handleNavClick = (tab: string) => {
  router.push({ path: '/', query: { tab } })
}

const isLoggedIn = computed(() => userStore.isAuthenticated)
const userAvatar = computed(() => userStore.getUserInfo?.avatar || '')
const searchText = ref('')
const isHidden = ref(false)
const showSearchPopover = ref(false)
const searchHistory = ref<string[]>([])
const hotWords = ref<string[]>([])

const isSearchPage = computed(() => route.path === '/search')

const loginDialogControl = inject<LoginDialogControl>('showLoginDialog')
const showLoginDialog = () => {
  if (loginDialogControl && loginDialogControl.show) {
    loginDialogControl.show()
  }
}

const handleScroll = () => {
  isHidden.value = window.scrollY > 0
}

const notificationUnread = ref(0)
const messageUnread = ref(0)
let updateTimer: ReturnType<typeof setInterval> | null = null

const handleUnreadCountUpdate = (count: number) => {
  notificationUnread.value = count
}

const handlePrivateMessageUpdate = (data: PrivateMessageData) => {
  if (data.type === 'private_message') {
    messageUnread.value++
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)

  if (userStore.isAuthenticated) {
    fetchUnreadCount()
  }

  updateTimer = setInterval(() => {
    if (userStore.isAuthenticated) {
      fetchUnreadCount()
    }
  }, 60000)

  onUnreadCount(handleUnreadCountUpdate)
  onPrivateMessage(handlePrivateMessageUpdate)
  eventBus.on(EVENT_REFRESH_UNREAD_COUNT, fetchUnreadCount)
  eventBus.on(EVENT_REFRESH_NOTIFICATION_COUNT, fetchNotificationUnreadCount)
  eventBus.on(EVENT_REFRESH_MESSAGE_COUNT, fetchMessageUnreadCount)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (updateTimer) {
    clearInterval(updateTimer)
  }
  offUnreadCount(handleUnreadCountUpdate)
  offPrivateMessage(handlePrivateMessageUpdate)
  eventBus.off(EVENT_REFRESH_UNREAD_COUNT, fetchUnreadCount)
  eventBus.off(EVENT_REFRESH_NOTIFICATION_COUNT, fetchNotificationUnreadCount)
  eventBus.off(EVENT_REFRESH_MESSAGE_COUNT, fetchMessageUnreadCount)
})

const handleSearch = async () => {
  if (!searchText.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  showSearchPopover.value = false
  router.push({
    path: '/search',
    query: { q: searchText.value.trim() }
  })
  searchText.value = ''
}

const handleSearchFocus = async () => {
  showSearchPopover.value = true
  try {
    const [historyRes, hotRes] = await Promise.all([getSearchHistory(), getHotWords()])
    if (historyRes.code === 20000 || historyRes.code === 200) {
      searchHistory.value = (historyRes.data as string[]) || []
    }
    if (hotRes.code === 20000 || hotRes.code === 200) {
      hotWords.value = (hotRes.data as string[]) || []
    }
  } catch (error) {
    console.error('加载搜索建议失败:', error)
  }
}

const handleSearchBlur = () => {
  setTimeout(() => {
    showSearchPopover.value = false
  }, 200)
}

const handleQuickSearch = (word: string) => {
  searchText.value = word
  showSearchPopover.value = false
  router.push({ path: '/search', query: { q: word } })
}

const handleClearHistory = async () => {
  try {
    await clearSearchHistory()
    searchHistory.value = []
    ElMessage.success('已清空')
  } catch (error) {
    console.error('清空历史失败:', error)
  }
}

const handlePublishCommand = (command: string) => {
  switch (command) {
    case 'post':
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录再发布帖子')
        showLoginDialog()
        return
      }
      router.push('/post/edit')
      break
    case 'draft':
      router.push('/draft')
      break
  }
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push(`/user/${userStore.getUserInfo?.id}`)
      break
    case 'posts':
      router.push('/post/user')
      break
    case 'columns':
      router.push('/user/columns')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const success = await userStore.logoutAction()
    if (success) {
      ElMessage.success('退出登录成功')
      localStorage.removeItem('user')
      router.push('/')
      window.location.reload()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录错误:', error)
      ElMessage.error('退出登录失败，请重试')
    }
  }
}

const fetchUnreadCount = async () => {
  try {
    const [notifRes, msgRes] = await Promise.all([
      getUnreadNotificationCount(),
      getMessageUnreadCount()
    ])

    if (notifRes.code === 20000) {
      notificationUnread.value = (notifRes.data as number) || 0
    }
    if (msgRes.code === 20000) {
      messageUnread.value = (msgRes.data as number) || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

const fetchNotificationUnreadCount = async () => {
  try {
    const res = await getUnreadNotificationCount()
    if (res.code === 20000) {
      notificationUnread.value = (res.data as number) || 0
    }
  } catch (error) {
    console.error('获取通知未读数失败:', error)
  }
}

const fetchMessageUnreadCount = async () => {
  try {
    const res = await getMessageUnreadCount()
    if (res.code === 20000) {
      messageUnread.value = (res.data as number) || 0
    }
  } catch (error) {
    console.error('获取私信未读数失败:', error)
  }
}

watch(
  () => userStore.isAuthenticated,
  (newValue) => {
    if (newValue) {
      fetchUnreadCount()
    } else {
      notificationUnread.value = 0
      messageUnread.value = 0
    }
  }
)

const isActive = (routePath: string) => {
  return router.currentRoute.value.path === routePath
}
</script>

<style scoped>
.app-header {
  height: var(--header-height);
  border-bottom: 1px solid #f0f0f0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
}

.header-container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.is-hidden {
  display: none; /* 隐藏导航栏 */
}

.logo-section {
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--el-color-primary), #409eff);
  color: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  transition: transform 0.3s ease;
}

.logo:hover .logo-icon {
  transform: scale(1.05);
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  background: linear-gradient(120deg, var(--el-color-primary), #409eff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-tabs {
  display: flex;
  align-items: center;
  height: var(--header-height);
  gap: 8px;
}

.nav-tab-item {
  height: var(--header-height);
  line-height: var(--header-height);
  padding: 0 20px;
  font-size: 15px;
  color: #515767;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  font-weight: 500;
}

.nav-tab-item:hover {
  color: #1e80ff;
}

.nav-tab-item.is-active {
  color: #1e80ff;
  font-weight: 600;
}

.nav-tab-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #1e80ff;
  border-radius: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-box {
  position: relative;
  width: 200px;
  height: 36px;
}

.search-box :deep(.el-input) {
  position: absolute;
  right: 0;
  width: 200px;
  transition: width 0.3s ease;
}

.search-box:focus-within :deep(.el-input) {
  width: 320px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: #f4f4f4;
  box-shadow: none;
  padding: 0 8px;
  transition: all 0.3s ease;
}

.search-box :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  background-color: #fff;
}

.search-box :deep(.el-input__inner) {
  height: 36px;
  font-size: 14px;
}

.user-section {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 8px;
}

.login-btn {
  padding: 8px 24px;
  font-size: 14px;
}

/* 应式调整 */
@media screen and (max-width: 768px) {
  .search-box {
    display: none;
  }

  .header-container {
    padding: 0 16px;
    gap: 20px;
  }

  .logo-text {
    display: none;
  }

  .main-nav :deep(.el-menu-item) {
    padding: 0 16px;
  }
}

.publish-dropdown {
  margin-right: 8px;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 16px;
  font-weight: normal;
}

.publish-icon {
  font-size: 14px;
}

.custom-dropdown {
  padding: 8px;
  min-width: 240px !important;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.user-dropdown {
  min-width: 180px !important;
}

:deep(.el-dropdown-menu__item) {
  padding: 8px 12px;
  line-height: 1.5;
  border-radius: 4px;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-content {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.el-dropdown-menu__item .el-icon {
  margin-right: 0;
}

.message-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #606266;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
}

.message-icon:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
}

.message-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
}

.nav-menu .el-menu-item.is-active {
  color: #409eff;
  background-color: transparent;
  border: none;
}

.nav-menu .el-menu-item {
  color: #606266;
}

.nav-menu .el-menu-item:hover {
  color: #409eff;
}

/* 搜索下拉框 */
.search-dropdown {
  max-height: 400px;
  overflow-y: auto;
}

.dropdown-section {
  margin-bottom: 16px;
}

.dropdown-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.keyword-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  padding: 4px 10px;
  background: #f4f4f5;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.keyword-tag:hover {
  background: #e6f0ff;
  color: #409eff;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  cursor: pointer;
  transition: all 0.2s;
}

.hot-item:hover {
  color: #409eff;
}

.hot-rank {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
  background: #f0f0f0;
  color: #909399;
}

.hot-rank.top {
  background: #ff6b6b;
  color: #fff;
}

.hot-text {
  font-size: 13px;
}

.empty-tips {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 16px 0;
}
</style>

<!-- 全局样式：popover需要非scoped -->
<style>
.search-popover {
  border-radius: 8px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
  padding: 12px !important;
}
</style>
