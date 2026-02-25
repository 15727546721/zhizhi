<script setup lang="ts">
import {
  getAnnouncementPage,
  addAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement,
  unpublishAnnouncement,
  toggleTopAnnouncement,
  type AnnouncementVO,
  type AnnouncementForm,
} from "@/api/announcement";

defineOptions({
  name: "AnnouncementManage",
  inheritAttrs: false,
});

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryFormRef = ref(ElForm);
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: "",
  type: undefined as number | undefined,
  status: undefined as number | undefined,
});

const tableData = ref<AnnouncementVO[]>([]);

// 公告类型选项
const typeOptions = [
  { label: "普通", value: 0 },
  { label: "活动", value: 1 },
  { label: "系统", value: 2 },
  { label: "更新", value: 3 },
];

// 状态选项
const statusOptions = [
  { label: "草稿", value: 0 },
  { label: "已发布", value: 1 },
  { label: "已下架", value: 2 },
];

// 编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref("");
const formLoading = ref(false);
const formRef = ref();
const formData = reactive<AnnouncementForm>({
  id: undefined,
  title: "",
  content: "",
  type: 0,
  status: 0,
  isTop: 0,
});
const formRules = {
  title: [{ required: true, message: "请输入公告标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入公告内容", trigger: "blur" }],
};

/** 获取类型标签样式 */
function getTypeTagType(type: number): string {
  const types: Record<number, string> = { 0: "info", 1: "warning", 2: "danger", 3: "success" };
  return types[type] || "info";
}

/** 获取类型名称 */
function getTypeName(type: number): string {
  const option = typeOptions.find(o => o.value === type);
  return option ? option.label : "未知";
}

/** 获取状态标签样式 */
function getStatusTagType(status: number): string {
  const types: Record<number, string> = { 0: "info", 1: "success", 2: "warning" };
  return types[status] || "info";
}

/** 获取状态名称 */
function getStatusName(status: number): string {
  const option = statusOptions.find(o => o.value === status);
  return option ? option.label : "未知";
}

/** 查询 */
function handleQuery() {
  loading.value = true;
  getAnnouncementPage(queryParams)
    .then(({ data }) => {
      if (data && data.data) {
        tableData.value = data.data;
        total.value = data.total || 0;
      } else {
        tableData.value = [];
        total.value = 0;
      }
    })
    .catch((error) => {
      console.error("获取公告列表失败:", error);
      tableData.value = [];
      total.value = 0;
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNo = 1;
  queryParams.title = "";
  queryParams.type = undefined;
  queryParams.status = undefined;
  handleQuery();
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: AnnouncementVO[]) {
  ids.value = selection.map((item) => item.id);
}

/** 打开新增对话框 */
function openAddDialog() {
  dialogTitle.value = "新增公告";
  formData.id = undefined;
  formData.title = "";
  formData.content = "";
  formData.type = 0;
  formData.status = 0;
  formData.isTop = 0;
  dialogVisible.value = true;
}

/** 打开编辑对话框 */
function openEditDialog(row: AnnouncementVO) {
  dialogTitle.value = "编辑公告";
  formData.id = row.id;
  formData.title = row.title;
  formData.content = row.content;
  formData.type = row.type;
  formData.status = row.status;
  formData.isTop = row.isTop;
  dialogVisible.value = true;
}

/** 提交表单 */
async function handleSubmit() {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    formLoading.value = true;
    
    if (formData.id) {
      await updateAnnouncement(formData);
      ElMessage.success("更新成功");
    } else {
      await addAnnouncement(formData);
      ElMessage.success("新增成功");
    }
    dialogVisible.value = false;
    handleQuery();
  } catch (error) {
    console.error("提交失败:", error);
  } finally {
    formLoading.value = false;
  }
}

/** 删除公告 */
function handleDelete(id?: number) {
  const deleteIds = id ? [id] : ids.value;
  if (!deleteIds.length) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的公告?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteAnnouncement(deleteIds)
      .then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 发布公告 */
function handlePublish(row: AnnouncementVO) {
  ElMessageBox.confirm(`确认发布公告"${row.title}"?`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "info",
  }).then(() => {
    publishAnnouncement(row.id).then(() => {
      ElMessage.success("发布成功");
      handleQuery();
    });
  });
}

/** 下架公告 */
function handleUnpublish(row: AnnouncementVO) {
  ElMessageBox.confirm(`确认下架公告"${row.title}"?`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    unpublishAnnouncement(row.id).then(() => {
      ElMessage.success("下架成功");
      handleQuery();
    });
  });
}

/** 切换置顶状态 */
function handleToggleTop(row: AnnouncementVO) {
  const newTop = row.isTop === 1 ? 0 : 1;
  const action = newTop === 1 ? "置顶" : "取消置顶";
  toggleTopAnnouncement(row.id, newTop).then(() => {
    ElMessage.success(`${action}成功`);
    handleQuery();
  });
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="title" label="标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入公告标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="type" label="类型">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择类型"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item prop="status" label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <i-ep-search />搜索
          </el-button>
          <el-button @click="resetQuery"><i-ep-refresh />重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never" class="table-container">
      <template #header>
        <el-button
          type="primary"
          @click="openAddDialog"
          v-hasPerm="['system:announcement:add']"
        >
          <i-ep-plus />新增
        </el-button>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete()"
          v-hasPerm="['system:announcement:delete']"
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
        <el-table-column type="selection" align="center" width="55" />
        <el-table-column prop="id" align="center" width="80" label="ID" />
        <el-table-column
          prop="title"
          align="center"
          min-width="200"
          label="标题"
          show-overflow-tooltip
        />
        <el-table-column align="center" width="80" label="类型">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)" size="small">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" label="置顶">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.isTop === 1"
              @change="handleToggleTop(scope.row)"
              v-hasPerm="['system:announcement:edit']"
            />
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" align="center" width="160" label="发布时间" />
        <el-table-column prop="createTime" align="center" width="160" label="创建时间" />
        <el-table-column
          align="center"
          label="操作"
          width="200"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              link
              type="success"
              @click="handlePublish(scope.row)"
              size="small"
              v-hasPerm="['system:announcement:edit']"
            >
              发布
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              link
              type="warning"
              @click="handleUnpublish(scope.row)"
              size="small"
              v-hasPerm="['system:announcement:edit']"
            >
              下架
            </el-button>
            <el-button
              link
              type="primary"
              @click="openEditDialog(scope.row)"
              size="small"
              v-hasPerm="['system:announcement:edit']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              size="small"
              v-hasPerm="['system:announcement:delete']"
            >
              删除
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶" prop="isTop">
          <el-switch
            v-model="formData.isTop"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.app-container {
  padding: 20px;

  .search-container {
    margin-bottom: 20px;
  }

  .table-container {
    :deep(.el-card__header) {
      padding: 15px 20px;
    }
  }
}
</style>
