import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/module/user'
import { ElMessage } from 'element-plus'

// 扩展 Window 接口
declare global {
  // eslint-disable-next-line no-var
  var globalAbortController: AbortController | null
}

/**
 * 路由元信息类型
 */
export interface RouteMeta {
  title?: string
  keepAlive?: boolean
  requireAuth?: boolean
  defaultTab?: string
}

/**
 * 路由名称常量
 */
export const RouteNames = {
  HOME: 'Home',
  RANKING: 'Ranking',
  ANNOUNCEMENTS: 'Announcements',
  POST_CREATE: 'PostCreate',
  POST_EDIT: 'PostEdit',
  POST_DETAIL: 'PostDetail',
  USER_POSTS: 'UserPosts',
  DRAFTS: 'Drafts',
  PROFILE: 'Profile',
  EDIT_PROFILE: 'EditProfile',
  CHANGE_PASSWORD: 'ChangePassword',
  SETTINGS: 'Settings',
  NOTIFICATION_CENTER: 'NotificationCenter',
  PRIVATE_MESSAGE_CENTER: 'PrivateMessageCenter',
  SEARCH: 'Search',
  SEARCH_POSTS: 'SearchPosts',
  MY_FEEDBACK: 'MyFeedback',
  TAG_SQUARE: 'TagSquare',
  TAG_DETAIL: 'TagDetail',
  MY_FAVORITES: 'MyFavorites'
} as const

const routes: RouteRecordRaw[] = [
  {
    path: '/announcements',
    name: RouteNames.ANNOUNCEMENTS,
    component: () => import('@/views/announcement/AnnouncementPage.vue'),
    meta: { title: '平台公告' }
  },
  {
    path: '/',
    name: RouteNames.HOME,
    component: () => import('@/views/home/HomePage.vue'),
    meta: { keepAlive: true }
  },
  {
    path: '/ranking',
    name: RouteNames.RANKING,
    component: () => import('@/views/home/RankingPage.vue'),
    meta: { title: '热门榜单' }
  },
  // 帖子 - 静态路由必须在动态路由之前
  {
    path: '/post/edit',
    name: RouteNames.POST_CREATE,
    component: () => import('@/views/post/PostEdit.vue'),
    meta: { title: '发布帖子', requireAuth: true }
  },
  {
    path: '/post/edit/:id',
    name: RouteNames.POST_EDIT,
    component: () => import('@/views/post/PostEdit.vue'),
    meta: { title: '编辑帖子', requireAuth: true }
  },
  {
    path: '/post/user',
    name: RouteNames.USER_POSTS,
    component: () => import('@/views/post/UserPosts.vue'),
    meta: { title: '我的帖子', requireAuth: true }
  },
  {
    path: '/draft',
    name: RouteNames.DRAFTS,
    component: () => import('@/views/post/UserPosts.vue'),
    meta: { title: '草稿箱', requireAuth: true, defaultTab: 'draft' }
  },
  {
    path: '/post/:id',
    name: RouteNames.POST_DETAIL,
    component: () => import('@/views/post/PostDetail.vue'),
    meta: { title: '帖子详情' }
  },
  {
    path: '/user/edit',
    name: RouteNames.EDIT_PROFILE,
    component: () => import('@/views/user/EditProfile.vue'),
    meta: { title: '编辑个人信息', requireAuth: true }
  },
  {
    path: '/user/change-password',
    name: RouteNames.CHANGE_PASSWORD,
    component: () => import('@/views/user/ChangePassword.vue'),
    meta: { title: '修改密码', requireAuth: true }
  },
  {
    path: '/settings',
    name: RouteNames.SETTINGS,
    component: () => import('@/views/user/Settings.vue'),
    meta: { title: '账户设置', requireAuth: true }
  },
  {
    path: '/user/settings',
    redirect: '/settings'
  },
  {
    path: '/user/:userId',
    name: RouteNames.PROFILE,
    component: () => import('@/views/user/Profile.vue'),
    meta: { title: '个人主页', requireAuth: true }
  },
  {
    path: '/message',
    name: RouteNames.NOTIFICATION_CENTER,
    component: () => import('@/views/message/NotificationCenter.vue'),
    meta: { title: '消息中心', requireAuth: true }
  },
  {
    path: '/private-messages',
    name: RouteNames.PRIVATE_MESSAGE_CENTER,
    component: () => import('@/views/message/PrivateMessageCenter.vue'),
    meta: { title: '私信', requireAuth: true }
  },
  {
    path: '/search',
    name: RouteNames.SEARCH,
    component: () => import('@/views/search/SearchPage.vue'),
    meta: { title: '聚合搜索' }
  },
  {
    path: '/search/posts',
    name: RouteNames.SEARCH_POSTS,
    component: () => import('@/views/post/PostSearch.vue'),
    meta: { title: '搜索帖子' }
  },
  {
    path: '/feedback',
    name: RouteNames.MY_FEEDBACK,
    component: () => import('@/views/feedback/MyFeedback.vue'),
    meta: { title: '我的反馈', requireAuth: true }
  },
  // 标签/话题
  {
    path: '/tags',
    name: RouteNames.TAG_SQUARE,
    component: () => import('@/views/tag/TagSquare.vue'),
    meta: { title: '标签广场' }
  },
  {
    path: '/tag/:tagId',
    name: RouteNames.TAG_DETAIL,
    component: () => import('@/views/tag/TagDetail.vue'),
    meta: { title: '话题详情' }
  },
  // 收藏夹
  {
    path: '/favorites',
    name: RouteNames.MY_FAVORITES,
    component: () => import('@/views/user/Favorites.vue'),
    meta: { title: '我的收藏', requireAuth: true }
  },
  // 查看他人公开收藏夹
  {
    path: '/user/:userId/favorites/:folderId',
    name: 'UserFavoriteFolder',
    component: () => import('@/views/user/UserFavoriteFolder.vue'),
    meta: { title: '收藏夹详情' }
  },
  // 专栏路由
  {
    path: '/columns',
    name: 'ColumnSquare',
    component: () => import('@/views/column/ColumnSquare.vue'),
    meta: { title: '专栏广场' }
  },
  {
    path: '/columns/:id',
    name: 'ColumnDetail',
    component: () => import('@/views/column/ColumnDetail.vue'),
    meta: { title: '专栏详情' }
  },
  {
    path: '/columns/:id/manage',
    name: 'ColumnManage',
    component: () => import('@/views/column/ColumnManage.vue'),
    meta: { title: '管理专栏', requireAuth: true }
  },
  {
    path: '/user/columns',
    name: 'MyColumns',
    component: () => import('@/views/column/MyColumns.vue'),
    meta: { title: '我的专栏', requireAuth: true }
  },
  {
    path: '/user/subscriptions',
    name: 'UserSubscriptions',
    component: () => import('@/views/column/UserSubscriptions.vue'),
    meta: { title: '我的订阅', requireAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 初始化全局 AbortController
if (typeof window !== 'undefined') {
  ;(window as any).globalAbortController = new AbortController()
}

// 全局导航守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 只有在真正有页面切换时才清理请求
  if (from.name && to.name !== from.name) {
    if ((window as any).globalAbortController) {
      ;(window as any).globalAbortController.abort()
      ;(window as any).globalAbortController = null
    }
    ;(window as any).globalAbortController = new AbortController()
  }

  // 检查页面是否需要登录权限
  if (to.matched.some((record) => record.meta.requireAuth)) {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next('/')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
