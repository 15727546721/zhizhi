<script setup lang="ts">
import {
  getRolePage,
  updateRole,
  addRole,
  deleteRoles,
  getRoleMenuIds,
  updateRoleMenus,
} from "@/api/role";
import { getMenuOptions } from "@/api/menu";

import { RolePageVO, RoleForm, RoleQuery } from "@/api/role/types";

defineOptions({
  name: "Role",
  inheritAttrs: false,
});

const queryFormRef = ref(ElForm);
const roleFormRef = ref(ElForm);
const menuRef = ref(ElTree);

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<RoleQuery>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
});

const roleList = ref<RolePageVO[]>([]);

const dialog = reactive({
  title: "",
  visible: false,
});

const formData = reactive<RoleForm>({
  code: "",
  name: "",
  remark: "",
});

const rules = reactive({
  name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
  code: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
});

const menuDialogVisible = ref(false);

const menuList = ref<OptionType[]>([]);

interface CheckedRole {
  id?: number;
  name?: string;
}
let checkedRole: CheckedRole = reactive({});

/** 查询 */
function handleQuery() {
  loading.value = true;
  getRolePage(queryParams)
    .then(({ data }) => {
      // 后端返回格式: { code, info, data: { pageNo, pageSize, total, data: [...] } }
      if (data && data.data) {
        roleList.value = data.data;
        total.value = data.total || 0;
        queryParams.pageNo = data.pageNo || 1;
        queryParams.pageSize = data.pageSize || 10;
      } else if (Array.isArray(data)) {
        roleList.value = data;
        total.value = data.length;
      } else {
        roleList.value = [];
        total.value = 0;
      }
    })
    .catch((error) => {
      console.error("获取角色列表失败:", error);
      ElMessage.error("获取角色列表失败");
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

/** 处理分页变化 */
function handlePageChange(page: number) {
  queryParams.pageNo = page;
  handleQuery();
}

/** 处理每页条数变化 */
function handleSizeChange(size: number) {
  queryParams.pageSize = size;
  queryParams.pageNo = 1;
  handleQuery();
}

/** 行checkbox 选中事件 */
function handleSelectionChange(selection: any) {
  ids.value = selection.map((item: any) => item.id);
}

/** 打开角色表单弹窗 */
function openDialog(role?: Object) {
  dialog.visible = true;
  if (role) {
    dialog.title = "修改角色";
    Object.assign(formData, role);
  } else {
    dialog.title = "新增角色";
  }
}

/** 角色保存提交 */
function handleSubmit() {
  roleFormRef.value.validate((valid: any) => {
    if (valid) {
      loading.value = true;
      const roleId = formData.id;
      if (roleId) {
        updateRole(formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            queryParams.pageNo = 1;
            handleQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addRole(formData)
          .then(() => {
            ElMessage.success("新增成功");
            closeDialog();
            queryParams.pageNo = 1;
            handleQuery();
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
  roleFormRef.value.resetFields();
  roleFormRef.value.clearValidate();
  formData.id = undefined;
  formData.name = "";
  formData.code = "";
  formData.remark = "";
}

/** 删除角色 */
function handleDelete(roleId?: number) {
  const roleIds = ref<any>([]);
  if (roleId) {
    roleIds.value.push(roleId);
  }
  if (ids.value.length) {
    roleIds.value = ids.value;
  }
  if (!roleIds.value) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteRoles(roleIds.value)
      .then(() => {
        ElMessage.success("删除成功");
        queryParams.pageNo = 1;
        handleQuery();
      })
      .finally(() => (loading.value = false));
  });
}

/** 打开分配菜单弹窗 */
function openMenuDialog(row: RolePageVO) {
  const roleId = row.id;
  if (roleId) {
    checkedRole = {
      id: roleId,
      name: row.name,
    };
    menuDialogVisible.value = true;
    loading.value = true;

    // 获取所有的菜单
    getMenuOptions().then((response) => {
      menuList.value = response.data;
      // 回显角色已拥有的菜单
      getRoleMenuIds(roleId)
        .then(({ data }) => {
          const checkedMenuIds = data;
          checkedMenuIds.forEach((menuId) =>
            menuRef.value!.setChecked(menuId, true, false)
          );
        })
        .finally(() => {
          loading.value = false;
        });
    });
  }
}

/** 角色分配菜单保存提交 */
function handleRoleMenuSubmit() {
  const roleId = checkedRole.id;
  if (roleId) {
    const checkedMenuIds: number[] = menuRef.value
      .getCheckedNodes(false, true)
      .map((node: any) => node.id);
    loading.value = true;
    updateRoleMenus(roleId, checkedMenuIds)
      .then(() => {
        ElMessage.success("分配权限成功");
        menuDialogVisible.value = false;
        queryParams.pageNo = 1;
        handleQuery();
      })
      .finally(() => {
        loading.value = false;
      });
  }
}

/** 处理分页事件 */
function handlePaginationChange(pagination: { page: number; limit: number }) {
  queryParams.pageNo = pagination.page;
  queryParams.pageSize = pagination.limit;
  handleQuery();
}

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form ref="queryFormRef" :model="queryParams" class="search-form" :inline="true">
        <el-form-item prop="name" label="角色名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入角色名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
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
            <el-button type="primary" @click="openDialog()" v-hasPerm="['system:role:add']">
              <i-ep-plus />新建
            </el-button>
            <el-button type="danger" :disabled="!ids.length" @click="handleDelete()" v-hasPerm="['system:role:delete']">
              <i-ep-delete />删除
            </el-button>
          </div>
        </div>

        <el-table
        v-loading="loading"
        :data="roleList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          label="角色编号"
          prop="id"
          width="100"
          align="center"
        />
        <el-table-column
          label="角色编码"
          prop="code"
          width="120"
          align="center"
        />
        <el-table-column
          label="角色名称"
          prop="name"
          width="120"
          align="center"
        />
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
        <el-table-column label="创建时间" align="center" width="180">
          <template #default="scope">
            <span>{{ scope.row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" width="180">
          <template #default="scope">
            <span>{{ scope.row.updateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button
                type="primary"
                link
                size="small"
                @click="openDialog(scope.row)"
                v-hasPerm="['system:role:update']"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                type="success"
                link
                size="small"
                @click="openMenuDialog(scope.row)"
                v-hasPerm="['system:role:assign']"
              >
                <el-icon><Menu /></el-icon>
                分配
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleDelete(scope.row.id)"
                v-hasPerm="['system:role:delete']"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <pagination
          v-if="total > 0"
          :total="total"
          :page="queryParams.pageNo"
          :limit="queryParams.pageSize"
          @pagination="handlePaginationChange"
        />
      </div>
    </div>

    <!-- 角色表单弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
      @close="closeDialog"
    >
      <el-form
        ref="roleFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="角色编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入角色编码" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleSubmit">确 定</el-button>
          <el-button @click="closeDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗  -->
    <el-dialog
      v-model="menuDialogVisible"
      :title="'【' + checkedRole.name + '】权限分配'"
      width="800px"
    >
      <el-scrollbar v-loading="loading" max-height="600px">
        <el-tree
          ref="menuRef"
          node-key="id"
          show-checkbox
          :data="menuList"
          :default-expand-all="false"
        />
      </el-scrollbar>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleRoleMenuSubmit"
            >确 定</el-button
          >
          <el-button @click="menuDialogVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
