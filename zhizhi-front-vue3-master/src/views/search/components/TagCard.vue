<template>
  <div class="tag-card" @click="$emit('click')">
    <div class="tag-icon">
      <el-icon><PriceTag /></el-icon>
    </div>
    <div class="tag-info">
      <div class="tag-name">
        <span>{{ tag.name }}</span>
        <el-tag v-if="tag.isRecommended" size="small" type="warning">热门</el-tag>
      </div>
      <p class="tag-desc" v-if="tag.description">{{ tag.description }}</p>
      <div class="tag-count">{{ formatCount(tag.usageCount) }} 篇帖子</div>
    </div>
  </div>
</template>

<script setup>
import { PriceTag } from '@element-plus/icons-vue'

defineProps({
  tag: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const formatCount = (count) => {
  if (!count) return 0
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}
</script>

<style scoped>
.tag-card {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--el-border-color-light);
  min-width: 200px;
}

.tag-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-5);
}

.tag-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
  color: var(--el-color-primary);
  font-size: 20px;
}

.tag-info {
  flex: 1;
  min-width: 0;
}

.tag-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.tag-desc {
  margin: 0 0 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-count {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
</style>
