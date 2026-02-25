<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item prop="userId" label="用户ID">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item prop="type" label="评论类型">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择评论类型"
            clearable
            style="width: 200px"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="文章评论" :value="1" />
            <el-option label="话题评论" :value="2" />
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
          type="danger"
          :disabled="ids.length === 0"
          v-hasPerm="['system:comment:delete']"
          @click="handleDelete()"
        >
          <i-ep-delete />批量删除
        </el-button>
      </template>

      <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="commentList"
        highlight-current-row
        stripe
        fit
        max-height="600px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" align="center" width="55" />
        <el-table-column prop="avatar" align="center" width="80" label="头像">
          <template #default="scope">
            <el-avatar
              shape="square"
              :size="40"
              :src="scope.row.avatar || '/avatar.jpg'"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="nickname"
          align="center"
          width="120"
          label="评论用户"
        >
          <template #default="scope">
            {{ scope.row.nickname }}({{ scope.row.userId }})
          </template>
        </el-table-column>
        <el-table-column align="center" width="100" label="评论类型">
          <template #default="scope">
            {{ scope.row.type === 1 ? "文章评论" : "话题评论" }}
          </template>
        </el-table-column>
        <el-table-column
          prop="content"
          align="center"
          min-width="200"
          label="内容"
        >
          <template #default="scope">
            <span v-html="scope.row.content" class="comment-content"></span>
          </template>
        </el-table-column>
        <el-table-column width="160" align="center" label="评论时间">
          <template #default="scope">
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          width="180"
          label="操作"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleViewReplies(scope.row)"
              size="small"
            >
              <el-icon><View /></el-icon>查看回复
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              size="small"
            >
              <el-icon><Delete /></el-icon>删除
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

    <el-drawer
      v-model="drawerVisible"
      title="回复列表"
      size="50%"
      :destroy-on-close="true"
    >
      <el-table
        v-loading="repliesLoading"
        :data="repliesList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="avatar" align="center" width="80" label="头像">
          <template #default="scope">
            <el-avatar
              shape="square"
              :size="40"
              :src="scope.row.avatar || '/avatar.jpg'"
            />
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          width="120"
          label="评论用户"
        >
          <template #default="scope">
            {{ scope.row.nickName }}({{ scope.row.userId }})
          </template>
        </el-table-column>
        <el-table-column align="center" width="120" label="回复用户">
          <template #default="scope">
            <template v-if="scope.row.replyUserId">
              {{ scope.row.replyNickname }}({{ scope.row.replyUserId }})
            </template>
            <template v-else>-</template>
          </template>
        </el-table-column>
        <el-table-column prop="content" align="center" min-width="200" label="内容">
          <template #default="scope">
            <span v-html="scope.row.content" class="comment-content"></span>
          </template>
        </el-table-column>
        <el-table-column width="160" align="center" label="评论时间">
          <template #default="scope">
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column align="center" width="100" label="操作" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleDeleteReply(scope.row.id)"
              size="small"
            >
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { getComments, getReplies, deleteComment } from "@/api/message/comment";
import { CommentDTO, CommentQuery, CommentReplyDTO } from "@/api/message/types";
import type { FormInstance } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref, reactive, onMounted, watch } from "vue";
import { useUserStore } from "@/store/modules/user";
import { View, Delete } from '@element-plus/icons-vue'

defineOptions({
  name: "Comment",
  inheritAttrs: false,
});

const userStore = useUserStore();
const queryFormRef = ref<FormInstance>();
const loading = ref(false);
const ids = ref<number[]>([]);
const commentList = ref<CommentDTO[]>([]);
const total = ref(0);

const drawerVisible = ref(false);
const repliesLoading = ref(false);
const repliesList = ref<CommentReplyDTO[]>([]);
const currentParentId = ref<number | null>(null);

const queryParams = reactive<CommentQuery>({
  pageNo: 1,
  pageSize: 10,
  type: undefined,
  userId: undefined,
});

/** 查询列表 */
function handleQuery() {
  loading.value = true;
  const params = {
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize,
    type: queryParams.type,
    userId: queryParams.userId
  };

  getComments(params)
    .then(({ data }) => {
      // 后端返回格式: { code, info, data: { pageNo, pageSize, total, data: [...] } }
      if (data && data.data) {
        commentList.value = data.data;
        total.value = data.total || 0;
      } else {
        commentList.value = [];
        total.value = 0;
      }
    })
    .catch((error) => {
      console.error("获取评论列表失败:", error);
      commentList.value = [];
      total.value = 0;
      ElMessage.error("获取评论列表失败");
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 重置查询 */
function resetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.pageNo = 1;
  handleQuery();
}

/** 行checkbox选中事件 */
function handleSelectionChange(selection: CommentDTO[]) {
  ids.value = selection.map((item) => item.id);
}

/** 删除评论 */
function handleDelete(commentId?: number) {
  const commentIds = commentId ? [commentId] : ids.value;
  if (!commentIds.length) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }

  ElMessageBox.confirm(
    commentId 
      ? "确认删除该评论吗？如果是一级评论，将同时删除其下所有回复" 
      : "确认删除已选中的评论吗？如果包含一级评论，将同时删除其下所有回复",
    "警告",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      loading.value = true;
      return Promise.all(commentIds.map((id) => deleteComment(id)));
    })
    .then(() => {
      ElMessage.success("删除成功");
      handleQuery();
    })
    .catch(() => {
      loading.value = false;
    });
}

/** 查看二级评论 */
function handleViewReplies(row: CommentDTO) {
  drawerVisible.value = true;
  repliesLoading.value = true;
  currentParentId.value = row.id;
  getReplies(row.id, { pageNo: 1, pageSize: 50 })
    .then(({ data }) => {
      repliesList.value = data;
    })
    .catch((error) => {
      console.error("获取回复列表失败:", error);
      ElMessage.error("获取回复列表失败");
    })
    .finally(() => {
      repliesLoading.value = false;
    });
}

/** 删除回复评论 */
function handleDeleteReply(commentId: number) {
  if (!currentParentId.value) {
    ElMessage.error("父评论ID不能为空");
    return;
  }
  
  ElMessageBox.confirm("确认删除该回复评论吗？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      repliesLoading.value = true;
      return deleteComment(commentId);
    })
    .then(() => {
      ElMessage.success("删除成功");
      return getReplies(currentParentId.value!, { pageNo: 1, pageSize: 50 });
    })
    .then(({ data }) => {
      repliesList.value = data;
    })
    .catch((error) => {
      console.error("操作失败:", error);
      ElMessage.error("操作失败");
    })
    .finally(() => {
      repliesLoading.value = false;
    });
}

// 在关闭抽屉时清除当前一级评论ID
watch(drawerVisible, (newVal) => {
  if (!newVal) {
    currentParentId.value = null;
  }
});

onMounted(() => {
  handleQuery();
});
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background: #f5f7f9;
  min-height: calc(100vh - 84px);

  .search-container {
    margin-bottom: 20px;
  }

  .table-container {
    margin-top: 20px;
    :deep(.el-card__header) {
      padding: 15px 20px;
    }

    :deep(.el-table) {
      border-radius: 4px;
      .el-table__header th {
        background-color: var(--el-fill-color-light);
        color: var(--el-text-color-primary);
        font-weight: bold;
      }
    }
  }

  .comment-content {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .operation-buttons {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;

    :deep(.el-button) {
      padding: 4px 8px;
      height: 28px;
    }
  }

  :deep(.el-button) {
    .el-icon {
      margin-right: 4px;
    }
    & + .el-button {
      margin-left: 0;
    }
  }
}
</style>
