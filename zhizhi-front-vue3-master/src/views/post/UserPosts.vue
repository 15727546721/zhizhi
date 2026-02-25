<template>
  <div class="user-posts">
    <div class="page-header">
      <h2>{{ pageTitle }}</h2>
      <el-button type="primary" @click="goToCreatePost">
        <el-icon><Plus /></el-icon>
        发布新帖子
      </el-button>
    </div>

    <!-- 帖子筛选 -->
    <div class="posts-filter">
      <div class="filter-header">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="已发布" name="published"></el-tab-pane>
          <el-tab-pane name="draft">
            <template #label>
              <span>草稿</span>
              <el-badge v-if="draftCount > 0" :value="draftCount" class="draft-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="待审核" name="pending"></el-tab-pane>
        </el-tabs>
        
        <div class="filter-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索我的帖子"
            clearable
            style="width: 200px; margin-right: 16px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button @click="handleSearch">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="posts-list">
      <el-table 
        :data="userPosts" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="post-title-cell">
              <span 
                class="post-title" 
                @click="goToPostDetail(row.id)"
              >
                {{ row.title }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <span v-if="row.status === 'draft' && row.updateTime">
                {{ row.updateTime }}
                <span class="time-label">（更新）</span>
              </span>
              <span v-else>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="viewCount" label="浏览" width="80">
          <template #default="{ row }">
            {{ row.viewCount }}
          </template>
        </el-table-column>
        
        <el-table-column prop="likeCount" label="点赞" width="80">
          <template #default="{ row }">
            {{ row.likeCount }}
          </template>
        </el-table-column>
        
        <el-table-column prop="commentCount" label="评论" width="80">
          <template #default="{ row }">
            {{ row.commentCount }}
          </template>
        </el-table-column>
        
        <el-table-column prop="favoriteCount" label="收藏" width="80">
          <template #default="{ row }">
            {{ row.favoriteCount }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right" align="left">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                link 
                type="primary" 
                size="small"
                @click="editPost(row)"
                v-if="row.status !== 'published'"
              >
                编辑
              </el-button>
              <el-button 
                link 
                type="primary" 
                size="small"
                @click="viewPost(row)"
                v-else
              >
                查看
              </el-button>
              <el-button 
                link 
                type="danger" 
                size="small"
                @click="handleDeletePost(row)"
              >
                删除
              </el-button>
              <el-dropdown @command="handleDropdownCommand">
                <el-button link type="primary" size="small">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{action: 'preview', post: row}">
                      预览
                    </el-dropdown-item>
                    <el-dropdown-item 
                      v-if="row.status === 'draft'" 
                      :command="{action: 'publish', post: row}"
                    >
                      发布
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除这篇帖子吗？此操作不可撤销。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 帖子预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="帖子预览"
      width="80%"
      class="post-preview-dialog"
      :before-close="handlePreviewClose"
    >
      <post-preview 
        :post-data="previewPostData" 
        :mode="previewPostData.type === 'ARTICLE' ? 'markdown' : 'text'"
        @close="handlePreviewClose"
      />
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import { getMyPosts, getPostDraftList, deletePost, deleteDraft, publishDraft } from '@/api/post'
import { usePostStore } from '@/stores/module/post'
import PostPreview from './components/PostPreview.vue'
import type { PageResponse } from '@/types'
import { validateApiResponse } from '@/utils/typeGuards'

// 类型定义
interface PostItem {
  id: number
  title: string
  status: string
  createTime: string
  updateTime?: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  content?: string
  description?: string
  tags?: string[]
  type?: string
  author?: {
    name: string
    avatar: string
  }
}

interface PreviewPostData {
  id: number | null
  type: string
  title: string
  content: string
  description: string
  tags: string[]
  viewCount: number
  likeCount?: number
  commentCount?: number
  favoriteCount?: number
  createTime: string
  updateTime?: string
  author: {
    name: string
    avatar: string
  }
}

interface DropdownCommand {
  action: string
  post: PostItem
}

defineOptions({
  name: 'UserPosts'
})

const router = useRouter()
const route = useRoute()
const postStore = usePostStore()

// 使用 Store 中的状态配置
const { STATUS_CONFIG, getStatusTagType, getStatusLabel, transformStatus } = postStore

// 动态页面标题
const pageTitle = computed(() => {
  return route.meta?.defaultTab === 'draft' ? '草稿箱' : '我的帖子'
})

// 帖子数据
const userPosts = ref<PostItem[]>([])

// 使用 Store 中的草稿数量（响应式）
const draftCount = computed(() => postStore.draftCount)

// 分页相关数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 搜索相关数据
const searchQuery = ref('')
const activeTab = ref('all')

// 删除相关数据
const deleteDialogVisible = ref(false)
const postToDelete = ref<PostItem | null>(null)

// 预览相关数据
const previewDialogVisible = ref(false)
const previewPostData = ref<PreviewPostData>({
  id: null,
  type: 'post',
  title: '',
  content: '',
  description: '',
  tags: [],
  viewCount: 0,
  createTime: '',
  author: {
    name: '',
    avatar: ''
  }
})

// 数据转换函数
const transformPostData = (item: any): PostItem => {
  const post = item.post || item.postItem || item
  const statusCode = post.status
  
  // 使用 Store 的方法转换状态
  const statusKey = postStore.getStatusKeyByCode(statusCode)
  
  return {
    id: post.id,
    title: post.title || post.titleValue || '无标题',
    status: statusKey,
    createTime: post.createTime || '',
    updateTime: post.updateTime || '',
    viewCount: post.viewCount || 0,
    likeCount: post.likeCount || 0,
    commentCount: post.commentCount || 0,
    favoriteCount: post.favoriteCount || 0,
    content: post.content || '',
    description: post.description || '',
    tags: post.tagNameList || post.tags || []
  }
}

// 加载帖子列表
const loadPosts = async (): Promise<void> => {
  loading.value = true
  try {
    // 获取当前状态
    const status = activeTab.value === 'published' ? 'PUBLISHED' : 
                   activeTab.value === 'draft' ? 'DRAFT' :
                   activeTab.value === 'pending' ? 'PENDING' : null
    
    // 统一使用getMyPosts接口，支持搜索
    const response = await getMyPosts({
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      status: status,
      keyword: searchQuery.value.trim() || null
    })
    
    const pageData = validateApiResponse<PageResponse<PostItem[]>>(response)
    
    if (pageData) {
      // 使用转换函数处理数据
      userPosts.value = pageData.data.map(transformPostData)
      total.value = pageData.total
      
      // 如果是草稿标签页，更新 Store 中的草稿数量
      if (activeTab.value === 'draft') {
        postStore.draftCount = total.value
      }
    }
  } catch (error) {
    ElMessage.error('加载帖子列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载草稿数量（使用 Store 的缓存方法）
const loadDraftCount = async (): Promise<void> => {
  // 如果当前就是草稿标签页，不需要单独加载
  if (activeTab.value === 'draft') {
    return
  }
  
  await postStore.fetchDraftCount()
}

// 处理标签页切换
const handleTabChange = (tab: string): void => {
  currentPage.value = 1
  loadPosts()
}

// 处理搜索
const handleSearch = (): void => {
  currentPage.value = 1
  loadPosts()
}

// 处理分页变化
const handlePageChange = (page: number): void => {
  currentPage.value = page
  loadPosts()
}

// 处理页面大小变化
const handleSizeChange = (size: number): void => {
  pageSize.value = size
  currentPage.value = 1
  loadPosts()
}

// 跳转到创建帖子页面
const goToCreatePost = (): void => {
  router.push('/post/edit')
}

// 编辑帖子
const editPost = (post: PostItem): void => {
  router.push(`/post/edit/${post.id}`)
}

// 查看帖子
const viewPost = (post: PostItem): void => {
  router.push(`/post/${post.id}`)
}

// 预览帖子
const previewPost = (post: PostItem): void => {
  previewPostData.value = {
    id: post.id,
    type: 'post',
    title: post.title,
    content: post.content || '',
    description: post.description || '',
    tags: post.tags || [],
    viewCount: post.viewCount || 0,
    likeCount: post.likeCount || 0,
    commentCount: post.commentCount || 0,
    favoriteCount: post.favoriteCount || 0,
    createTime: post.createTime,
    updateTime: post.updateTime,
    author: post.author || { 
      name: '我', 
      avatar: '' 
    }
  }
  previewDialogVisible.value = true
}

// 删除帖子
const handleDeletePost = (post: PostItem): void => {
  postToDelete.value = post
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async (): Promise<void> => {
  if (!postToDelete.value) return
  
  try {
    const post = postToDelete.value
    if (post.status === 'draft') {
      // 删除草稿
      await deleteDraft(post.id)
      // 更新 Store 中的草稿数量
      postStore.updateDraftCount(-1)
    } else {
      // 删除已发布帖子
      await deletePost(post.id)
    }
    ElMessage.success('删除成功')
    
    // 重新加载数据
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败，请稍后重试')
  } finally {
    deleteDialogVisible.value = false
    postToDelete.value = null
  }
}

// 处理下拉菜单命令
const handleDropdownCommand = async (command: DropdownCommand): Promise<void> => {
  const { action, post } = command
  
  try {
    switch (action) {
      case 'preview':
        previewPost(post)
        break
        
      case 'publish':
        await publishDraft({
          id: post.id,
          title: post.title,
          content: post.content || '',
          description: post.description || ''
        })
        ElMessage.success('发布成功')
        
        // 更新 Store 中的草稿数量
        postStore.updateDraftCount(-1)
        
        // 重新加载数据
        await loadPosts()
        break
        
      default:
        // 未知操作
    }
  } catch (error: any) {
    ElMessage.error(error.message || `${action}操作失败，请稍后重试`)
  }
}

// 跳转到帖子详情
const goToPostDetail = (postId: number): void => {
  router.push(`/post/${postId}`)
}

// 处理预览对话框关闭
const handlePreviewClose = (): void => {
  previewDialogVisible.value = false
}

onMounted(() => {
  // 从路由 meta 读取默认 Tab（草稿箱页面）
  const defaultTab = route.meta?.defaultTab as string | undefined
  if (defaultTab) {
    activeTab.value = defaultTab
  }
  loadPosts()
  loadDraftCount()
})

</script>

<style scoped>
.user-posts {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - var(--header-height));
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.posts-filter {
  background: #fff;
  border-radius: 8px;
  padding: 0 20px 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.posts-filter :deep(.el-tabs__header) {
  margin: 0;
}

.posts-filter :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.posts-filter :deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 16px;
  height: 52px;
  line-height: 52px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  white-space: nowrap;
}

/* 帖子标题单元格 */
.post-title-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-type-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.article-badge {
  background: #ffeaa7;
  color: #d35400;
}

.discussion-badge {
  background: #d6eaf8;
  color: #2980b9;
}

.question-badge {
  background: #e8f8f5;
  color: #16a085;
}

.post-badge {
  background: #f8f9fa;
  color: #606266;
}

.post-title {
  cursor: pointer;
  color: #303133;
  font-weight: 500;
  transition: color 0.3s;
}

.post-title:hover {
  color: #409eff;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 帖子预览对话框样式 */
.post-preview-dialog {
  max-height: 80vh;
}

.post-preview-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.post-preview-content {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.post-preview-content .post-type-badge {
  position: absolute;
  top: 20px;
  right: 20px;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: #303133;
}

.publish-info {
  font-size: 13px;
  color: #909399;
}

.publish-info .dot {
  margin: 0 6px;
}

.post-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.6;
}

.post-content {
  margin-bottom: 20px;
}

.markdown-content {
  font-size: 16px;
  line-height: 1.7;
  color: #303133;
}

.plain-content {
  font-size: 16px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.tag-item {
  cursor: pointer;
}

/* 草稿徽章样式 */
.draft-badge {
  margin-left: 6px;
}

.draft-badge :deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

/* 时间单元格样式 */
.time-cell {
  font-size: 13px;
  color: #606266;
}

.time-cell .time-label {
  font-size: 12px;
  color: #909399;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  margin: 0;
  padding: 0;
  font-size: 14px;
  height: auto;
}

.action-buttons .el-button + .el-button {
  margin-left: 0;
}

.action-buttons .el-dropdown {
  margin: 0;
}

.action-buttons .el-dropdown .el-button {
  margin: 0;
  padding: 0;
}

/* 操作列单元格样式 */
.posts-list :deep(.el-table .el-table__cell) {
  padding: 12px 8px;
}

.posts-list :deep(.el-table__fixed-right) {
  right: 0 !important;
}
</style>