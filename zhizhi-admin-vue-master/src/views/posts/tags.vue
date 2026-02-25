<script setup lang="ts">
import { getTagPage, updateTag, addTag, deleteTag, topTag } from "@/api/tag";
import { TagForm, TagPageVO, TagQuery } from "@/api/tag/types";

defineOptions({
  name: "Tag",
  inheritAttrs: false,
});

const queryFormRef = ref(ElForm);
const tagFormRef = ref(ElForm);

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref(0);

const queryParams = reactive<TagQuery>({
  pageNo: 1,
  pageSize: 10,
});

const tagList = ref<TagPageVO[]>();

const dialog = reactive({
  title: "",
  visible: false,
});

const formData = reactive<TagForm>({
  id: undefined,
  name: "",
  description: "",
  sort: 1,
});

const rules = reactive({
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
});

/** 查询 */
function handleQuery() {
  loading.value = true;
  getTagPage(queryParams)
    .then(({ data }: any) => {
      // 后端返回格式: { code, info, data: [...] } 或 { code, info, data: { data: [...], total } }
      if (Array.isArray(data)) {
        tagList.value = data;
        total.value = data.length;
      } else {
        tagList.value = data.data || data;
        total.value = data.total || tagList.value.length;
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

/** 打开表单弹窗 */
function openDialog(tag?: Object) {
  dialog.visible = true;
  if (tag) {
    dialog.title = "修改标签";
    Object.assign(formData, tag);
  } else {
    dialog.title = "新增标签";
  }
}

/** 保存提交 */
function handleSubmit() {
  tagFormRef.value.validate((valid: any) => {
    if (valid) {
      loading.value = true;
      const id = formData.id;
      if (id) {
        updateTag(formData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addTag(formData)
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
  tagFormRef.value.resetFields();
  tagFormRef.value.clearValidate();
  formData.id = undefined;
  formData.name = "";
  formData.description = "";
  formData.sort = 1;
}

/** 置顶/取消置顶 */
function handleToggleTop(id: number) {
  loading.value = true;
  topTag(id)
    .then(() => {
      ElMessage.success("操作成功");
      handleQuery();
    })
    .finally(() => (loading.value = false));
}

/** 删除 */
function handleDelete(id?: number) {
  const formIds = ref<any>([]);
  if (id) {
    formIds.value.push(id);
  }
  if (ids.value.length) {
    formIds.value = ids.value;
  }
  if (!formIds.value) {
    ElMessage.warning("请勾选删除项");
    return;
  }
  ElMessageBox.confirm("确认删除已选中的数据项?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    loading.value = true;
    deleteTag(formIds.value)
      .then(() => {
        ElMessage.success("删除成功");
        resetQuery();
      })
      .finally(() => (loading.value = false));
  });
}

const handleChange = (value: Number) => {
  formData.sort = value;
};

onMounted(() => {
  handleQuery();
});
</script>

<template>
  <div class="app-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form ref="queryFormRef" :model="queryParams" class="search-form">
        <div class="form-row">
          <el-form-item prop="name" label="标签名称">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入标签名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <div class="btn-group">
            <el-button type="primary" @click="handleQuery">
              <i-ep-search />查询
            </el-button>
            <el-button @click="resetQuery">
              <i-ep-refresh />重置
            </el-button>
          </div>
        </div>
      </el-form>

      <div class="divider"></div>

      <!-- 表格区域 -->
      <div class="table-section">
        <div class="operation-bar">
          <div class="left">
            <el-button type="primary" @click="openDialog()" v-hasPerm="['system:tag:add']">
              <i-ep-plus />新建
            </el-button>
            <el-button type="danger" :disabled="ids.length === 0" @click="handleDelete()" v-hasPerm="['system:tag:delete']">
              <i-ep-delete />批量删除
            </el-button>
          </div>
        </div>

        <el-table
        ref="dataTableRef"
        v-loading="loading"
        :data="tagList"
        highlight-current-row
        stripe
        fit
        max-height="600px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="标签名" align="center" prop="name" min-width="120" />
        <el-table-column label="描述" align="center" prop="description" min-width="180" show-overflow-tooltip />
        <el-table-column label="使用次数" align="center" prop="usageCount" width="100" />
        <el-table-column label="推荐" align="center" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isRecommended === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.isRecommended === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" align="center" prop="sort" width="80" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column fixed="right" align="center" label="操作" width="260">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              link
              @click="openDialog(scope.row)"
              v-hasPerm="['system:tag:update']"
            >
              <i-ep-edit />编辑
            </el-button>
            <el-button
              :type="scope.row.isRecommended === 1 ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleTop(scope.row.id)"
              v-hasPerm="['system:tag:update']"
            >
              {{ scope.row.isRecommended === 1 ? '取消推荐' : '推荐' }}
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row.id)"
              v-hasPerm="['system:tag:delete']"
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
      </div>
    </div>

    <!-- 标签表单弹窗 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
      @close="closeDialog"
    >
      <el-form
        ref="tagFormRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标签名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入标签名称" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入标签描述（可选）" 
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="1"
            :max="100"
            @change="handleChange"
          />
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
