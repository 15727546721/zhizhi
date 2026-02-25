<script setup lang="ts">
import {
  getUserPage,
  addUser,
  updateUser,
  deleteUsers,
  getUserInfo,
} from "@/api/user";
import { getRolePage } from "@/api/role";
import { uploadFile } from "@/api/file";

defineOptions({
  name: "User",
  inheritAttrs: false,
});

const statusOptions = ref([
  { label: "禁用", value: 0 },
  { label: "正常", value: 1 },
]);
const userTypeOptions = ref([
  { label: "普通用户", value: 1 },
  { label: "官方账号", value: 2 },
  { label: "管理员", value: 3 },
]);

const uploadPictureHost = ref(
  import.meta.env.VITE_APP_API_URL + "/file/upload"
);
const files = ref();

const queryFormRef = ref(ElForm);
const formRef = ref(ElForm);

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<any>({
  pageNo: 1,
  pageSize: 10,
});

const tableData = ref<any[]>();
const roleList = ref<any[]>();

const dialog = reactive({
  title: "",
  visible: false,
  type: "",
});

const formData = reactive<any>({
  name: "",
  sort: 1,
});

const rules = reactive({
  username: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 1, max: 50, message: "长度在1到50个字符" },
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 1, max: 50, message: "长度在1到50个字符" },
  ],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }],
  roleId: [{ required: true, message: "请选择角色", trigger: "change" }],
});

/** 查询 */
function handleQuery() {
  loading.value = true;
  getUserPage(queryParams)
    .then(({ data }) => {
      // 后端返回格式: { code, info, data: [...] } 列表格式
      if (Array.isArray(data)) {
        tableData.value = data;
        total.value = data.length;
      } else if (data && data.data) {
        tableData.value = data.data;
        total.value = data.total || data.data.length;
      } else {
        tableData.value = [];
        total.value = 0;
      }
    })
    .finally(() => {
      loading.value = false;
    });
}
/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields();
  queryParams.pageNo = 1;
  handleQuery();
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id);
}

/** 打开标签表单弹窗 */
function openDialog(user?: any) {
  dialog.visible = true;
  if (user) {
    dialog.title = "修改用户";
    getUserInfo(user.id).then((res) => {
      Object.assign(formData, res.data);
      dialog.type = "update";
    });
  } else {
    dialog.title = "新增用户";
    dialog.type = "add";
    Object.assign(formData, { status: 1 });
  }
}

