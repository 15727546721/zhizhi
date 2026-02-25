<template>
  <div class="collection-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form :model="searchForm" inline class="search-form">
        <div class="form-row">
          <el-form-item label="集合编号">
            <el-input v-model="searchForm.code" placeholder="请输入集合编号" class="custom-input" />
          </el-form-item>
          <el-form-item label="集合名称">
            <el-input v-model="searchForm.name" placeholder="请输入集合名称" class="custom-input" />
          </el-form-item>
          <el-form-item label="内容体裁">
            <el-select v-model="searchForm.contentType" placeholder="全部" class="custom-select">
              <el-option
                v-for="item in contentTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="search-button" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="筛选方式">
            <el-select v-model="searchForm.filterType" placeholder="全部" class="custom-select">
              <el-option label="全部" value="" />
              <el-option label="rules" value="rules" />
              <el-option label="artificial" value="artificial" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="custom-date-picker"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" class="custom-select">
              <el-option label="全部" value="" />
              <el-option label="已上线" value="online" />
              <el-option label="已下线" value="offline" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </div>
      </el-form>

      <div class="divider"></div>

      <!-- 操作按钮和表格部分 -->
      <div class="table-section">
        <!-- 操作按钮 -->
        <div class="operation-bar">
          <div class="left">
            <el-button type="primary" class="add-button">
              <el-icon><Plus /></el-icon>新建
            </el-button>
            <el-button>批量导入</el-button>
          </div>
          <div class="right">
            <el-button class="icon-text-button">
              <el-icon><Download /></el-icon>下载
            </el-button>
            <div class="icon-group">
              <el-button class="icon-only-button">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
              <el-button class="icon-only-button">
                <el-icon><Sort /></el-icon>
              </el-button>
              <el-button class="icon-only-button">
                <el-icon><Setting /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table :data="tableData" style="width: 100%" class="custom-table">
          <el-table-column type="selection" width="40" />
          <el-table-column label="#" type="index" width="50" align="center" />
          <el-table-column prop="code" label="集合编号" min-width="100" />
          <el-table-column prop="name" label="集合名称" min-width="120" />
          <el-table-column prop="contentType" label="内容体裁" min-width="120">
            <template #default="{ row }">
              <div class="content-type">
                <el-icon :class="row.contentType.includes('竖版') ? 'vertical-icon' : 'horizontal-icon'">
                  <VideoPlay />
                </el-icon>
                {{ row.contentType }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="filterType" label="筛选方式" min-width="100" />
          <el-table-column prop="contentCount" label="内容量" min-width="80" align="right" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column prop="status" label="状态" min-width="80">
            <template #default="{ row }">
              <div class="status-tag">
                <span class="dot" :class="row.status === 'online' ? 'online' : 'offline'" />
                <span :class="row.status === 'online' ? 'text-success' : 'text-secondary'">
                  {{ row.status === 'online' ? '已上线' : '已下线' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  Download,
  Refresh,
  RefreshRight,
  Sort,
  Setting,
  Plus,
  Search,
  VideoPlay
} from '@element-plus/icons-vue'

// 搜索表单数据
const searchForm = reactive({
  code: '',
  name: '',
  contentType: '',
  filterType: '',
  dateRange: [],
  status: ''
})

// 模拟表格数据
const tableData = ref([
  {
    code: '26',
    name: 'LYJVBKZ',
    contentType: '横版短视频',
    filterType: 'rules',
    contentCount: 82,
    createTime: '1982-10-13 05:53:45',
    status: 'offline'
  },
  {
    code: '708',
    name: 'QXZYY',
    contentType: '横版短视频',
    filterType: 'rules',
    contentCount: 44,
    createTime: '1982-10-13 05:53:45',
    status: 'offline'
  },
  {
    code: '21',
    name: 'GGEK',
    contentType: '横版短视频',
    filterType: 'rules',
    contentCount: 730,
    createTime: '1982-10-13 05:53:45',
    status: 'offline'
  },
  {
    code: '55',
    name: 'CTPEP',
    contentType: '横版短视频',
    filterType: 'artificial',
    contentCount: 179,
    createTime: '1982-10-13 05:53:45',
    status: 'online'
  }
])

// 处理搜索
const handleSearch = () => {
  // TODO: 实现搜索功能
}

// 处理重置
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
}

// 处理新建
const handleAdd = () => {
  // TODO: 实现新建功能
}

// 处理批量导入
const handleBatchImport = () => {
  // TODO: 实现批量导入功能
}

// 处理查看
const handleView = (row: any) => {
  // TODO: 实现查看详情功能
}

// 在 <script setup> 中添加选项数据
const contentTypeOptions = [
  { label: '全部', value: '' },
  { label: '图文', value: '图文' },
  { label: '横版短视频', value: '横版短视频' },
  { label: '竖版小视频', value: '竖版小视频' }
]
</script>

<style scoped>
.collection-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.content-wrapper {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.search-form {
  padding: 24px 24px 0;
}

.form-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
}

.form-row .el-form-item {
  margin-bottom: 0;
  margin-right: 32px;
}

.form-row .el-form-item:last-child {
  margin-right: 0;
  margin-left: auto;
}

.divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 0 24px;
}

.table-section {
  padding: 24px;
}

.search-form :deep(.custom-input) {
  width: 240px;
}

.search-form :deep(.custom-select) {
  width: 240px;
}

.search-form :deep(.custom-date-picker) {
  width: 320px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left {
  display: flex;
  gap: 8px;
}

.right {
  display: flex;
  align-items: center;
}

.icon-group {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}

.add-button {
  --el-button-bg-color: #1677ff;
  --el-button-border-color: #1677ff;
  --el-button-hover-bg-color: #4096ff;
  --el-button-hover-border-color: #4096ff;
}

:deep(.icon-only-button) {
  padding: 8px;
  height: 32px;
  width: 32px;
}

:deep(.icon-text-button) {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-button .el-icon) {
  margin-right: 4px;
}

:deep(.icon-only-button .el-icon) {
  margin-right: 0;
}

:deep(.el-button) {
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-button:not(.el-button--primary)) {
  border-color: #d9d9d9;
  &:hover {
    border-color: #4096ff;
    color: #4096ff;
  }
}

:deep(.el-form-item__label) {
  color: rgba(0, 0, 0, 0.85);
  font-weight: normal;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #d9d9d9 inset !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #4096ff inset !important;
}

:deep(.custom-table) {
  margin-top: 0;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.dot.online {
  background-color: #52c41a;
}

.dot.offline {
  background-color: #bfbfbf;
}

.text-success {
  color: #52c41a;
}

.text-secondary {
  color: #8c8c8c;
}
</style>
