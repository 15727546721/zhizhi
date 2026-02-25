<template>
  <div class="search-page">
    <!-- 搜索头部 -->
    <div class="search-header">
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索帖子、用户、标签..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 搜索类型切换 -->
      <div class="search-tabs" v-if="hasSearched">
        <el-radio-group v-model="activeTab" @change="handleTabChange">
          <el-radio-button value="all">
            全部
            <span class="count" v-if="totalCount > 0">({{ totalCount }})</span>
          </el-radio-button>
          <el-radio-button value="posts">
            帖子
            <span class="count" v-if="searchResult?.posts?.total">({{ searchResult.posts.total }})</span>
          </el-radio-button>
          <el-radio-button value="users">
            用户
            <span class="count" v-if="searchResult?.users?.total">({{ searchResult.users.total }})</span>
          </el-radio-button>
          <el-radio-button value="tags">
            标签
            <span class="count" v-if="searchResult?.tags?.total">({{ searchResult.tags.total }})</span>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-content" v-loading="loading">
      <!-- 未搜索状态：显示搜索历史和热词 -->
      <div v-if="!hasSearched" class="search-suggestions">
        <!-- 搜索历史 -->
        <div class="suggestion-section" v-if="searchHistory.length > 0">
          <div class="section-title">
            <span>搜索历史</span>
            <el-button text size="small" @click="handleClearHistory">清空</el-button>
          </div>
          <div class="suggestion-tags">
            <el-tag
              v-for="item in searchHistory"
              :key="item"
              class="suggestion-tag"
              closable
              @click="handleQuickSearch(item)"
              @close="handleDeleteHistory(item)"
            >
              {{ item }}
            </el-tag>
          </div>
        </div>
        
        <!-- 热门搜索 -->
        <div class="suggestion-section" v-if="hotWords.length > 0">
          <div class="section-title">
            <span>🔥 热门搜索</span>
          </div>
          <div class="suggestion-tags">
            <el-tag
              v-for="(item, index) in hotWords"
              :key="item"
              class="suggestion-tag hot"
              :type="index < 3 ? 'danger' : 'info'"
              @click="handleQuickSearch(item)"
            >
              <span class="hot-rank" v-if="index < 3">{{ index + 1 }}</span>
              {{ item }}
            </el-tag>
          </div>
        </div>
        
        <!-- 无历史无热词 -->
        <div v-if="searchHistory.length === 0 && hotWords.length === 0" class="empty-state">
          <el-icon class="search-icon"><Search /></el-icon>
          <p>输入关键词开始搜索</p>
        </div>
      </div>

      <!-- 无结果 -->
      <div v-else-if="!loading && totalCount === 0" class="empty-state">
        <el-icon class="empty-icon"><DocumentDelete /></el-icon>
        <p>未找到与 "{{ keyword }}" 相关的内容</p>
        <p class="tips">试试其他关键词吧</p>
      </div>

      <!-- 搜索结果展示 -->
      <template v-else-if="searchResult">
        <!-- 全部结果 -->
        <template v-if="activeTab === 'all'">
          <!-- 帖子结果 -->
          <div class="result-section" v-if="searchResult.posts?.list?.length > 0">
            <div class="section-header">
              <h3>帖子</h3>
              <el-button text type="primary" @click="activeTab = 'posts'" v-if="searchResult.posts.hasMore">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="post-list">
              <PostCard 
                v-for="post in searchResult.posts.list" 
                :key="post.id" 
                :post="post"
                @click="goToPost(post.id)"
              />
            </div>
          </div>

          <!-- 用户结果 -->
          <div class="result-section" v-if="searchResult.users?.list?.length > 0">
            <div class="section-header">
              <h3>用户</h3>
              <el-button text type="primary" @click="activeTab = 'users'" v-if="searchResult.users.hasMore">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="user-list">
              <UserCard 
                v-for="user in searchResult.users.list" 
                :key="user.id" 
                :user="user"
                @click="goToUser(user.id)"
              />
            </div>
          </div>

          <!-- 标签结果 -->
          <div class="result-section" v-if="searchResult.tags?.list?.length > 0">
            <div class="section-header">
              <h3>标签</h3>
              <el-button text type="primary" @click="activeTab = 'tags'" v-if="searchResult.tags.hasMore">
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="tag-list">
              <TagCard 
                v-for="tag in searchResult.tags.list" 
                :key="tag.id" 
                :tag="tag"
                @click="goToTag(tag.id)"
              />
            </div>
          </div>
        </template>

        <!-- 仅帖子 -->
        <template v-else-if="activeTab === 'posts'">
          <div class="post-list full">
            <PostCard 
              v-for="post in searchResult.posts?.list" 
              :key="post.id" 
              :post="post"
              @click="goToPost(post.id)"
            />
          </div>
          <div v-if="searchResult.posts?.list?.length === 0" class="empty-section">
            暂无相关帖子
          </div>
        </template>

        <!-- 仅用户 -->
        <template v-else-if="activeTab === 'users'">
          <div class="user-list full">
            <UserCard 
              v-for="user in searchResult.users?.list" 
              :key="user.id" 
              :user="user"
              @click="goToUser(user.id)"
            />
          </div>
          <div v-if="searchResult.users?.list?.length === 0" class="empty-section">
            暂无相关用户
          </div>
        </template>

        <!-- 仅标签 -->
        <template v-else-if="activeTab === 'tags'">
          <div class="tag-list full">
            <TagCard 
              v-for="tag in searchResult.tags?.list" 
              :key="tag.id" 
              :tag="tag"
              @click="goToTag(tag.id)"
            />
          </div>
          <div v-if="searchResult.tags?.list?.length === 0" class="empty-section">
            暂无相关标签
          </div>
        </template>
      </template>
    </div>

    <!-- 搜索耗时 -->
    <div class="search-meta" v-if="hasSearched && searchResult?.costTime">
      搜索用时 {{ searchResult.costTime }} 毫秒
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ArrowRight, DocumentDelete } from '@element-plus/icons-vue'
import { aggregateSearch, getSearchHistory, getHotWords, deleteSearchHistory, clearSearchHistory } from '@/api/search'
import { ElMessage } from 'element-plus'
import PostCard from './components/PostCard.vue'
import UserCard from './components/UserCard.vue'
import TagCard from './components/TagCard.vue'

