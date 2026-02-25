<template>
  <div class="user-subscriptions">
    <div class="page-header">
      <h1>我的订阅</h1>
      <p>已订阅 {{ total }} 个专栏</p>
    </div>

    <el-skeleton v-if="loading" :rows="3" animated />

    <div v-else-if="columns.length > 0" class="subscriptions-grid">
      <ColumnCard
        v-for="column in columns"
        :key="column.id"
        :column="column"
        @unsubscribe="handleUnsubscribe"
      />
    </div>

    <el-empty v-else description="还没有订阅任何专栏" />

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
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ColumnCard from '@/components/column/ColumnCard.vue'
import * as columnApi from '@/api/column'
import type { ColumnVO } from '@/types/column'

const loading = ref(false)
const columns = ref<ColumnVO[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

onMounted(() => {
  loadSubscriptions()
})

const loadSubscriptions = async () => {
  try {
    loading.value = true
    
    // 调用后端API获取用户订阅的专栏列表
    const res = await columnApi.getUserSubscriptions(currentPage.value, pageSize.value) as any
    
    if (res.code === 20000 && res.data) {
      // 后端返回的是 PageResponse 结构：{ pageNo, pageSize, total, data }
      columns.value = res.data.data || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleUnsubscribe = (columnId: number) => {
  // 从列表中移除
  columns.value = columns.value.filter(c => c.id !== columnId)
  total.value--
  ElMessage.success('已取消订阅')
}

const handlePageChange = () => {
  loadSubscriptions()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadSubscriptions()
}
</script>

<style scoped lang="scss">
.user-subscriptions {
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

.subscriptions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.pagination {
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .user-subscriptions {
    padding: 16px;
  }

  .subscriptions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
