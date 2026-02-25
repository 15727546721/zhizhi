<script setup lang="ts">
import { getMessagePage, deleteMessage, sendNotification, searchUsersForMessage } from "@/api/message/message";

defineOptions({
  name: "SystemMessage",
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
  isRead: undefined,
});

// 发送公告对话框
const sendDialogVisible = ref(false);
const sendLoading = ref(false);
const sendFormRef = ref();
const sendForm = reactive({
  title: "",
  content: "",
  sendType: "broadcast", // broadcast=群发, specific=指定用户
  selectedUsers: [] as number[], // 选中的用户ID数组
});
const sendRules = {
  title: [{ required: true, message: "请输入公告标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入公告内容", trigger: "blur" }],
};

// 用户搜索
const userOptions = ref<any[]>([]);
const userSearchLoading = ref(false);

/** 远程搜索用户 */
function handleUserSearch(query: string) {
  if (query.length < 1) {
    userOptions.value = [];
    return;
  }
  userSearchLoading.value = true;
  searchUsersForMessage(query)
    .then(({ data }) => {
      if (data) {
        userOptions.value = data.map((user: any) => ({
          value: user.id,
          label: `${user.nickname || user.username} (ID: ${user.id})`,
          username: user.username,
          nickname: user.nickname,
          avatar: user.avatar,
        }));
      } else {
        userOptions.value = [];
      }
    })
    .catch(() => {
      userOptions.value = [];
    })
    .finally(() => {
      userSearchLoading.value = false;
    });
}

const tableData = ref<any[]>([]);

// 消息类型选项
const typeOptions = [
  { label: "全部", value: undefined },
  { label: "系统通知", value: 0 },
  { label: "点赞", value: 1 },
  { label: "收藏", value: 2 },
  { label: "评论", value: 3 },
  { label: "回复", value: 4 },
  { label: "关注", value: 5 },
  { label: "@提及", value: 6 },
];

// 已读状态选项
const readOptions = [
  { label: "全部", value: undefined },
  { label: "未读", value: 0 },
  { label: "已读", value: 1 },
];

/** 获取类型名称 */
function getTypeName(type: number | null): string {
  if (type === null || type === undefined) return "未知";
  const option = typeOptions.find(o => o.value === type);
  return option ? option.label : "其他";
}

/** 查询 */
function handleQuery() {
  loading.value = true;
  getMessagePage(queryParams)
    .then(({ data }) => {
      // 后端返回格式: { code, info, data: { pageNo, pageSize, total, data: [...] } }
      if (data && data.data) {
        tableData.value = data.data;
        total.value = data.total || 0;
      } else {
        tableData.value = [];
        total.value = 0;
      }
    })
    .catch((error) => {
      console.error("获取消息列表失败:", error);
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
  queryParams.type = undefined;
  queryParams.isRead = undefined;
  handleQuery();
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id);
}

/** 删除消息 */
function handleDelete(id?: number) {
  const deleteIds = id ? [id] : ids.value;
  if (!deleteIds.length) {
    ElMessage.warning("请勾选删除项");
    return;
  }

  ElMessageBox.confirm("确认删除已选中的消息?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteMessage(deleteIds)
      .then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 打开发送公告对话框 */
function openSendDialog() {
  sendForm.title = "";
  sendForm.content = "";
  sendForm.sendType = "broadcast";
  sendForm.selectedUsers = [];
  userOptions.value = [];
  sendDialogVisible.value = true;
}

/** 发送公告 */
async function handleSend() {
  if (!sendFormRef.value) return;
  
  try {
    await sendFormRef.value.validate();
    sendLoading.value = true;
    
    // 构建请求数据
    const data: any = {
      title: sendForm.title,
      content: sendForm.content,
    };
    
    // 如果是指定用户发送
    if (sendForm.sendType === "specific" && sendForm.selectedUsers.length > 0) {
      data.receiverIds = sendForm.selectedUsers;
    }
    
    await sendNotification(data);
    ElMessage.success(sendForm.sendType === "broadcast" ? "群发公告成功" : "发送成功");
    sendDialogVisible.value = false;
    handleQuery();
  } catch (error) {
    console.error("发送失败:", error);
  } finally {
    sendLoading.value = false;
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
        <el-form-item prop="type" label="消息类型">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择消息类型"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item prop="isRead" label="已读状态">
          <el-select
            v-model="queryParams.isRead"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="item in readOptions"
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
          @click="openSendDialog"
          v-hasPerm="['system:message:send']"
        >
          <i-ep-promotion />发送公告
        </el-button>
        <el-button
          type="danger"
          :disabled="ids.length === 0"
          @click="handleDelete()"
          v-hasPerm="['system:message:delete']"
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
        <el-table-column align="center" width="100" label="类型">
          <template #default="scope">
            <el-tag :type="scope.row.type === 0 ? 'danger' : 'primary'" size="small">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="title"
          align="center"
          width="150"
          label="标题"
          show-overflow-tooltip
        />
        <el-table-column
          prop="content"
          align="center"
          min-width="200"
          label="内容"
          show-overflow-tooltip
        />
        <el-table-column align="center" width="120" label="接收者">
          <template #default="scope">
            <el-tag v-if="!scope.row.receiverId" type="warning" size="small">全部用户</el-tag>
            <span v-else>{{ scope.row.receiverId }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" label="已读">
          <template #default="scope">
            <el-tag :type="scope.row.isRead === 1 ? 'success' : 'warning'" size="small">
              {{ scope.row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" align="center" width="160" label="创建时间" />
        <el-table-column
          align="center"
          label="操作"
          width="100"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              size="small"
              v-hasPerm="['system:message:delete']"
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

    <!-- 发送公告对话框 -->
    <el-dialog v-model="sendDialogVisible" title="发送公告" width="500px">
      <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="100px">
        <el-form-item label="发送类型">
          <el-radio-group v-model="sendForm.sendType">
            <el-radio value="broadcast">群发公告</el-radio>
            <el-radio value="specific">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="sendForm.sendType === 'specific'" label="选择用户">
          <el-select
            v-model="sendForm.selectedUsers"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="输入用户名搜索"
            :remote-method="handleUserSearch"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="handleSend">
          {{ sendForm.sendType === 'broadcast' ? '群发' : '发送' }}
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
