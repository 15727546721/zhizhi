<template>
  <div class="column-square">
    <div class="page-header">
      <h1>专栏广场</h1>
      <p>发现优质专栏,订阅感兴趣的内容</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索专栏"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-radio-group v-model="sortType" class="sort-tabs" @change="handleSortChange">
        <el-radio-button label="latest">最新</el-radio-button>
        <el-radio-button label="hot">热门</el-radio-button>
        <el-radio-button label="subscribe">订阅最多</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 专栏列表 -->
    <div class="column-list-section">
      <h2 v-if="!searchKeyword" class="section-title">全部专栏</h2>
      <h2 v-else class="section-title">搜索结果</h2>

      <el-skeleton v-if="loading" :rows="3" animated />

      <div v-else-if="displayColumns && displayColumns.length > 0" class="column-grid">
        <ColumnCard v-for="column in displayColumns" :key="column.id" :column="column" />
      </div>

      <el-empty v-else description="暂无专栏" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import ColumnCard from '@/components/column/ColumnCard.vue'
import type { ColumnVO } from '@/types/column'
import * as columnApi from '@/api/column'

const searchKeyword = ref('')
const sortType = ref<'latest' | 'hot' | 'subscribe'>('latest')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const loading = ref(false)

const columns = ref<ColumnVO[]>([])

// 前端排序：推荐的专栏排在最前面
const displayColumns = computed(() => {
  if (!columns.value || columns.value.length === 0) {
    return []
  }
  
  // 复制数组避免修改原数组
  const sorted = [...columns.value]
  
  // 按 isRecommended 排序，推荐的在前
  sorted.sort((a, b) => {
    // isRecommended 为 true 的排在前面
    if (a.isRecommended && !b.isRecommended) return -1
    if (!a.isRecommended && b.isRecommended) return 1
    return 0
  })
  
  return sorted
})

onMounted(() => {
  loadColumns()
})

const loadColumns = async () => {
  try {
    loading.value = true

    if (searchKeyword.value.trim()) {
      // 搜索模式
      const res = await columnApi.searchColumns({
        keyword: searchKeyword.value.trim(),
        page: currentPage.value,
        size: pageSize.value,
      }) as any
      if (res.code === 20000 && res.data) {
        columns.value = res.data.data || []
        total.value = res.data.total || 0
      }
    } else {
      // 广场模式
      const res = await columnApi.getColumnSquare({
        sortType: sortType.value,
        page: currentPage.value,
        size: pageSize.value,
      }) as any
      if (res.code === 20000 && res.data) {
        columns.value = res.data.data || []
        total.value = res.data.total || 0
      }
    }
  } catch (error) {
    columns.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadColumns()
}

const handleSortChange = () => {
  currentPage.value = 1
  loadColumns()
}

const handlePageChange = () => {
  loadColumns()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadColumns()
}
</script>

<style scoped lang="scss">
.column-square {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 32px;
    font-weight: 700;
    color: #1f2329;
    margin: 0 0 12px 0;
  }

  p {
    font-size: 16px;
    color: #646a73;
    margin: 0;
  }
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  align-items: center;

  .search-input {
    flex: 1;
    max-width: 400px;
  }

  .sort-tabs {
    flex-shrink: 0;
  }
}

.column-list-section {
  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f2329;
    margin: 0 0 20px 0;
  }

  .column-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 32px;
  }

  .pagination {
    display: flex;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .column-square {
    padding: 16px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;

    .search-input {
      max-width: none;
    }
  }

  .column-grid {
    grid-template-columns: 1fr;
  }
}
</style>
