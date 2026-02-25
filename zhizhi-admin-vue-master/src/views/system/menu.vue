<template>
  <div class="app-container">
    <el-card shadow="never" class="table-container">
      <template #header>
        <el-button
          type="success"
          @click="openDialog(0)"
          v-hasPerm="['system:menu:add']"
        >
          <template #icon><i-ep-plus /></template>
          新增</el-button
        >
      </template>

      <el-table
        v-loading="loading"
        :data="menuList"
        highlight-current-row
        row-key="id"
        :expand-row-keys="['1']"
        @row-click="onRowClick"
        :tree-props="{
          children: 'children',
          hasChildren: 'hasChildren',
        }"
      >
        <el-table-column label="菜单名称" min-width="150">
          <template #default="scope">
            <svg-icon :icon-class="scope.row.icon" />
            {{ scope.row.title }}
          </template>
        </el-table-column>

        <el-table-column label="类型" align="center" width="80">
          <template #default="scope">
            <el-tag
              v-if="scope.row.type === MenuTypeEnum.CATALOG"
              type="warning"
              >目录</el-tag
            >
            <el-tag v-if="scope.row.type === MenuTypeEnum.MENU" type="success"
              >菜单</el-tag
            >
            <el-tag v-if="scope.row.type === MenuTypeEnum.BUTTON" type="danger"
              >按钮</el-tag
            >
            <el-tag v-if="scope.row.type === MenuTypeEnum.EXTLINK" type="info"
              >外链</el-tag
            >
          </template>
        </el-table-column>

        <el-table-column
          label="路由路径"
          align="left"
          width="150"
          prop="path"
        />

        <el-table-column
          label="组件路径"
          align="left"
          width="250"
          prop="component"
        />

        <el-table-column
          label="权限标识"
          align="center"
          width="200"
          prop="perm"
        />

        <el-table-column label="状态" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.hidden === 1" type="success">显示</el-tag>
            <el-tag v-else type="info">隐藏</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="排序" align="center" width="80" prop="sort" />

        <el-table-column fixed="right" align="center" label="操作" width="220">
          <template #default="scope">
            <el-button
              v-if="scope.row.type == 'CATALOG' || scope.row.type == 'MENU'"
              type="success"
              link
              size="small"
              @click.stop="openDialog(scope.row.id)"
              v-hasPerm="['system:menu:add']"
            >
              <i-ep-plus />新增
            </el-button>

            <el-button
              type="primary"
              link
              size="small"
              @click.stop="openDialog(undefined, scope.row.id)"
              v-hasPerm="['system:menu:update']"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click.stop="handleDelete(scope.row.id)"
              v-hasPerm="['system:menu:delete']"
              ><i-ep-delete />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      destroy-on-close
      append-to-body
      width="1000px"
      @close="closeDialog"
      top="5vh"
    >
      <el-form
        ref="menuFormRef"
        :model="formData"
        :rules="rules"
        label-width="160px"
      >
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            placeholder="选择上级菜单"
            :data="menuOptions"
            filterable
            check-strictly
            :render-after-expand="false"
            :filter-node-method="filterNodeMethod"
            style="width: 100%"
            node-key="value"
            :props="{
              value: 'value',
              label: 'label',
              children: 'children'
            }"
          >
            <template #default="{ data }">
              <span>{{ data.label }}</span>
            </template>
          </el-tree-select>
        </el-form-item>

        <el-form-item label="菜单名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="formData.type" @change="onMenuTypeChange">
            <el-radio value="CATALOG">目录</el-radio>
            <el-radio value="MENU">菜单</el-radio>
            <el-radio value="BUTTON">按钮</el-radio>
            <el-radio value="EXTLINK">外链</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="formData.type == 'EXTLINK'"
          label="外链地址"
          prop="path"
        >
          <el-input v-model="formData.path" placeholder="请输入外链完整路径" />
        </el-form-item>

        <el-form-item
          v-if="
            formData.type == MenuTypeEnum.CATALOG ||
            formData.type == MenuTypeEnum.MENU
          "
          label="路由路径"
          prop="path"
        >
          <el-input
            v-if="formData.type == MenuTypeEnum.CATALOG"
            v-model="formData.path"
            placeholder="system"
          />
          <el-input v-else v-model="formData.path" placeholder="user" />
        </el-form-item>

        <!-- 组件页面完整路径 -->
        <el-form-item
          v-if="formData.type == MenuTypeEnum.MENU"
          label="页面路径"
          prop="component"
        >
          <el-input
            v-model="formData.component"
            placeholder="system/user/index"
            style="width: 95%"
          >
            <template v-if="formData.type == MenuTypeEnum.MENU" #prepend
              >src/views</template
            >
            <template v-if="formData.type == MenuTypeEnum.MENU" #append
              >.vue</template
            >
          </el-input>
        </el-form-item>

        <el-form-item
          v-if="formData.type !== MenuTypeEnum.BUTTON"
          label="图标"
          prop="icon"
        >
          <!-- 图标选择器 -->
          <icon-select v-model="formData.icon" />
        </el-form-item>

        <el-form-item
          v-if="formData.type !== MenuTypeEnum.BUTTON"
          prop="hidden"
          label="显示状态"
        >
          <el-radio-group v-model="formData.hidden">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            style="width: 100px"
            controls-position="right"
            :min="0"
          />
        </el-form-item>

        <!-- 权限标识 -->
        <el-form-item
          v-if="formData.type == MenuTypeEnum.BUTTON"
          label="权限标识"
          prop="perm"
        >
          <el-input v-model="formData.perm" placeholder="sys:user:add" />
        </el-form-item>

        <el-form-item
          v-if="formData.type == MenuTypeEnum.CATALOG"
          label="跳转路由"
        >
          <el-input v-model="formData.redirect" placeholder="跳转路由" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="closeDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: "Menu",
  inheritAttrs: false,
});