/** 保存提交 */
function handleSubmit() {
  formRef.value.validate((valid: any) => {
    if (valid) {
      loading.value = true;
      const userId = formData.id;
      if (userId) {
        updateUser(formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addUser(formData)
          .then(() => {
            ElMessage.success("新增成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      }
    }
  });
}

/** 关闭表单弹窗 */
function closeDialog() {
  dialog.visible = false;
  resetForm();
}

/** 重置表单 */
function resetForm() {
  formRef.value.resetFields();
  formRef.value.clearValidate();
  formData.id = undefined;
}

/** 删除标签 */
function handleDelete(id?: number) {
  const userIds = ref<any>([]);
  if (id) {
    userIds.value.push(id);
  }
  if (ids.value.length) {
    userIds.value = ids.value;
  }
  if (!userIds.value) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteUsers(userIds.value)
      .then(() => {
        ElMessage.success("删除成功");
        resetQuery();
      })
      .finally(() => (loading.value = false));
  });
}

//上传之前的操作
function uploadBefore() {
  loading.value = true;
}

// 上传头像
function uploadSectionFile(param: any) {
  let file = param.file;
  files.value = file;
  uploadFile(files.value).then((res: any) => {
    formData.avatar = res.data;
    loading.value = false;
  });
}

//获取角色列表
function getRoleList() {
  getRolePage({ pageNo: 1, pageSize: 10 }).then((res) => {
    roleList.value = res.data.data;
  });
}

onMounted(() => {
  handleQuery();
  getRoleList();
});

// 获取用户类型标签
function getUserTypeLabel(userType: number) {
  const option = userTypeOptions.value.find((item) => item.value === userType);
  return option ? option.label : "未知";
}

// 获取用户类型标签样式
function getUserTypeTagType(userType: number) {
  switch (userType) {
    case 1:
      return "";
    case 2:
      return "warning";
    case 3:
      return "danger";
    default:
      return "info";
  }
}

// 获取状态标签
function getStatusLabel(status: number) {
  return status === 1 ? "正常" : "禁用";
}

// 获取状态标签样式
function getStatusTagType(status: number) {
  return status === 1 ? "success" : "danger";
}
</script>

<template>
  <div class="app-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form ref="queryFormRef" :model="queryParams" class="search-form" :inline="true">
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select
            v-model="queryParams.userType"
            clearable
            placeholder="全部"
            style="width: 120px"
          >
            <el-option
              v-for="item in userTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            clearable
            placeholder="全部"
            style="width: 100px"
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
            <i-ep-search />查询
          </el-button>
          <el-button @click="resetQuery">
            <i-ep-refresh />重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="divider"></div>

      <!-- 表格区域 -->
      <div class="table-section">
        <div class="operation-bar">
          <div class="left">
            <el-button type="primary" @click="openDialog()" v-hasPerm="['system:user:add']">
              <i-ep-plus />新建
            </el-button>
            <el-button type="danger" :disabled="ids.length === 0" @click="handleDelete()" v-hasPerm="['system:user:delete']">
              <i-ep-delete />批量删除
            </el-button>
          </div>
        </div>

        <el-table
        ref="dataTableRef"
        :data="tableData"
        highlight-current-row
        v-loading="loading"
        stripe
        fit
        max-height="600px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="avatar" align="center" width="80" label="头像">
          <template #default="scope">
            <el-avatar :src="scope.row.avatar" :size="50" />
          </template>
        </el-table-column>
        <el-table-column prop="username" width="120" align="center" label="账号" />
        <el-table-column prop="nickname" width="120" align="center" label="昵称" />
        <el-table-column prop="email" width="180" align="center" label="邮箱" />
        <el-table-column prop="phone" width="130" align="center" label="手机号" />
        <el-table-column align="center" width="100" label="用户类型">
          <template #default="scope">
            <el-tag :type="getUserTypeTagType(scope.row.userType)">
              {{ getUserTypeLabel(scope.row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" width="80" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" width="100" label="帖子数">
          <template #default="scope">
            <span>{{ scope.row.postCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" width="100" label="粉丝数">
          <template #default="scope">
            <span>{{ scope.row.fansCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" align="center" width="160" label="创建时间" />
        <el-table-column prop="lastLoginTime" align="center" width="160" label="最后登录" />

        <!--        <el-table-column align="center" label="状态">
          <template #default="scope">
            <span>{{statusOptions[scope.row.status]}}</span>
          </template>
        </el-table-column>-->
        <el-table-column
          align="center"
          label="操作"
          width="160"
          fixed="right"
        >
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              link
              @click="openDialog(scope.row)"
              icon="Edit"
              v-hasPerm="['system:user:edit']"
              >编辑</el-button
            >
            <el-button
              size="small"
              type="danger"
              link
              @click="handleDelete(scope.row.id)"
              icon="Delete"
              v-hasPerm="['system:user:delete']"
              >删除
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
      </div>
    </div>

    <!-- 用户表单弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="800px"
      @close="closeDialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item prop="avatar" label="头像">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :action="uploadPictureHost"
            :before-upload="uploadBefore"
            :http-request="uploadSectionFile"
          >
            <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username" label="用户名">
          <el-input v-model="formData.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="dialog.type == 'add'" prop="password" label="密码">
          <el-input v-model="formData.password" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="nickname" label="昵称">
          <el-input v-model="formData.nickname" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="email" label="邮箱">
          <el-input v-model="formData.email" autocomplete="off" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item prop="phone" label="手机号">
          <el-input v-model="formData.phone" autocomplete="off" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item prop="userType" label="用户类型">
          <el-radio-group v-model="formData.userType">
            <el-radio
              v-for="item in userTypeOptions"
              :key="item.value"
              :value="item.value"
              border
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="status" label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio
              v-for="item in statusOptions"
              :key="item.value"
              :value="item.value"
              border
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="roleId" label="角色">
          <el-radio-group v-model="formData.roleId">
            <el-radio
              v-for="item in roleList"
              :key="item.id"
              :value="item.id"
              border
            >
              {{ item.name }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
          <el-button @click="closeDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
}
</style>