const route = useRoute()
const router = useRouter()

// 状态
const keyword = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const activeTab = ref('all')
const searchResult = ref(null)
const searchHistory = ref([])
const hotWords = ref([])

// 计算属性
const totalCount = computed(() => {
  if (!searchResult.value) return 0
  return (
    (searchResult.value.posts?.total || 0) +
    (searchResult.value.users?.total || 0) +
    (searchResult.value.tags?.total || 0)
  )
})

// 搜索处理
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true

  try {
    // 根据当前tab决定请求参数
    let params = { keyword: keyword.value.trim() }
    
    if (activeTab.value === 'all') {
      params.postLimit = 5
      params.userLimit = 5
      params.tagLimit = 10
    } else if (activeTab.value === 'posts') {
      params.postLimit = 20
      params.userLimit = 0
      params.tagLimit = 0
    } else if (activeTab.value === 'users') {
      params.postLimit = 0
      params.userLimit = 20
      params.tagLimit = 0
    } else if (activeTab.value === 'tags') {
      params.postLimit = 0
      params.userLimit = 0
      params.tagLimit = 50
    }

    const res = await aggregateSearch(params)
    if (res.code === 20000 || res.code === 200) {
      searchResult.value = res.data
      // 更新URL
      router.replace({ query: { q: keyword.value, tab: activeTab.value } })
    } else {
      ElMessage.error(res.info || '搜索失败')
    }
  } catch (error) {
    // 搜索失败
  } finally {
    loading.value = false
  }
}

// 清空搜索
const handleClear = () => {
  searchResult.value = null
  hasSearched.value = false
  router.replace({ query: {} })
}

// Tab切换
const handleTabChange = () => {
  if (hasSearched.value) {
    handleSearch()
  }
}

// 导航方法
const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const goToUser = (userId) => {
  router.push(`/user/${userId}`)
}

const goToTag = (tagId) => {
  router.push(`/tag/${tagId}`)
}

// 加载搜索历史和热词
const loadSuggestions = async () => {
  try {
    const [historyRes, hotRes] = await Promise.all([
      getSearchHistory(),
      getHotWords()
    ])
    if (historyRes.code === 20000 || historyRes.code === 200) {
      searchHistory.value = historyRes.data || []
    }
    if (hotRes.code === 20000 || hotRes.code === 200) {
      hotWords.value = hotRes.data || []
    }
  } catch (error) {
    // 加载失败
  }
}

// 快速搜索
const handleQuickSearch = (word) => {
  keyword.value = word
  handleSearch()
}

// 删除单条历史
const handleDeleteHistory = async (word) => {
  try {
    await deleteSearchHistory(word)
    searchHistory.value = searchHistory.value.filter(h => h !== word)
  } catch (error) {
    // 删除失败
  }
}

// 清空历史
const handleClearHistory = async () => {
  try {
    await clearSearchHistory()
    searchHistory.value = []
    ElMessage.success('已清空搜索历史')
  } catch (error) {
    // 清空失败
  }
}

// 初始化
onMounted(() => {
  // 加载搜索历史和热词
  loadSuggestions()
  
  // 从URL恢复搜索状态
  if (route.query.q) {
    keyword.value = route.query.q
    activeTab.value = route.query.tab || 'all'
    handleSearch()
  }
})

// 监听路由变化
watch(
  () => route.query.q,
  (newKeyword) => {
    if (newKeyword && newKeyword !== keyword.value) {
      keyword.value = newKeyword
      handleSearch()
    }
  }
)
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 24px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto 20px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 4px 16px;
}

.search-tabs {
  display: flex;
  justify-content: center;
}

.search-tabs .count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 4px;
}

.search-content {
  min-height: 400px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--el-text-color-secondary);
}

.empty-state .search-icon,
.empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: var(--el-border-color);
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.empty-state .tips {
  font-size: 14px;
  margin-top: 8px;
}

.result-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.empty-section {
  text-align: center;
  padding: 40px;
  color: var(--el-text-color-secondary);
}

.search-meta {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* 搜索历史和热词 */
.search-suggestions {
  max-width: 600px;
  margin: 0 auto;
}

.suggestion-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.suggestion-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-tag:hover {
  transform: translateY(-2px);
}

.suggestion-tag.hot {
  position: relative;
}

.hot-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 10px;
  font-weight: bold;
  margin-right: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-page {
    padding: 12px;
  }
  
  .user-list {
    grid-template-columns: 1fr;
  }
}
</style>
