<template>
  <div class="post-search">
    <div class="search-header">
      <div class="header-container">
        <!-- 居中搜索框区域 -->
        <div class="search-center">
          <div class="search-title">
            <h1>搜索帖子</h1>
            <p v-if="searchQuery && searchPerformed" class="search-keyword">
              关键词: <span>{{ searchQuery }}</span>
            </p>
          </div>
          <div class="search-box-wrapper">
            <div class="search-input-wrapper">
              <el-input
                v-model="searchQuery"
                placeholder="搜索帖子、话题或用户"
                size="large"
                class="search-input"
                @keyup.enter="handleSearch"
                @focus="handleSearchFocus"
                @blur="handleSearchBlur"
                clearable
                maxlength="100"
                show-word-limit
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
              </el-input>
              
              <!-- 搜索建议下拉框 -->
              <div v-if="showSuggestions && searchSuggestions.length > 0" class="suggestions-dropdown">
                <div 
                  v-for="(suggestion, index) in searchSuggestions" 
                  :key="index"
                  class="suggestion-item"
                  @mousedown.prevent="selectSuggestion(suggestion)"
                >
                  <el-icon class="suggestion-icon"><Search /></el-icon>
                  <span>{{ suggestion }}</span>
                </div>
              </div>
            </div>
            
            <el-button 
              type="primary" 
              size="large" 
              class="search-btn"
              @click="handleSearch"
              :icon="Search"
            >
              搜索
            </el-button>
          </div>
          
          <!-- 热门搜索词 -->
          <div v-if="hotKeywords.length > 0 && !searchPerformed" class="hot-keywords">
            <span class="hot-keywords-label">热门搜索：</span>
            <span
              v-for="(keyword, index) in hotKeywords"
              :key="index"
              class="hot-keyword-tag"
              @click="searchQuery = keyword; handleSearch()"
            >
              {{ keyword }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="content-layout">
      <!-- 左侧筛选区域 -->
      <div class="filters-sidebar">
        <div class="filter-group">
          <h3 class="filter-title">帖子类型</h3>
          <el-checkbox-group v-model="selectedTypes">
            <el-checkbox label="ARTICLE">文章</el-checkbox>
            <el-checkbox label="POST">普通帖子</el-checkbox>
            <el-checkbox label="DISCUSSION">讨论</el-checkbox>
            <el-checkbox label="QUESTION">问答</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="filter-group">
          <h3 class="filter-title">发布时间</h3>
          <el-radio-group v-model="timeRange">
            <el-radio value="all">全部时间</el-radio>
            <el-radio value="day">24小时内</el-radio>
            <el-radio value="week">一周内</el-radio>
            <el-radio value="month">一个月内</el-radio>
            <el-radio value="year">一年内</el-radio>
          </el-radio-group>
        </div>

        <div class="filter-group">
          <h3 class="filter-title">排序方式</h3>
          <el-radio-group v-model="sortOption">
            <el-radio value="time">最新发布</el-radio>
            <el-radio value="hot">最热</el-radio>
            <el-radio value="comment">评论最多</el-radio>
            <el-radio value="like">点赞最多</el-radio>
          </el-radio-group>
        </div>

        <el-button type="primary" @click="applyFilters" class="filter-apply-btn">应用筛选</el-button>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <div class="search-info">
          <h2>搜索结果</h2>
          <div class="result-stats">
            找到 {{ total }} 条相关结果
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 搜索结果列表 -->
        <div v-else class="search-results">
          <div 
            v-for="item in searchResults" 
            :key="item.id"
            class="result-item"
            @click="goToPostDetail(item.id)"
          >
            <div class="result-content">
              <h3 class="result-title" v-html="highlightKeyword(item.title, searchQuery)"></h3>
              <p class="result-excerpt" v-html="highlightKeyword(item.summary || item.description || item.content || '', searchQuery)"></p>
              <div class="result-meta">
                <div class="author-info">
                  <el-avatar :size="24" :src="item.avatar">
                    {{ (item.authorName || 'U').charAt(0).toUpperCase() }}
                  </el-avatar>
                  <span class="author-name">{{ item.authorName || '匿名用户' }}</span>
                </div>
                <div class="post-stats">
                  <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ item.commentCount || 0 }}</span>
                  <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
                </div>
                <div class="post-time">{{ formatTime(item.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="!loading && total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
            :disabled="loading"
          />
        </div>

        <!-- 无结果提示 -->
        <div class="no-results" v-if="!loading && searchResults.length === 0 && searchPerformed">
          <el-empty description="没有找到相关结果">
            <el-button type="primary" @click="resetSearch">重新搜索</el-button>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Search, 
  View, 
  ChatDotRound, 
  Star,
  // Collection
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { searchPosts, getSearchSuggestions, getHotKeywords } from '@/api/post'
import type { PageResponse, PostItem } from '@/types'
import { validateApiResponse } from '@/utils/typeGuards'

// 类型定义
interface SearchResult {
  id: number
  title: string
  summary?: string
  description?: string
  content?: string
  avatar?: string
  authorName?: string
  viewCount?: number
  commentCount?: number
  likeCount?: number
  createTime?: string
}

// 防抖函数
const debounce = <T extends (...args: any[]) => any>(fn: T, delay: number) => {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function(this: any, ...args: Parameters<T>) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

defineOptions({
  name: 'PostSearch'
})

const route = useRoute()
const router = useRouter()

// 搜索相关数据
const searchQuery = ref((route.query.keyword || route.query.q || '') as string)
const searchPerformed = ref(false)

// 筛选条件
const selectedTypes = ref<string[]>([])
const timeRange = ref('all')
const sortOption = ref('time')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索结果
const searchResults = ref<SearchResult[]>([])
const loading = ref(false)

// 搜索建议相关
const searchSuggestions = ref<string[]>([])
const hotKeywords = ref<string[]>([])
const showSuggestions = ref(false)

// 执行搜索
const handleSearch = async (): Promise<void> => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searchPerformed.value = true
  currentPage.value = 1
  loading.value = true
  
  try {
    // 构建搜索参数，包含筛选条件
    const searchParams: Record<string, any> = {
      keyword: searchQuery.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加类型筛选
    if (selectedTypes.value && selectedTypes.value.length > 0) {
      // 将前端的小写类型转换为后端需要的大写格式
      searchParams.types = selectedTypes.value.map(type => type.toUpperCase())
    }
    
    // 添加时间范围筛选
    if (timeRange.value && timeRange.value !== 'all') {
      searchParams.timeRange = timeRange.value
    }
    
    // 添加排序方式
    if (sortOption.value && sortOption.value !== 'time') {
      searchParams.sortOption = sortOption.value
    }
    
    const response = await searchPosts(searchParams)

    // 使用类型守卫验证分页响应
    const pageData = validateApiResponse<PageResponse<PostItem[]>>(response)
    
    if (pageData) {
      // 映射 PostItem 到 SearchResult
      searchResults.value = pageData.data.map(post => ({
        id: post.id,
        title: post.title,
        description: post.description,
        avatar: post.userAvatar,
        authorName: post.userName,
        viewCount: post.viewCount,
        commentCount: post.commentCount,
        likeCount: post.likeCount,
        createTime: post.createTime
      }))
      
      total.value = pageData.total
      currentPage.value = pageData.pageNo
      pageSize.value = pageData.pageSize
    } else {
      searchResults.value = []
      total.value = 0
    }
    
    if (total.value > 0) {
      ElMessage.success(`找到 ${total.value} 条相关结果`)
    }
  } catch (error) {
    searchResults.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 应用筛选
const applyFilters = (): void => {
  // 如果已执行过搜索，重新搜索以应用筛选条件
  if (searchQuery.value.trim()) {
    handleSearch()
  } else {
    ElMessage.warning('请先输入搜索关键词')
  }
}

// 重置搜索
const resetSearch = (): void => {
  searchQuery.value = ''
  selectedTypes.value = []
  timeRange.value = 'all'
  sortOption.value = 'time'
  searchResults.value = []
  total.value = 0
  searchPerformed.value = false
}

// 处理分页变化
const handlePageChange = async (page: number): Promise<void> => {
  currentPage.value = page
  if (searchQuery.value.trim()) {
    loading.value = true
    try {
      // 构建搜索参数，包含筛选条件
      const searchParams: Record<string, any> = {
        keyword: searchQuery.value,
        pageNo: page,
        pageSize: pageSize.value
      }
      
      // 添加类型筛选
      if (selectedTypes.value && selectedTypes.value.length > 0) {
        searchParams.types = selectedTypes.value.map(type => type.toUpperCase())
      }
      
      // 添加时间范围筛选
      if (timeRange.value && timeRange.value !== 'all') {
        searchParams.timeRange = timeRange.value
      }
      
      // 添加排序方式
      if (sortOption.value && sortOption.value !== 'time') {
        searchParams.sortOption = sortOption.value
      }
      
      const response = await searchPosts(searchParams)

      // 使用类型守卫验证分页响应
      const pageData = validateApiResponse<PageResponse<PostItem[]>>(response)
      
      if (pageData) {
        // 映射 PostItem 到 SearchResult
        searchResults.value = pageData.data.map(post => ({
          id: post.id,
          title: post.title,
          description: post.description,
          avatar: post.userAvatar,
          authorName: post.userName,
          viewCount: post.viewCount,
          commentCount: post.commentCount,
          likeCount: post.likeCount,
          createTime: post.createTime
        }))
        
        total.value = pageData.total
        currentPage.value = pageData.pageNo
        pageSize.value = pageData.pageSize
      } else {
        searchResults.value = []
        total.value = 0
      }
      
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (error) {
      // 加载失败
    } finally {
      loading.value = false
    }
  }
}

// 跳转到帖子详情
const goToPostDetail = (postId: number): void => {
  router.push(`/post/${postId}`)
}

// 格式化时间
const formatTime = (time: string | undefined): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

// HTML转义函数（防止XSS攻击）
const escapeHtml = (text: string): string => {
  if (!text) return ''
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return String(text).replace(/[&<>"']/g, (m) => map[m])
}

// 关键词高亮（安全的HTML渲染）
const highlightKeyword = (text: string, keyword: string): string => {
  if (!text || !keyword || !keyword.trim()) {
    return escapeHtml(text || '')
  }
  
  // 先对文本进行HTML转义，防止XSS攻击
  const escapedText = escapeHtml(text)
  const keywordTrimmed = keyword.trim()
  
  // 对关键词也进行转义
  const escapedKeyword = escapeHtml(keywordTrimmed)
  
  // 使用正则表达式进行大小写不敏感的匹配
  const regex = new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return escapedText.replace(regex, '<mark class="keyword-highlight">$1</mark>')
}

// 获取搜索建议
const fetchSearchSuggestions = async (keyword: string): Promise<void> => {
  if (!keyword || !keyword.trim()) {
    searchSuggestions.value = []
    showSuggestions.value = false
    return
  }
  
  try {
    const response = await getSearchSuggestions({ keyword: keyword.trim(), limit: 10 })
    const suggestions = validateApiResponse<string[]>(response)
    
    if (suggestions) {
      searchSuggestions.value = suggestions
      showSuggestions.value = suggestions.length > 0
    } else {
      searchSuggestions.value = []
      showSuggestions.value = false
    }
  } catch (error) {
    searchSuggestions.value = []
    showSuggestions.value = false
  }
}

// 搜索输入防抖处理（300ms延迟，用于搜索建议）
const debouncedFetchSuggestions = debounce((keyword: string) => {
  fetchSearchSuggestions(keyword)
}, 300)

// 监听搜索关键词变化，实时获取搜索建议
watch(searchQuery, (newVal) => {
  if (newVal && newVal.trim()) {
    debouncedFetchSuggestions(newVal)
  } else {
    searchSuggestions.value = []
    showSuggestions.value = false
  }
})

// 加载热门搜索词
const loadHotKeywords = async (): Promise<void> => {
  try {
    const response = await getHotKeywords({ limit: 10 })
    const keywords = validateApiResponse<string[]>(response)
    
    if (keywords) {
      hotKeywords.value = keywords
    }
  } catch (error) {
    // 获取失败
  }
}

// 选择搜索建议
const selectSuggestion = (suggestion: string): void => {
  searchQuery.value = suggestion
  showSuggestions.value = false
  handleSearch()
}

// 处理搜索输入框获得焦点
const handleSearchFocus = (): void => {
  if (searchQuery.value && searchQuery.value.trim()) {
    showSuggestions.value = true
  }
}

// 处理搜索输入框失去焦点
const handleSearchBlur = (): void => {
  // 延迟隐藏建议，以便用户能够点击建议项
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// 初始化搜索
const initSearch = (): void => {
  // 从路由参数获取搜索关键词
  if (route.query.keyword) {
    searchQuery.value = route.query.keyword as string
    handleSearch()
  } else if (route.query.q) {
    searchQuery.value = route.query.q as string
    handleSearch()
  }
}

// 监听路由参数变化
watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword && newKeyword !== searchQuery.value) {
      searchQuery.value = newKeyword as string
      handleSearch()
    }
  }
)

onMounted(() => {
  initSearch()
  loadHotKeywords()
})
</script>

<style scoped>
.post-search {
  min-height: calc(100vh - var(--header-height));
  background-color: #f4f5f5;
}

/* 顶部搜索栏样式 */
.search-header {
  background: transparent;
  padding: 0;
  position: relative;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

/* 居中搜索区域 */
.search-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 700px;
}

.search-title {
  color: #1d2129;
  text-align: center;
  width: 100%;
}

.search-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  letter-spacing: -0.5px;
}

.search-keyword {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #515767;
  font-weight: 400;
}

.search-keyword span {
  font-weight: 500;
  background: #e8f3ff;
  color: #1e80ff;
  padding: 3px 10px;
  border-radius: 6px;
  margin-left: 6px;
}

.search-box-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  max-width: 600px;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 28px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e6eb;
  transition: all 0.3s ease;
  padding: 0 16px;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #1e80ff;
  box-shadow: 0 4px 12px rgba(30, 128, 255, 0.15);
  transform: translateY(-1px);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #1e80ff;
  box-shadow: 0 4px 16px rgba(30, 128, 255, 0.2);
}

.search-input :deep(.el-input__prefix) {
  margin-left: 4px;
}

.search-icon {
  color: #1e80ff;
  font-size: 18px;
}

.search-btn {
  border-radius: 24px;
  padding: 0 24px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  white-space: nowrap;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.content-layout {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  display: flex;
  gap: 20px;
}

/* 左侧筛选区域 */
.filters-sidebar {
  width: 250px;
  background: transparent;
  border-radius: 8px;
  padding: 20px;
  align-self: flex-start;
  position: sticky;
  top: calc(var(--header-height) + 20px);
}

.filter-group {
  margin-bottom: 24px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 12px;
}

.filter-group :deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-group :deep(.el-checkbox) {
  margin-right: 0;
}

.filter-group :deep(.el-checkbox__label) {
  color: #515767;
  font-size: 14px;
}

.filter-group :deep(.el-checkbox:hover .el-checkbox__label) {
  color: #1d2129;
}

.filter-group :deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-group :deep(.el-radio) {
  margin-right: 0;
}

.filter-group :deep(.el-radio__label) {
  color: #515767;
  font-size: 14px;
}

.filter-group :deep(.el-radio:hover .el-radio__label) {
  color: #1d2129;
}

.filter-apply-btn {
  width: 100%;
  margin-top: 8px;
  border-radius: 6px;
  font-weight: 500;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  min-width: 0;
}

.search-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.search-info h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.result-stats {
  font-size: 14px;
  color: #909399;
}

/* 搜索结果列表 */
.search-results {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.result-item {
  padding: 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.result-item:hover {
  background-color: #f5f7fa;
}

.result-item:last-child {
  border-bottom: none;
}

.post-type-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
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
  background: #e8f6f3;
  color: #27ae60;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.result-excerpt {
  font-size: 15px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  color: #303133;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-time {
  margin-left: auto;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 加载状态 */
.loading-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}

/* 无结果提示 */
.no-results {
  background: transparent;
  padding: 60px 20px;
  text-align: center;
  margin-top: 20px;
}

/* 搜索输入框包装器 */
.search-input-wrapper {
  position: relative;
  flex: 1;
}

/* 搜索建议下拉框 */
.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 4px;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: #f5f7fa;
}

.suggestion-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 14px;
}

/* 热门搜索词 */
.hot-keywords {
  margin-top: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-keywords-label {
  color: #909399;
  font-size: 14px;
}

.hot-keyword-tag {
  display: inline-block;
  padding: 4px 12px;
  background-color: #f0f2f5;
  border-radius: 12px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.hot-keyword-tag:hover {
  background-color: #409eff;
  color: #fff;
}

/* 关键词高亮样式 */
.keyword-highlight {
  background-color: #fff3cd;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 600;
}

.result-title :deep(.keyword-highlight) {
  background-color: #fff3cd;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 600;
}

.result-excerpt :deep(.keyword-highlight) {
  background-color: #fff3cd;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
  font-weight: 500;
}

/* 响应式设计 */
@media screen and (max-width: 992px) {
  .content-layout {
    flex-direction: column;
  }
  
  .filters-sidebar {
    width: 100%;
    position: static;
  }
  
  .header-container {
    padding: 24px 20px 32px;
  }
  
  .search-center {
    gap: 20px;
  }
  
  .search-title h1 {
    font-size: 24px;
  }
  
  .search-box-wrapper {
    flex-direction: column;
    width: 100%;
    max-width: 100%;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .result-meta {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .post-time {
    margin-left: 0;
  }
}

@media screen and (max-width: 768px) {
  .search-header {
    padding: 0;
  }
  
  .header-container {
    padding: 20px 16px 28px;
  }
  
  .search-center {
    gap: 16px;
  }
  
  .search-title h1 {
    font-size: 22px;
  }
  
  .search-keyword {
    font-size: 13px;
  }
  
  .content-layout {
    padding: 0 16px;
    gap: 16px;
  }
  
  .result-item {
    padding: 16px;
  }
  
  .post-type-badge {
    top: 16px;
    right: 16px;
  }
  
  .result-title {
    font-size: 16px;
  }
  
  .result-excerpt {
    font-size: 14px;
  }
  
  .result-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .back-btn {
    font-size: 13px;
    padding: 6px 10px;
  }
}
</style>