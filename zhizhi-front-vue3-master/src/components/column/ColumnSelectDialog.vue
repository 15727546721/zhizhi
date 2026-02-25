<template>
  <el-dialog
    :model-value="modelValue"
    title="选择专栏"
    width="600px"
    @close="handleClose"
  >
    <div class="column-select-dialog">
      <!-- 提示信息 -->
      <el-alert
        v-if="!hasColumns"
        title="您还没有创建专栏"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <div>请先创建专栏后再添加文章</div>
          <el-button type="primary" size="small" style="margin-top: 8px" @click="handleCreate">
            创建专栏
          </el-button>
        </template>
      </el-alert>

      <!-- 专栏列表 -->
      <div v-else class="column-list">
        <div class="list-header">
          <span>已选择 {{ selectedIds.length }} / {{ maxSelect }} 个专栏</span>
          <el-button type="primary" size="small" link @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建新专栏
          </el-button>
        </div>

        <el-checkbox-group v-model="selectedIds" :max="maxSelect">
          <div
            v-for="column in publishedColumns"
            :key="column.id"
            class="column-item"
            :class="{ disabled: !canSelect(column.id) }"
          >
            <el-checkbox :label="column.id" :disabled="!canSelect(column.id)">
              <div class="column-content">
                <img
                  v-if="column.coverUrl"
                  :src="column.coverUrl"
                  :alt="column.name"
                  class="column-cover"
                />
                <div v-else class="column-cover default">
                  <el-icon><Collection /></el-icon>
                </div>
                <div class="column-info">
                  <div class="column-name">{{ column.name }}</div>
                  <div class="column-stats">
                    <span>{{ column.postCount }} 篇文章</span>
                    <span>{{ column.subscribeCount }} 订阅</span>
                  </div>
                </div>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>

        <el-empty v-if="publishedColumns.length === 0" description="暂无已发布的专栏" />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleConfirm">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus, Collection } from '@element-plus/icons-vue'
import type { ColumnVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import { useUserStore } from '@/stores/module/user'

interface Props {
  modelValue: boolean
  selected?: number[]
  maxSelect?: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', columnIds: number[]): void
  (e: 'create'): void
}

const props = withDefaults(defineProps<Props>(), {
  selected: () => [],
  maxSelect: 3,
})

const emit = defineEmits<Emits>()

const columnStore = useColumnStore()
const userStore = useUserStore()

const selectedIds = ref<number[]>([])

// 只显示已发布的专栏
const publishedColumns = computed(() => {
  return columnStore.myColumns.filter((c) => c.status === 1)
})

const hasColumns = computed(() => publishedColumns.value.length > 0)

// 判断是否可以选择
const canSelect = (columnId: number) => {
  return selectedIds.value.includes(columnId) || selectedIds.value.length < props.maxSelect
}

// 监听selected变化
watch(
  () => props.selected,
  (newSelected) => {
    selectedIds.value = [...newSelected]
  },
  { immediate: true }
)

// 监听dialog打开,加载专栏列表
watch(
  () => props.modelValue,
  async (visible) => {
    if (visible && userStore.userInfo) {
      await columnStore.fetchMyColumns(userStore.userInfo.id)
    }
  }
)

const handleConfirm = () => {
  emit('confirm', selectedIds.value)
  handleClose()
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleCreate = () => {
  emit('create')
}
</script>

<style scoped lang="scss">
.column-select-dialog {
  min-height: 200px;
}

.column-list {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;

    span {
      font-size: 14px;
      color: #606266;
    }
  }

  .el-checkbox-group {
    width: 100%;
  }

  .column-item {
    padding: 12px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 12px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background: #f5f7fa;
    }

    &.disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    :deep(.el-checkbox) {
      width: 100%;

      .el-checkbox__label {
        width: 100%;
      }
    }
  }

  .column-content {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .column-cover {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    object-fit: cover;
    flex-shrink: 0;

    &.default {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24px;
    }
  }

  .column-info {
    flex: 1;
    min-width: 0;
  }

  .column-name {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .column-stats {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #909399;
  }
}
</style>
