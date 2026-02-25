<template>
  <div class="my-columns">
    <div class="page-header">
      <h1>我的专栏</h1>
      <el-button type="primary" :disabled="!canCreate" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建专栏
      </el-button>
    </div>

    <el-alert v-if="!canCreate" type="warning" :closable="false" show-icon>
      专栏数量已达上限(10个),请删除部分专栏后再创建
    </el-alert>

    <el-skeleton v-if="loading" :rows="5" animated />

    <div v-else-if="columns.length > 0" class="columns-grid">
      <div v-for="column in columns" :key="column.id" class="column-item">
        <ColumnCard :column="column" :show-subscribe-button="false" />
        <div class="column-actions">
          <el-button size="small" @click="handleEdit(column)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button size="small" @click="handleManage(column.id)">
            <el-icon><Setting /></el-icon>
            管理
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(column)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="还没有创建专栏">
      <el-button type="primary" @click="showCreateDialog = true">立即创建</el-button>
    </el-empty>

    <!-- 创建/编辑对话框 -->
    <ColumnFormDialog
      v-model="showCreateDialog"
      :column="editingColumn"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Setting, Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import ColumnCard from '@/components/column/ColumnCard.vue'
import ColumnFormDialog from '@/components/column/ColumnFormDialog.vue'
import type { ColumnVO, ColumnDetailVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import { useUserStore } from '@/stores/module/user'

const router = useRouter()
const columnStore = useColumnStore()
const userStore = useUserStore()

const loading = ref(false)
const showCreateDialog = ref(false)
const editingColumn = ref<ColumnDetailVO | null>(null)

const columns = computed(() => columnStore.myColumns)
const canCreate = computed(() => columnStore.canCreateColumn)

onMounted(async () => {
  if (userStore.userInfo) {
    loading.value = true
    await columnStore.fetchMyColumns(userStore.userInfo.id)
    loading.value = false
  }
})

const handleEdit = (column: ColumnVO) => {
  editingColumn.value = column as ColumnDetailVO
  showCreateDialog.value = true
}

const handleManage = (columnId: number) => {
  router.push(`/columns/${columnId}/manage`)
}

const handleDelete = async (column: ColumnVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除专栏"${column.name}"吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await columnStore.deleteColumn(column.id)
  } catch (error) {
    // 用户取消
  }
}

const handleSuccess = async () => {
  // 关闭对话框
  showCreateDialog.value = false
  editingColumn.value = null
  
  if (userStore.userInfo) {
    loading.value = true
    try {
      await columnStore.fetchMyColumns(userStore.userInfo.id)
    } finally {
      loading.value = false
    }
  }
}
</script>

<style scoped lang="scss">
.my-columns {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 28px;
    font-weight: 700;
    color: #1f2329;
    margin: 0;
  }
}

.columns-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.column-item {
  position: relative;

  .column-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;

    .el-button {
      flex: 1;
    }
  }
}

@media (max-width: 768px) {
  .columns-grid {
    grid-template-columns: 1fr;
  }
}
</style>
