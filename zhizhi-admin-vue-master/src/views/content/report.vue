<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-value warning">{{ stats.pending }}</div>
            <div class="stats-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-value success">{{ stats.handled }}</div>
            <div class="stats-label">已处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-value primary">{{ stats.total }}</div>
            <div class="stats-label">总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in StatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.targetType" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in TargetTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-select v-model="queryParams.reason" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in ReasonOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="搜索说明" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 举报列表 -->
    <el-card shadow="never">
      <!-- 批量操作 -->
      <div class="table-toolbar" v-if="selectedIds.length > 0">
        <el-button type="info" @click="handleBatchIgnore">
          批量忽略 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="reportList"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="ID" prop="id" width="70" />
        
        <!-- 举报人 -->
        <el-table-column label="举报人" width="140">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.reporterAvatar" :size="28">
                {{ row.reporterNickname?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ row.reporterNickname }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 举报目标 -->
        <el-table-column label="举报目标" min-width="200">
          <template #default="{ row }">
            <div class="target-info">
              <el-tag :type="getTargetTypeColor(row.targetType)" size="small">
                {{ row.targetTypeName }}
              </el-tag>
              <span class="target-content">{{ row.targetContent }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 被举报人 -->
        <el-table-column label="被举报人" width="140">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.targetUserAvatar" :size="28">
                {{ row.targetUserNickname?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ row.targetUserNickname }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 原因 -->
        <el-table-column label="原因" prop="reasonName" width="100" />

        <!-- 状态 -->
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 时间 -->
        <el-table-column label="举报时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              link 
              @click="handleProcess(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="举报详情" width="600px">
      <el-descriptions :column="2" border v-if="currentReport">
        <el-descriptions-item label="举报ID">{{ currentReport.id }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">{{ formatTime(currentReport.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="举报人">{{ currentReport.reporterNickname }}</el-descriptions-item>
        <el-descriptions-item label="被举报人">{{ currentReport.targetUserNickname }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ currentReport.targetTypeName }}</el-descriptions-item>
        <el-descriptions-item label="举报原因">{{ currentReport.reasonName }}</el-descriptions-item>
        <el-descriptions-item label="目标内容" :span="2">{{ currentReport.targetContent }}</el-descriptions-item>
        <el-descriptions-item label="详细说明" :span="2">{{ currentReport.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentReport.status)">{{ currentReport.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" v-if="currentReport.status > 0">
          {{ currentReport.handleResult || '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 证据图片 -->
      <div v-if="currentReport?.evidenceUrls?.length" class="evidence-section">
        <div class="evidence-title">证据截图</div>
        <el-image
          v-for="(url, index) in currentReport.evidenceUrls"
          :key="index"
          :src="url"
          :preview-src-list="currentReport.evidenceUrls"
          :initial-index="index"
          fit="cover"
          class="evidence-image"
        />
      </div>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="processVisible" title="处理举报" width="500px">
      <el-form :model="processForm" :rules="processRules" ref="processFormRef" label-width="100px">
        <el-form-item label="处理决定" prop="status">
          <el-radio-group v-model="processForm.status">
            <el-radio :label="1">通过（举报属实）</el-radio>
            <el-radio :label="2">驳回（举报不实）</el-radio>
            <el-radio :label="3">忽略</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="处罚措施" v-if="processForm.status === 1">
          <el-select v-model="processForm.handleAction" placeholder="选择处罚措施">
            <el-option
              v-for="item in ActionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.handleResult"
            type="textarea"
            :rows="3"
            placeholder="请输入处理说明（选填）"
            maxlength="500"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="processing" @click="submitProcess">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import { 
  getReportList, 
  getReportDetail, 
  handleReport, 
  batchIgnoreReports, 
  getReportStats 
} from '@/api/report';
import { 
  StatusOptions, 
  TargetTypeOptions, 
  ReasonOptions, 
  ActionOptions,
  type ReportDetail 
} from '@/api/report/types';

// ==================== 响应式数据 ====================

const loading = ref(false);
const reportList = ref<ReportDetail[]>([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);

const stats = reactive({
  total: 0,
  pending: 0,
  handled: 0
});

const queryParams = reactive({
  status: undefined as number | undefined,
  targetType: undefined as number | undefined,
  reason: undefined as number | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
});

// 详情弹窗
const detailVisible = ref(false);
const currentReport = ref<ReportDetail | null>(null);

// 处理弹窗
const processVisible = ref(false);
const processing = ref(false);
const processFormRef = ref();
const processForm = reactive({
  status: 1,
  handleAction: 0,
  handleResult: ''
});
const processRules = {
  status: [{ required: true, message: '请选择处理决定', trigger: 'change' }]
};

// ==================== 生命周期 ====================

onMounted(() => {
  handleQuery();
  loadStats();
});

// ==================== 方法 ====================

// 加载统计
const loadStats = async () => {
  try {
    const res = await getReportStats();
    if (res.data) {
      stats.total = res.data.total || 0;
      stats.pending = res.data.pending || 0;
      stats.handled = res.data.handled || 0;
    }
  } catch (e) {
    console.error('加载统计失败', e);
  }
};

// 查询列表
const handleQuery = async () => {
  loading.value = true;
  try {
    const res = await getReportList(queryParams);
    if (res.data) {
      reportList.value = res.data.data || [];
      total.value = res.data.total || 0;
    }
  } catch (e) {
    console.error('查询失败', e);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  queryParams.pageNo = 1;
  handleQuery();
};

// 重置
const resetQuery = () => {
  queryParams.status = undefined;
  queryParams.targetType = undefined;
  queryParams.reason = undefined;
  queryParams.keyword = '';
  queryParams.pageNo = 1;
  handleQuery();
};

// 选择变化
const handleSelectionChange = (selection: ReportDetail[]) => {
  selectedIds.value = selection.filter(r => r.status === 0).map(r => r.id);
};

// 查看详情
const handleView = async (row: ReportDetail) => {
  try {
    const res = await getReportDetail(row.id);
    currentReport.value = res.data;
    detailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取详情失败');
  }
};

// 处理举报
const handleProcess = (row: ReportDetail) => {
  currentReport.value = row;
  processForm.status = 1;
  processForm.handleAction = 0;
  processForm.handleResult = '';
  processVisible.value = true;
};

// 提交处理
const submitProcess = async () => {
  if (!currentReport.value) return;
  
  try {
    await processFormRef.value.validate();
    processing.value = true;
    
    await handleReport(currentReport.value.id, {
      status: processForm.status,
      handleAction: processForm.status === 1 ? processForm.handleAction : undefined,
      handleResult: processForm.handleResult
    });
    
    ElMessage.success('处理成功');
    processVisible.value = false;
    handleQuery();
    loadStats();
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '处理失败');
    }
  } finally {
    processing.value = false;
  }
};

// 批量忽略
const handleBatchIgnore = async () => {
  if (selectedIds.value.length === 0) return;
  
  try {
    await ElMessageBox.confirm(
      `确定要忽略选中的 ${selectedIds.value.length} 条举报吗？`,
      '批量忽略',
      { type: 'warning' }
    );
    
    await batchIgnoreReports(selectedIds.value);
    ElMessage.success('批量忽略成功');
    handleQuery();
    loadStats();
  } catch (e) {
    // 取消操作
  }
};

// 工具方法
const getStatusColor = (status: number) => {
  const colors: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' };
  return colors[status] || 'info';
};

const getTargetTypeColor = (type: number) => {
  const colors: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning' };
  return colors[type] || 'info';
};

const formatTime = (time: string) => {
  if (!time) return '-';
  return time.replace('T', ' ').substring(0, 19);
};
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  text-align: center;
}

.stats-content {
  padding: 10px 0;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stats-value.warning { color: #e6a23c; }
.stats-value.success { color: #67c23a; }
.stats-value.primary { color: #409eff; }

.stats-label {
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-toolbar {
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.target-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.target-content {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.evidence-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.evidence-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.evidence-image {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  border-radius: 4px;
}
</style>
