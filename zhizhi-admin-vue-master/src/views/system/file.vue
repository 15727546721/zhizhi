<script setup lang="ts">
import { getFileList, batchDeleteFiles, upload, type FileVO } from "@/api/file";

defineOptions({
  name: "FileManagement",
  inheritAttrs: false,
});

const loading = ref(false);
const uploading = ref(false);
const selectedUrls = ref<string[]>([]);
const total = ref(0);
const previewVisible = ref(false);
const previewUrl = ref("");

const queryFormRef = ref(ElForm);
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  prefix: "",
});

const tableData = ref<FileVO[]>([]);

// 文件类型选项
const fileTypeOptions = [
  { label: "全部", value: "" },
  { label: "图片", value: "image" },
  { label: "视频", value: "video" },
  { label: "文档", value: "document" },
  { label: "压缩包", value: "archive" },
];

/** 查询 */
function handleQuery() {
  loading.value = true;
  getFileList(queryParams)
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
      console.error("获取文件列表失败:", error);
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
  queryParams.prefix = "";
  handleQuery();
}

/** 行选中事件 */
function handleSelectionChange(selection: FileVO[]) {
  selectedUrls.value = selection.map((item) => item.fileUrl);
}

/** 预览图片 */
function handlePreview(row: FileVO) {
  if (row.fileType === "image") {
    previewUrl.value = row.fileUrl;
    previewVisible.value = true;
  } else {
    // 非图片直接打开链接
    window.open(row.fileUrl, "_blank");
  }
}

/** 复制链接 */
function handleCopyUrl(url: string) {
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success("链接已复制到剪贴板");
  }).catch(() => {
    ElMessage.error("复制失败");
  });
}

/** 删除文件 */
function handleDelete(url?: string) {
  const deleteUrls = url ? [url] : selectedUrls.value;
  if (!deleteUrls.length) {
    ElMessage.warning("请选择要删除的文件");
    return;
  }

  ElMessageBox.confirm(`确认删除选中的 ${deleteUrls.length} 个文件?`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    batchDeleteFiles(deleteUrls)
      .then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 上传前检查 */
function beforeUpload(file: File) {
  const isLt100M = file.size / 1024 / 1024 < 100;
  if (!isLt100M) {
    ElMessage.error("文件大小不能超过 100MB!");
    return false;
  }
  return true;
}

/** 上传文件 */
function handleUpload(options: any) {
  const formData = new FormData();
  formData.append("files", options.file);
  
  uploading.value = true;
  upload(formData)
    .then(() => {
      ElMessage.success("上传成功");
      handleQuery();
    })
    .catch((error) => {
      console.error("上传失败:", error);
      ElMessage.error("上传失败");
    })
    .finally(() => {
      uploading.value = false;
    });
}

/** 获取文件类型图标 */
function getFileIcon(fileType: string) {
  switch (fileType) {
    case "image":
      return "Picture";
    case "video":
      return "VideoCamera";
    case "document":
      return "Document";
    case "archive":
      return "FolderOpened";
    default:
      return "Files";
  }
}

/** 获取文件类型标签样式 */
function getFileTypeTag(fileType: string) {
  switch (fileType) {
    case "image":
      return "success";
    case "video":
      return "warning";
    case "document":
      return "primary";
    case "archive":
      return "info";
    default:
      return "";
  }
}

/** 获取文件类型名称 */
function getFileTypeName(fileType: string) {
  switch (fileType) {
    case "image":
      return "图片";
    case "video":
      return "视频";
    case "document":
      return "文档";
    case "archive":
      return "压缩包";
    default:
      return "其他";
  }
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="prefix" label="文件名">
          <el-input
            v-model="queryParams.prefix"
            placeholder="输入文件名前缀搜索"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
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
        <div class="header-actions">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            v-hasPerm="['system:file:upload']"
          >
            <el-button type="primary" :loading="uploading">
              <i-ep-upload />上传文件
            </el-button>
          </el-upload>
          <el-button
            type="danger"
            :disabled="selectedUrls.length === 0"
            @click="handleDelete()"
            v-hasPerm="['system:file:delete']"
          >
            <i-ep-delete />批量删除
          </el-button>
        </div>
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
        
        <el-table-column label="预览" align="center" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.fileType === 'image'"
              :src="scope.row.fileUrl"
              :preview-src-list="[scope.row.fileUrl]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer;"
            />
            <el-icon v-else :size="40" color="#909399">
              <component :is="getFileIcon(scope.row.fileType)" />
            </el-icon>
          </template>
        </el-table-column>

        <el-table-column
          prop="fileName"
          label="文件名"
          min-width="200"
          show-overflow-tooltip
        />

        <el-table-column label="类型" align="center" width="100">
          <template #default="scope">
            <el-tag :type="getFileTypeTag(scope.row.fileType)" size="small">
              {{ getFileTypeName(scope.row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="fileSizeStr"
          label="大小"
          align="center"
          width="100"
        />

        <el-table-column
          prop="lastModified"
          label="上传时间"
          align="center"
          width="160"
        />

        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              @click="handlePreview(scope.row)"
            >
              <i-ep-view />预览
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleCopyUrl(scope.row.fileUrl)"
            >
              <i-ep-link />复制
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(scope.row.fileUrl)"
              v-hasPerm="['system:file:delete']"
            >
              <i-ep-delete />删除
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

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="60%">
      <div style="text-align: center;">
        <el-image :src="previewUrl" fit="contain" style="max-height: 70vh;" />
      </div>
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
    .header-actions {
      display: flex;
      gap: 10px;
    }

    :deep(.el-card__header) {
      padding: 15px 20px;
    }
  }
}
</style>
