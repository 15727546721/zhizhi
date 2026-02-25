<template>
  <div class="home-page">
    <div class="content-layout" :class="{ 'feed-mode': viewMode === 'feed' }">
      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 顶部工具栏 -->
        <div class="top-toolbar">
          <!-- 标签筛选 -->
          <div class="tag-filter-wrapper">
            <div class="tag-filter" :class="{ 'is-expanded': tagFilterExpanded }">
              <el-tag 
                :type="!selectedTagId ? 'primary' : 'info'"
                class="filter-tag"
                :class="{ 'is-active': !selectedTagId }"
                @click="clearTagFilter"
              >
                全部
              </el-tag>
              <el-tag 
                v-for="tag in displayedTags" 
                :key="tag.id"
                :type="selectedTagId === tag.id ? 'primary' : 'info'"
                class="filter-tag"
                :class="{ 'is-active': selectedTagId === tag.id }"
                @click="selectTag(tag.id)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
            
            <!-- 展开/收起按钮 -->
            <div 
              v-if="hotTags.length > TAG_COLLAPSED_COUNT" 
              class="tag-expand-btn"
              @click="toggleTagFilter"
            >
              <span>{{ tagFilterExpanded ? '收起' : `展开 ${hotTags.length - TAG_COLLAPSED_COUNT}+` }}</span>
              <el-icon :class="{ 'is-rotate': tagFilterExpanded }">
                <ArrowDown />
              </el-icon>
            </div>
          </div>
          
          <!-- 浏览模式切换按钮 -->
          <div class="view-mode-switch">
            <el-tooltip :content="viewMode === 'list' ? '切换到沉浸模式' : '切换到列表模式'" placement="bottom">
              <el-button 
                :icon="viewMode === 'list' ? Grid : List" 
                circle 
                size="small"
                @click="toggleViewMode"
              />
            </el-tooltip>
          </div>
        </div>

        <!-- 沉浸模式：快速发帖 -->
        <div v-if="viewMode === 'feed'" class="quick-post-section">
          <CreatePost @post-published="handlePostPublished" />
        </div>

        <!-- 列表模式 -->
        <post-list 
          v-if="viewMode === 'list'"
          ref="postListRef"
          :active-tab="activeTab"
          :tag-id="selectedTagId"
        />
        
        <!-- 沉浸模式 -->
        <PostFeedList 
          v-else
          ref="postFeedListRef"
          :active-tab="activeTab"
          :tag-id="selectedTagId"
        />
      </div>

      <!-- 右侧边栏（仅列表模式显示） -->
      <div class="right-sidebar" v-show="viewMode === 'list'">
        <!-- 帖子排行榜 -->
        <post-ranking />
        
        <!-- 作者排行榜 -->
        <user-ranking />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Grid, List, ArrowDown } from '@element-plus/icons-vue'
import { getHotTags } from '@/api/tag'
import type { Tag } from '@/types'
import UserRanking from './components/UserRanking.vue'
import PostRanking from './components/PostRanking.vue'
import PostList from './components/PostList.vue'
import PostFeedList from './components/PostFeedList.vue'
import CreatePost from './components/CreatePost.vue'

defineOptions({
  name: 'HomePage'
})

interface PostListInstance {
  refresh?: () => void
}

const router = useRouter()
const route = useRoute()

// ==================== 常量配置 ====================
const TAG_COLLAPSED_COUNT = 10  // 收起时显示的标签数
const TAG_MAX_COUNT = 30        // 展开后最大显示数

// ==================== 响应式数据 ====================
const activeTab = computed(() => (route.query.tab as string) || 'latest')
const hotTags = ref<Tag[]>([])
const viewMode = ref(localStorage.getItem('homeViewMode') || 'list')
const postListRef = ref<PostListInstance | null>(null)
const postFeedListRef = ref<PostListInstance | null>(null)
const selectedTagId = ref<number | null>(null)
const tagFilterExpanded = ref(false)

// 计算显示的标签
const displayedTags = computed(() => {
  if (tagFilterExpanded.value) {
    return hotTags.value.slice(0, TAG_MAX_COUNT)
  }
  return hotTags.value.slice(0, TAG_COLLAPSED_COUNT)
})

// ==================== 方法 ====================

// 切换浏览模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'list' ? 'feed' : 'list'
  localStorage.setItem('homeViewMode', viewMode.value)
}

// 切换标签筛选器展开状态
const toggleTagFilter = () => {
  tagFilterExpanded.value = !tagFilterExpanded.value
}

// 发帖成功后刷新列表
const handlePostPublished = () => {
  if (viewMode.value === 'feed') {
    postFeedListRef.value?.refresh?.()
  } else {
    postListRef.value?.refresh?.()
  }
}

// 标签筛选
const selectTag = (tagId: number) => {
  selectedTagId.value = tagId
}

const clearTagFilter = () => {
  selectedTagId.value = null
}

// 加载热门标签
const loadHotTags = async () => {
  try {
    const response = await getHotTags('all', TAG_MAX_COUNT)
    if (response && response.data) {
      hotTags.value = response.data
    }
  } catch (error) {
    // 加载失败
  }
}

// 监听tab变化，刷新列表
watch(activeTab, () => {
  // tab变化时清除标签筛选
  selectedTagId.value = null
})

// 生命周期
onMounted(() => {
  loadHotTags()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f4f5f5;
}

.content-layout {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 24px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  background: white;
  border-radius: 4px;
  overflow: visible;
  align-self: flex-start;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.right-sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-self: flex-start;
}

.sidebar-widget {
  background: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.widget-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #252933;
  padding-left: 8px;
  border-left: 4px solid #1e80ff;
  line-height: 1;
}

.top-toolbar {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e6eb;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.tag-filter-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  max-height: 36px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.tag-filter.is-expanded {
  max-height: 200px;
}

.filter-tag {
  cursor: pointer;
  border: none;
  background-color: #f4f5f5;
  color: #8a919f;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  border-radius: 14px;
  transition: all 0.2s ease;
  font-size: 13px;
  flex-shrink: 0;
}

.filter-tag:hover {
  background-color: #e8f3ff;
  color: #1e80ff;
}

.filter-tag.is-active {
  background-color: #1e80ff;
  color: #fff;
}

.filter-tag.is-active:hover {
  background-color: #409eff;
  color: #fff;
}

.tag-expand-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8a919f;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 2px 0;
}

.tag-expand-btn:hover {
  color: #1e80ff;
}

.tag-expand-btn .el-icon {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.tag-expand-btn .el-icon.is-rotate {
  transform: rotate(180deg);
}

.view-mode-switch {
  flex-shrink: 0;
}

.view-mode-switch .el-button {
  color: #8a919f;
  border-color: #e4e6eb;
}

.view-mode-switch .el-button:hover {
  color: #1e80ff;
  border-color: #1e80ff;
}

.quick-post-section {
  padding: 16px;
  border-bottom: 1px solid #e4e6eb;
}

/* 沉浸模式样式 */
.feed-mode .main-content {
  max-width: 800px;
  margin: 0 auto;
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .content-layout {
    max-width: 100%;
  }
}

@media screen and (max-width: 992px) {
  .content-layout {
    flex-direction: column;
  }
  
  .right-sidebar {
    width: 100%;
  }
}

@media screen and (max-width: 576px) {
  .content-layout {
    padding: 12px;
    gap: 16px;
  }

  .top-nav :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 14px;
  }
}
</style>