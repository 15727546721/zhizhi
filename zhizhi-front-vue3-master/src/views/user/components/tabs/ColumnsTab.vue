<template>
  <div class="columns-tab">
    <el-skeleton v-if="loading" :rows="3" animated />

    <div v-else-if="columns.length > 0" class="columns-grid">
      <ColumnCard
        v-for="column in columns"
        :key="column.id"
        :column="column"
        :show-subscribe-button="!isOwnProfile"
      />
    </div>

    <el-empty v-else :description="emptyText" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import ColumnCard from '@/components/column/ColumnCard.vue'
import type { ColumnVO } from '@/types/column'
import * as columnApi from '@/api/column'

interface Props {
  userId: number
  isOwnProfile: boolean
}

const props = defineProps<Props>()

const loading = ref(false)
const columns = ref<ColumnVO[]>([])

const emptyText = computed(() => {
  return props.isOwnProfile ? '还没有创建专栏' : 'TA还没有创建专栏'
})

onMounted(() => {
  loadColumns()
})

const loadColumns = async () => {
  loading.value = true
  try {
    const response = await columnApi.getUserColumns(props.userId)
    if (response.code === 20000 && response.data) {
      // 如果不是自己的主页,只显示已发布的专栏
      columns.value = props.isOwnProfile
        ? response.data
        : response.data.filter((c) => c.status === 1)
    }
  } catch (error) {
    // 加载失败
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.columns-tab {
  padding: 20px 0;
}

.columns-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .columns-grid {
    grid-template-columns: 1fr;
  }
}
</style>
