<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form ref="queryFormRef" :model="queryParams" :inline="true">
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索专栏名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已归档" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item label="推荐" prop="isRecommended">
        <el-select
          v-model="queryParams.isRecommended"
          placeholder="全部"
          clearable
          style="width: 120px"
        >
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <i-ep-search />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <i-ep-refresh />
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">总专栏数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ statistics.publishedCount || 0 }}</div>
            <div class="stat-label">已发布</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ statistics.recommendedCount || 0 }}</div>
            <div class="stat-label">推荐专栏</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalSubscriptions || 0 }}</div>
            <div class="stat-label">总订阅数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮 -->
    <el-row style="margin-bottom: 10px">
      <el-button
        type="danger"
        :disabled="ids.length === 0"
        @click="handleBatchDelete"
      >
        <i-ep-delete />
        批量删除
      </el-button>
    </el-row>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="columnList"
      @selection-change="handleSelectionChange"
      border
    >
      <el-table-column type="selection" width="55" align="center" />
      
      <el-table-column label="封面" align="center" width="100">
        <template #default="scope">
          <el-image
            v-if="scope.row.coverUrl"
            :src="scope.row.coverUrl"
            :preview-src-list="[scope.row.coverUrl]"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="专栏名称" min-width="150" />

      <el-table-column label="作者" align="center" width="120">
        <template #default="scope">
          <div style="display: flex; align-items: center; justify-content: center">
            <el-avatar :src="scope.row.userAvatar" :size="30" style="margin-right: 8px" />
            <span>{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">草稿</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">已发布</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="warning">已归档</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="推荐" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isRecommended" type="danger">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="postCount" label="文章数" align="center" width="80" />
      <el-table-column prop="subscribeCount" label="订阅数" align="center" width="80" />

      <el-table-column prop="createTime" label="创建时间" align="center" width="160" />

      <el-table-column label="操作" align="center" width="280" fixed="right">
        <template #default="scope">
          <el-button
            v-if="!scope.row.isRecommended"
            type="warning"
            size="small"
            link
            @click="handleRecommend(scope.row.id, 1)"
          >
            <i-ep-star />
            推荐
          </el-button>
          <el-button
            v-else
            type="info"
            size="small"
            link
            @click="handleRecommend(scope.row.id, 0)"
          >
            <i-ep-star />
            取消推荐
          </el-button>

          <el-button
            v-if="scope.row.status !== 2"
            type="warning"
            size="small"
            link
            @click="handleArchive(scope.row.id)"
          >
            <i-ep-box />
            归档
          </el-button>

          <el-button
            type="danger"
            size="small"
            link
            @click="handleDelete(scope.row.id)"
          >
            <i-ep-delete />
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-if="total > 0"
      v-model:total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.size"
      @pagination="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import {
  getColumnList,
  getColumnStatistics,
  deleteColumn,
  archiveColumn,
  setRecommend,
  batchDeleteColumns
} from '@/api/column'

defineOptions({
  name: 'Column',
  inheritAttrs: false
})

const queryFormRef = ref(ElForm)
const loading = ref(false)
const ids = ref<number[]>([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: undefined as number | undefined,
  isRecommended: undefined as number | undefined
})

const columnList = ref<any[]>([])

const statistics = reactive({
  totalCount: 0,
  publishedCount: 0,
  draftCount: 0,
  archivedCount: 0,
  recommendedCount: 0,
  totalSubscriptions: 0
})

/** 查询 */
function handleQuery() {
  loading.value = true
  getColumnList(queryParams)
    .then(({ data }: any) => {
      if (data && data.data) {
        columnList.value = data.data
        total.value = data.total || 0
      }
    })
    .finally(() => {
      loading.value = false
    })
}

/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields()
  queryParams.page = 1
  handleQuery()
}

/** 加载统计数据 */
function loadStatistics() {
  getColumnStatistics().then(({ data }: any) => {
    Object.assign(statistics, data)
  })
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id)
}

/** 设置推荐 */
function handleRecommend(id: number, isRecommended: number) {
  const action = isRecommended === 1 ? '推荐' : '取消推荐'
  ElMessageBox.confirm(`确认${action}该专栏?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true
    setRecommend(id, isRecommended)
      .then(() => {
        ElMessage.success(`${action}成功`)
        handleQuery()
        loadStatistics()
      })
      .finally(() => {
        loading.value = false
      })
  })
}

/** 归档专栏 */
function handleArchive(id: number) {
  ElMessageBox.confirm('确认归档该专栏?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true
    archiveColumn(id)
      .then(() => {
        ElMessage.success('归档成功')
        handleQuery()
        loadStatistics()
      })
      .finally(() => {
        loading.value = false
      })
  })
}

/** 删除 */
function handleDelete(id: number) {
  ElMessageBox.confirm('确认删除该专栏? 此操作不可恢复!', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true
    deleteColumn(id)
      .then(() => {
        ElMessage.success('删除成功')
        handleQuery()
        loadStatistics()
      })
      .finally(() => {
        loading.value = false
      })
  })
}

/** 批量删除 */
function handleBatchDelete() {
  if (ids.value.length === 0) {
    ElMessage.warning('请选择要删除的专栏')
    return
  }

  ElMessageBox.confirm(`确认删除选中的 ${ids.value.length} 个专栏? 此操作不可恢复!`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true
    batchDeleteColumns(ids.value)
      .then(() => {
        ElMessage.success('批量删除成功')
        ids.value = []
        handleQuery()
        loadStatistics()
      })
      .finally(() => {
        loading.value = false
      })
  })
}

onMounted(() => {
  handleQuery()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.stat-card {
  text-align: center;
  padding: 10px 0;

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #409eff;
    margin-bottom: 8px;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}
</style>
