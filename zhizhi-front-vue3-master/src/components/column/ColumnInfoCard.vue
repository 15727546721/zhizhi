<template>
  <div v-if="columns.length > 0" class="column-info-card">
    <div class="card-header">
      <el-icon><Collection /></el-icon>
      <span>所属专栏</span>
    </div>

    <div class="card-body">
      <!-- 专栏选择器(多专栏时) -->
      <el-select
        v-if="columns.length > 1"
        v-model="currentColumnId"
        placeholder="选择专栏"
        class="column-selector"
        @change="handleColumnChange"
      >
        <el-option
          v-for="column in columns"
          :key="column.id"
          :label="column.name"
          :value="column.id"
        />
      </el-select>

      <!-- 单个专栏信息 -->
      <div v-else class="single-column">
        <router-link :to="`/columns/${columns[0].id}`" class="column-link">
          {{ columns[0].name }}
        </router-link>
      </div>

      <!-- 导航按钮 -->
      <div v-if="showNavigation && currentColumnId" class="navigation-buttons">
        <el-button
          :disabled="!navigation.previousPostId"
          size="small"
          @click="handleNavigate(navigation.previousPostId)"
        >
          <el-icon><ArrowLeft /></el-icon>
          上一篇
        </el-button>
        <el-button
          :disabled="!navigation.nextPostId"
          size="small"
          @click="handleNavigate(navigation.nextPostId)"
        >
          下一篇
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 上下篇文章标题 -->
      <div v-if="showNavigation && (navigation.previousPostTitle || navigation.nextPostTitle)" class="post-titles">
        <div v-if="navigation.previousPostTitle" class="post-title prev">
          <span class="label">上一篇:</span>
          <router-link :to="`/posts/${navigation.previousPostId}`" class="title-link">
            {{ navigation.previousPostTitle }}
          </router-link>
        </div>
        <div v-if="navigation.nextPostTitle" class="post-title next">
          <span class="label">下一篇:</span>
          <router-link :to="`/posts/${navigation.nextPostId}`" class="title-link">
            {{ navigation.nextPostTitle }}
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { ColumnVO } from '@/types/column'
import * as columnApi from '@/api/column'

interface Props {
  columns: ColumnVO[]
  currentPostId?: number
  showNavigation?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showNavigation: true,
})

const router = useRouter()
const currentColumnId = ref<number>()
const navigation = ref<{
  previousPostId: number | null
  previousPostTitle: string | null
  nextPostId: number | null
  nextPostTitle: string | null
}>({
  previousPostId: null,
  previousPostTitle: null,
  nextPostId: null,
  nextPostTitle: null,
})

// 初始化当前专栏
watch(
  () => props.columns,
  (newColumns) => {
    if (newColumns.length > 0 && !currentColumnId.value) {
      currentColumnId.value = newColumns[0].id
    }
  },
  { immediate: true }
)

const handleColumnChange = async (columnId: number) => {
  // 切换专栏时,重新加载导航信息
  await loadNavigation(columnId)
}

const loadNavigation = async (columnId: number) => {
  if (!props.currentPostId) return

  try {
    const res = await columnApi.getPostNavigation(columnId, props.currentPostId) as any
    if (res.code === 20000 && res.data) {
      navigation.value = res.data
    }
  } catch (error) {
    console.error('加载导航失败:', error)
  }
}

const handleNavigate = (postId?: number | null) => {
  if (postId) {
    router.push(`/posts/${postId}`)
  }
}

// 监听当前专栏变化,加载导航
watch(
  currentColumnId,
  (newColumnId) => {
    if (newColumnId && props.showNavigation) {
      loadNavigation(newColumnId)
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.column-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 16px;

  .el-icon {
    font-size: 18px;
    color: #409eff;
  }
}

.card-body {
  .column-selector {
    width: 100%;
    margin-bottom: 16px;
  }

  .single-column {
    margin-bottom: 16px;

    .column-link {
      font-size: 15px;
      color: #409eff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .navigation-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .el-button {
      flex: 1;
    }
  }

  .post-titles {
    border-top: 1px solid #ebeef5;
    padding-top: 16px;

    .post-title {
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-size: 13px;
        color: #909399;
        margin-right: 8px;
      }

      .title-link {
        font-size: 14px;
        color: #303133;
        text-decoration: none;

        &:hover {
          color: #409eff;
        }
      }
    }
  }
}
</style>
