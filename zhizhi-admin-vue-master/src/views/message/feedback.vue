<script setup lang="ts">
import {
  getFeedBackPage,
  deleteFeedBack,
  replyFeedback,
  updateFeedbackStatus,
  FeedbackTypeOptions,
  FeedbackStatusOptions,
} from "@/api/message/feedback";

defineOptions({
  name: "FeedbackManage",
  inheritAttrs: false,
});

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryFormRef = ref(ElForm);
const queryParams = reactive<any>({
  pageNo: 1,
  pageSize: 10,
  type: undefined,
  status: undefined,
});

const tableData = ref<any[]>([]);

// 回复弹窗
const replyDialogVisible = ref(false);
const replyLoading = ref(false);
const currentFeedback = ref<any>(null);
const replyContent = ref("");

/** 查询 */
function handleQuery() {
  loading.value = true;
  getFeedBackPage(queryParams)
    .then((res) => {
      // 后端返回: { code, data: { pageNo, pageSize, total, data: [...] } }
      const pageData = res.data;
      tableData.value = pageData?.data || [];
      total.value = pageData?.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNo = 1;
  queryParams.type = undefined;
  queryParams.status = undefined;
  handleQuery();
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id);
}

/** 删除 */
function handleDelete(id?: number) {
  const deleteIds = id ? [id] : ids.value;
  if (!deleteIds.length) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteFeedBack(deleteIds)
      .then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 打开回复弹窗 */
function openReplyDialog(row: any) {
  currentFeedback.value = row;
  replyContent.value = row.reply || "";
  replyDialogVisible.value = true;
}

/** 提交回复 */
function submitReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning("请输入回复内容");
    return;
  }
  replyLoading.value = true;
  replyFeedback({ id: currentFeedback.value.id, reply: replyContent.value })
    .then(() => {
      ElMessage.success("回复成功");
      replyDialogVisible.value = false;
      handleQuery();
    })
    .finally(() => {
      replyLoading.value = false;
    });
}

/** 修改状态 */
function handleStatusChange(id: number, status: number) {
  updateFeedbackStatus({ id, status }).then(() => {
    ElMessage.success("状态更新成功");
    handleQuery();
  });
}

/** 获取类型标签类型 */
function getTypeTagType(type: number): string {
  const types = ["danger", "primary", "warning", "info"];
  return types[type] || "info";
}

/** 获取状态标签类型 */
function getStatusTagType(status: number): string {
  const option = FeedbackStatusOptions.find(o => o.value === status);
  return option?.type || "info";
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="反馈类型">
          <el-select
            v-model="queryParams.type"
            placeholder="全部类型"
            style="width: 150px"
            @change="handleQuery"
            clearable
          >
            <el-option
              v-for="item in FeedbackTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            style="width: 150px"
            @change="handleQuery"
            clearable
          >
            <el-option
              v-for="item in FeedbackStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never" class="table-container">
      <template #header>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete()"
          v-hasPerm="['system:feedback:delete']"
        >
          <i-ep-delete />批量删除
        </el-button>
      </template>

      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="tableData"
        highlight-current-row
        stripe
        fit
        max-height="600px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" align="center" width="50" />
        <el-table-column prop="id" align="center" width="80" label="ID" />
        <el-table-column prop="typeName" align="center" width="100" label="类型">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ scope.row.typeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" align="center" width="120" label="用户">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="24" :src="scope.row.avatar" />
              <span>{{ scope.row.nickname || scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" align="left" min-width="200" label="标题" show-overflow-tooltip />
        <el-table-column prop="content" align="left" min-width="250" label="内容" show-overflow-tooltip />
        <el-table-column prop="contact" align="center" width="120" label="联系方式" show-overflow-tooltip />
        <el-table-column prop="statusName" align="center" width="100" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" width="160" align="center" label="提交时间" />
        <el-table-column align="center" label="操作" width="200" fixed="right">
          <template #default="scope">
            <div class="operation-btns">
              <el-button link type="primary" size="small" @click="openReplyDialog(scope.row)">
                回复
              </el-button>
              <el-dropdown trigger="click" @command="(cmd: number) => handleStatusChange(scope.row.id, cmd)">
                <el-button link type="success" size="small">
                  状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="item in FeedbackStatusOptions"
                      :key="item.value"
                      :command="item.value"
                      :disabled="scope.row.status === item.value"
                    >
                      {{ item.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handleQuery"
      />
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="500px">
      <div v-if="currentFeedback" class="feedback-detail">
        <p><strong>标题：</strong>{{ currentFeedback.title }}</p>
        <p><strong>内容：</strong>{{ currentFeedback.content }}</p>
        <p><strong>联系方式：</strong>{{ currentFeedback.contact || '未填写' }}</p>
      </div>
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="4"
        placeholder="请输入回复内容..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.feedback-detail {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.feedback-detail p {
  margin: 8px 0;
  font-size: 14px;
}

.operation-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
</style>
