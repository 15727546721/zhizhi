<template>
  <div class="comment-header">
    <h3 class="section-title">评论</h3>
    <div class="header-content">
      <div class="comment-count">{{ count }} 条评论</div>
      <div class="sort-options">
        <div
          v-for="(option, index) in sortOptions"
          :key="index"
          :class="['sort-option', { active: currentSort === option.value }]"
          @click="handleSortChange(option.value)"
        >
          {{ option.label }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { SortOption } from './types'

defineProps<{
  count: number
  currentSort: string
}>()

const emit = defineEmits<{
  (e: 'sort-change', value: string): void
}>()

const sortOptions = ref<SortOption[]>([
  { label: '最热', value: 'HOT' },
  { label: '最新', value: 'NEW' }
])

const handleSortChange = (sortValue: string) => {
  emit('sort-change', sortValue)
}
</script>

<style scoped>
.comment-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
  margin-bottom: 12px;
}

.comment-count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.sort-options {
  display: flex;
  margin-bottom: 20px;
}

.sort-option {
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background-color: #f5f5f5;
  margin-right: 8px;
}

.sort-option.active {
  background-color: #1890ff;
  color: white;
}
</style>