import {
  listMenus,
  getMenuForm,
  getMenuOptions,
  addMenu,
  deleteMenu,
  updateMenu,
} from "@/api/menu";

import { MenuTypeEnum } from "@/enums/MenuTypeEnum";

const menuFormRef = ref(ElForm);

const loading = ref(false);
const dialog = reactive({
  title: "",
  visible: false,
});

const menuList = ref<any[]>([]);

const menuOptions = ref<any[]>([]);

// 添加过滤节点方法
const filterNodeMethod = (value: string, data: any) => {
  if (!value) return true;
  return data.label.includes(value);
};

const formData = reactive<any>({
  parentId: 0,
  hidden: 0,
  sort: 1,
  type: MenuTypeEnum.MENU,
});

const rules = reactive({
  parentId: [{ required: true, message: "请选择顶级菜单", trigger: "blur" }],
  name: [{ required: true, message: "请输入菜单名称", trigger: "blur" }],
  type: [{ required: true, message: "请选择菜单类型", trigger: "blur" }],
  path: [{ required: true, message: "请输入路由路径", trigger: "blur" }],

  component: [{ required: true, message: "请输入组件路径", trigger: "blur" }],
  hidden: [{ required: true, message: "请输入选择显示状态", trigger: "blur" }],
});

// 选择表格的行菜单ID
const selectedRowMenuId = ref<number | undefined>();

const menuCacheData = reactive({
  type: "",
  path: "",
  component: "",
});

/**
 * 查询
 */
function handleQuery() {
  // 重置父组件
  loading.value = true;
  listMenus()
    .then(({ data }) => {
      menuList.value = data;
    })
    .then(() => {
      loading.value = false;
    });
}

/**行点击事件 */
function onRowClick(row: any) {
  selectedRowMenuId.value = row.id;
}

/**
 * 递归处理菜单数据，确保每个节点都有 value 属性
 */
const handleMenuData = (data: any[]): any[] => {
  return data.map(item => ({
    ...item,
    value: item.id,
    label: item.title || item.label,
    children: item.children ? handleMenuData(item.children) : []
  }));
};

/**
 * 打开表单弹窗
 */
function openDialog(parentId?: number, menuId?: number) {
  getMenuOptions()
    .then(({ data }) => {
      // 处理菜单数据，确保每个节点都有 value 属性
      const processedData = handleMenuData(data);
      // 构造菜单选择数据
      menuOptions.value = [
        {
          value: 0,
          label: "顶级菜单",
          children: processedData
        }
      ];
    })
    .then(() => {
      dialog.visible = true;
      if (menuId) {
        dialog.title = "编辑菜单";
        getMenuForm(menuId).then(({ data }) => {
          Object.assign(formData, data);
          menuCacheData.type = data.type;
          menuCacheData.path = data.path ?? "";
          menuCacheData.component = data.component ?? "";
        });
      } else {
        dialog.title = "新增菜单";
        formData.parentId = parentId;
        formData.component = "";
        formData.path = "";
      }
    });
}

/** 菜单类型切换事件处理 */
function onMenuTypeChange() {
  // 如果菜单类型改变，清空路由路径；未改变在切换后还原路由路径
  if (formData.type !== menuCacheData.type) {
    formData.path = "";
  } else {
    formData.path = menuCacheData.path;
  }
}

/** 菜单保存提交 */
function submitForm() {
  menuFormRef.value.validate((isValid: boolean) => {
    if (isValid) {
      const menuId = formData.id;
      
      // 构造提交数据，只包含后端支持的字段
      const submitData: any = {
        id: formData.id,
        parentId: formData.parentId,
        title: formData.title,
        type: formData.type,
        path: formData.path,
        component: formData.component,
        icon: formData.icon,
        hidden: formData.hidden,
        sort: formData.sort,
        perm: formData.perm,
        redirect: formData.redirect,
        name: formData.name,
      };
      
      // 对于CATALOG类型，确保path以/开头（但不重复添加）
      if (submitData.type === MenuTypeEnum.CATALOG && submitData.path) {
        if (!submitData.path.startsWith('/')) {
          submitData.path = '/' + submitData.path;
        }
      }
      
      if (menuId) {
        updateMenu(menuId, submitData).then(() => {
          ElMessage.success("修改成功");
          closeDialog();
          handleQuery();
        });
      } else {
        addMenu(submitData).then(() => {
          ElMessage.success("新增成功");
          closeDialog();
          handleQuery();
        });
      }
    }
  });
}

/** 删除菜单 */
function handleDelete(menuId: number) {
  if (!menuId) {
    ElMessage.warning("请勾选删除项");
    return false;
  }

  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      deleteMenu(menuId).then(() => {
        ElMessage.success("删除成功");
        handleQuery();
      });
    })
    .catch(() => ElMessage.info("已取消删除"));
}

/** 关闭弹窗 */
function closeDialog() {
  dialog.visible = false;
  resetForm();
}

/** 重置表单 */
function resetForm() {
  menuFormRef.value.resetFields();
  menuFormRef.value.clearValidate();

  formData.id = undefined;
  formData.parentId = 0;
  formData.hidden = 0;
  formData.sort = 1;
  formData.perm = undefined;
  formData.component = undefined;
  formData.path = undefined;
  formData.redirect = undefined;
  formData.name = undefined;
  formData.icon = undefined;
  formData.title = undefined;
  formData.type = MenuTypeEnum.MENU;
}

onMounted(() => {
  handleQuery();
});
</script>
