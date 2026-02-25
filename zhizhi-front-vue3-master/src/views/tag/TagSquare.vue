<template>
  <div class="tag-square-page">
    <div class="page-header">
      <h1>标签广场</h1>
      <p class="page-desc">发现感兴趣的话题，探索更多优质内容</p>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索标签..."
        :prefix-icon="Search"
        clearable
        size="large"
        @input="handleSearch"
      />
    </div>

    <!-- 热门标签 -->
    <section v-if="!searchKeyword" class="tag-section">
      <div class="section-header">
        <h2>🔥 热门标签</h2>
      </div>
      <div v-loading="hotLoading" class="tag-grid">
        <div
          v-for="tag in hotTags"
          :key="tag.id"
          class="tag-card hot"
          @click="goToTag(tag.id)"
        >
          <div class="tag-name"># {{ tag.name }}</div>
          <div class="tag-desc">{{ tag.description || '暂无描述' }}</div>
          <div class="tag-stats">
            <span>{{ tag.usageCount || 0 }} 篇文章</span>
          </div>
        </div>
        <el-empty v-if="!hotLoading && hotTags.length === 0" description="暂无热门标签" />
      </div>
    </section>

    <!-- 全部标签 / 搜索结果 -->
    <section class="tag-section">
      <div class="section-header">
        <h2>{{ searchKeyword ? '搜索结果' : '📚 全部标签' }}</h2>
        <span v-if="!searchKeyword" class="tag-count">共 {{ allTags.length }} 个标签</span>
      </div>
      <div v-loading="allLoading" class="tag-grid">
        <div
          v-for="tag in displayTags"
          :key="tag.id"
          class="tag-card"
          @click="goToTag(tag.id)"
        >
          <div class="tag-name"># {{ tag.name }}</div>
          <div class="tag-desc">{{ tag.description || '暂无描述' }}</div>
          <div class="tag-stats">
            <span>{{ tag.usageCount || 0 }} 篇文章</span>
            <el-tag v-if="tag.isRecommended" type="warning" size="small">推荐</el-tag>
          </div>
        </div>
        <el-empty v-if="!allLoading && displayTags.length === 0" :description="searchKeyword ? '未找到相关标签' : '暂无标签'" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getTagList, getHotTags, searchTags } from '@/api/tag'
import type { Tag } from '@/types'

defineOptions({ name: 'TagSquare' })

const router = useRouter()

const searchKeyword = ref('')
const hotTags = ref<Tag[]>([])
const allTags = ref<Tag[]>([])
const searchResults = ref<Tag[]>([])
const hotLoading = ref(false)
const allLoading = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

const displayTags = computed(() => {
  if (searchKeyword.value) {
    return searchResults.value
  }
  return allTags.value
})

const loadHotTags = async () => {
  hotLoading.value = true
  try {
    const res = await getHotTags('all', 12)
    if (res.code === 20000) {
      hotTags.value = res.data || []
    }
  } catch (error) {
    // 加载失败
  } finally {
    hotLoading.value = false
  }
}

const loadAllTags = async () => {
  allLoading.value = true
  try {
    const res = await getTagList()
    if (res.code === 20000) {
      allTags.value = res.data || []
    }
  } catch (error) {
    // 加载失败
  } finally {
    allLoading.value = false
  }
}

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  searchTimer = setTimeout(async () => {
    allLoading.value = true
    try {
      const res = await searchTags(searchKeyword.value.trim())
      if (res.code === 20000) {
        searchResults.value = res.data || []
      }
    } catch (error) {
      // 搜索失败
    } finally {
      allLoading.value = false
    }
  }, 300)
}

const goToTag = (tagId: number) => {
  router.push(`/tag/${tagId}`)
}

onMounted(() => {
  loadHotTags()
  loadAllTags()
})
</script>

<style scoped>
.tag-square-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.search-section {
  max-width: 500px;
  margin: 0 auto 40px;
}

.tag-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.tag-count {
  font-size: 14px;
  color: #999;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.tag-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.tag-card.hot {
  background: linear-gradient(135deg, #fff9f0 0%, #fff 100%);
  border-color: #ffe4c4;
}

.tag-card.hot:hover {
  border-color: #ff9500;
  box-shadow: 0 2px 12px rgba(255, 149, 0, 0.15);
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.tag-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

@media (max-width: 768px) {
  .tag-square-page {
    padding: 16px;
  }
  
  .tag-grid {
    grid-template-columns: 1fr;
  }
}
</style>
