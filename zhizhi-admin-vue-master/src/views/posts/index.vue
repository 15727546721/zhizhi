<script setup lang="ts">
import { MdEditor } from "md-editor-v3";
import "md-editor-v3/lib/style.css";
import { getTagPage } from "@/api/tag";

import {
  getPostPage,
  addPost,
  updatePost,
  deletePost,
  topPost,
  getPostInfo,
  togglePostFeatured,
  togglePostStatus,
} from "@/api/post";
import type { PostPageVO, PostQuery } from "@/api/post/types";

import { upload, delBatchFile } from "@/api/file";

import {
  Promotion,
  Close,
  Edit,
  ArrowLeft,
  Picture,
  Plus,
  Upload,
  InfoFilled,
  Search,
  Refresh,
  User,
} from "@element-plus/icons-vue";
import { ref, reactive, onMounted } from "vue";

defineOptions({
  name: "Article",
  inheritAttrs: false,
});

const queryFormRef = ref(ElForm);
const formRef = ref(ElForm);

const loading = ref(false);
const ids = ref<number[]>([]);
const total = ref<number>(0);

const queryParams = reactive<PostQuery>({
  pageNo: 1,
  pageSize: 10,
  title: undefined,
  tagId: undefined,
});

const tableData = ref<PostPageVO[]>([]);

const dialog = reactive({
  title: "",
  visible: false,
});

interface Article {
  id?: number;
  title: string;
  content: string;
  description: string;
  tagNames?: string;
  tagIds?: number[] | string;
}

interface PostForm {
  id?: number;
  title: string;
  content: string;
  description: string;
  coverUrl?: string;
  tagNameList: string[];
  tagIds: number[];
}

const formData = reactive<PostForm>({
  title: "",
  content: "",
  description: "",
  tagNameList: [],
  tagIds: [],
});

const rules = reactive({
  title: [{ required: true, message: "必填字段", trigger: "blur" }],
  description: [{ required: true, message: "必填字段", trigger: "blur" }],
  tagNameList: [{ required: true, message: "必填字段", trigger: "blur" }],
  tagIds: [{ required: true, message: "必填字段", trigger: "blur" }],
});

const tagList = ref<Tag[]>([]);
const tagListLoaded = ref(false); // 标签列表是否已加载
const mdRef = ref();

interface Tag {
  id: number;
  name: string;
}

interface UploadParams {
  file: File;
}

interface FormDataWithCover extends FormData {
  coverUrl?: string;
}

interface UploadResponse {
  data: string;
}

/** 查询 */
function handleQuery() {
  loading.value = true;
  getPostPage(queryParams)
    .then((res) => {
      const { data, total: totalCount, pageNo, pageSize } = res.data;
      tableData.value = data;
      total.value = totalCount;
      // 后端返回的pageNo已经是从1开始，无需转换
      queryParams.pageNo = pageNo;
      queryParams.pageSize = pageSize;
    })
    .catch((error) => {
      console.error("获取帖子列表失败:", error);
      ElMessage.error("获取帖子列表失败，请确保后端服务已启动");
    })
    .finally(() => {
      loading.value = false;
    });
}

/** 重置查询 */
function resetQuery() {
  queryFormRef.value.resetFields();
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: 10,
    title: undefined,
    tagId: undefined,
  });
  handleQuery();
}

/** 处理分页变化 */
function handlePaginationChange({
  page,
  limit,
}: {
  page: number;
  limit: number;
}) {
  queryParams.pageNo = page;
  queryParams.pageSize = limit;
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

/** 打开帖子表单弹窗 */
function openDialog(row?: any) {
  dialog.visible = true;
  if (row) {
    dialog.title = "修改帖子";
    loading.value = true;
    // 获取帖子详情
    getPostInfo(row.id)
      .then((res) => {
        const data = res.data;
        const post = data.post; // 后端返回 { post: {...}, tagNames: [...], tagIds: [...] }

        // 更新formData基本信息
        Object.assign(formData, {
          id: post.id,
          title: post.title,
          coverUrl: post.coverUrl,
          content: post.content,
          description: post.description,
          tagNameList: data.tagNames || [],
          tagIds: data.tagIds || [],
        });
      })
      .catch((error) => {
        console.error("获取帖子详情失败:", error);
        ElMessage.error("获取帖子详情失败");
      })
      .finally(() => {
        loading.value = false;
      });
  } else {
    dialog.title = "新增帖子";
    resetForm();
  }
}

/** 加精/取消加精 */
function handleToggleFeatured(row: any) {
  const action = row.isFeatured === 1 ? "取消加精" : "加精";
  ElMessageBox.confirm(`确认${action}该帖子吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    togglePostFeatured({ id: row.id }).then(() => {
      ElMessage.success(`${action}成功`);
      handleQuery();
    });
  });
}

/** 发布/下架 */
function handleToggleStatus(row: any) {
  const action = row.status === 1 ? "下架" : "发布";
  ElMessageBox.confirm(`确认${action}该帖子吗？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    togglePostStatus({ id: row.id }).then(() => {
      ElMessage.success(`${action}成功`);
      handleQuery();
    });
  });
}

/**
 * 处理标签变化
 */
function handleTagChange(tagNames: string[]) {
  formData.tagNameList = tagNames;
  formData.tagIds = tagNames
    .map((name) => {
      const tag = tagList.value.find((item: Tag) => item.name === name);
      return tag ? tag.id : null;
    })
    .filter((id): id is number => id !== null);
}

/** 标签存提交 */
function handleSubmit() {
  formRef.value.validate((valid: any) => {
    if (valid) {
      loading.value = true;
      const id = formData.id;

      // 构造提交的数据对象
      const submitData = {
        ...formData,
        tagIds: formData.tagIds,
      };

      if (id) {
        updatePost(submitData)
          .then(() => {
            ElMessage.success("修改成功");
            closeDialog();
            resetQuery();
          })
          .finally(() => (loading.value = false));
      } else {
        addPost(submitData)
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
  if (formRef.value) {
    formRef.value.resetFields();
    formRef.value.clearValidate();
  }

  // 重置所有字段为初始值
  Object.assign(formData, {
    id: undefined,
    title: "",
    content: "",
    description: "",
    coverUrl: "",
    tagNameList: [],
    tagIds: [],
  });
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
    deletePost(formIds.value)
      .then(() => {
        ElMessage.success("删除成功");
        resetQuery();
      })
      .finally(() => (loading.value = false));
  });
}

// 函数用于返回匹配的标签名称
function getTagNamesByTagIds(tagList: Tag[], tagIds: number[]): string[] {
  return tagList
    .filter((tag: Tag) => tagIds.includes(tag.id))
    .map((filteredTag: Tag) => filteredTag.name);
}

function strSplit(item: any) {
  return item.split(",");
}

//上传之前的操作
function uploadBefore() {
  loading.value = true;
}

//上传文件
function uploadSectionFile(param: UploadParams): Promise<any> {
  loading.value = true;
  const formData: FormDataWithCover = new FormData();
  formData.append("files", param.file);

  return upload(formData)
    .then((res: UploadResponse) => {
      formData.coverUrl = res.data;
      return res;
    })
    .finally(() => {
      loading.value = false;
    });
}

function randomImg() {
  const url =
    "https://picsum.photos/500/300?random=" + Math.round(Date.now() / 1000);
  formData.coverUrl = url; // 同时设置 coverUrl
}

// 编辑器图片上传
const onUploadImg = async (
  files: File[],
  callback: (urls: string[]) => void
) => {
  try {
    const uploadPromises = files.map((file) => {
      const formData = new FormData();
      formData.append("files", file);
      return upload(formData);
    });

    const responses = await Promise.all(uploadPromises);
    const urls = responses.map((res) => res.data);

    // 将图片URL数组传给回调函数
    callback(urls);
  } catch (error) {
    console.error("图片上传失败:", error);
    ElMessage.error("图片上传失败");
  }
};

function loadTagList() {
  // 避免重复加载
  if (tagListLoaded.value && tagList.value.length > 0) {
    return;
  }
  getTagPage({ pageSize: 100 }).then((res) => {
    tagList.value = res.data;
    tagListLoaded.value = true;
  });
}

onMounted(() => {
  handleQuery();
  loadTagList();
});
</script>

<template>
  <div class="app-container">
    <div class="content-wrapper">
      <!-- 搜索表单 -->
      <el-form ref="queryFormRef" :model="queryParams" class="search-form">
        <div class="form-row">
          <el-form-item prop="title" label="帖子标题">
            <el-input
              v-model="queryParams.title"
              placeholder="请输入帖子标题"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="标签筛选" prop="tagId">
            <el-select
              v-model="queryParams.tagId"
              filterable
              clearable
              placeholder="请选择标签"
            >
              <el-option
                v-for="item in tagList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <div class="btn-group">
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </div>
        </div>
      </el-form>

      <div class="divider"></div>

      <!-- 表格区域 -->
      <div class="table-section">
        <div class="operation-bar">
          <div class="left">
            <el-button type="primary" @click="openDialog()">
              <i-ep-plus />新建
            </el-button>
            <el-button type="danger" :disabled="ids.length === 0" @click="handleDelete()">
              <i-ep-delete />批量删除
            </el-button>
          </div>
        </div>

        <el-table
          ref="dataTableRef"
          :data="tableData"
          highlight-current-row
          stripe
          fit
          @selection-change="handleSelectionChange"
          v-loading="loading"
          max-height="600px"
          :row-style="{ height: '120px' }"
        >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column width="100" align="center" label="封面">
          <template #default="scope">
            <div class="cover-wrapper" v-if="scope.row.coverUrl">
              <el-image
                class="article-cover"
                :src="scope.row.coverUrl"
                fit="cover"
                lazy
                :preview-src-list="[scope.row.coverUrl]"
                preview-teleported
                hide-on-click-modal
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-placeholder" v-else>
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="标题"
          min-width="200"
          align="left"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="article-title-cell">
              {{ scope.row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="120" align="center">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">
              <el-icon><User /></el-icon>
              {{ scope.row.nickname }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="180" align="left">
          <template #default="scope">
            <div class="article-tags">
              <template
                v-if="scope.row.tagNames && scope.row.tagNames.length > 0"
              >
                <el-tag
                  v-for="tag in scope.row.tagNames.split(',')"
                  :key="tag"
                  size="small"
                  type="success"
                  effect="light"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </template>
              <el-tag
                v-else
                size="small"
                type="info"
                effect="plain"
                class="empty-tag"
              >
                <el-icon><InfoFilled /></el-icon>
                暂无标签
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 0 ? 'info' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '已发布' : scope.row.status === 0 ? '草稿' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加精" width="70" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isFeatured === 1 ? 'warning' : 'info'" size="small">
              {{ scope.row.isFeatured === 1 ? '精选' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="帖子描述" min-width="200" align="left">
          <template #default="scope">
            <el-popover
              placement="top-start"
              :width="300"
              trigger="hover"
              :popper-style="{ padding: '12px' }"
            >
              <template #default>
                <div class="article-description-popover">
                  {{ scope.row.description || "暂无描述" }}
                </div>
              </template>
              <template #reference>
                <div class="article-description">
                  {{ scope.row.description || "暂无描述" }}
                </div>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" width="160" sortable>
          <template #default="scope">
            <el-tooltip
              :content="scope.row.createTime"
              placement="top"
              effect="light"
            >
              <span>{{ scope.row.createTime }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" width="160" sortable>
          <template #default="scope">
            <el-tooltip
              :content="scope.row.updateTime"
              placement="top"
              effect="light"
            >
              <span>{{ scope.row.updateTime }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button
                type="primary"
                link
                size="small"
                @click="openDialog(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                :type="scope.row.isFeatured === 1 ? 'info' : 'warning'"
                link
                size="small"
                @click="handleToggleFeatured(scope.row)"
              >
                {{ scope.row.isFeatured === 1 ? '取消加精' : '加精' }}
              </el-button>
              <el-button
                :type="scope.row.status === 1 ? 'info' : 'success'"
                link
                size="small"
                @click="handleToggleStatus(scope.row)"
              >
                {{ scope.row.status === 1 ? '下架' : '发布' }}
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleDelete(scope.row.id)"
              >
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

    <!--添加or修改区域-->
    <el-dialog
      v-model="dialog.visible"
      :fullscreen="true"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      title=""
    >
      <div class="article-editor">
        <!-- 顶部导航栏 -->
        <div class="editor-header">
          <div class="left">
            <el-button link @click="closeDialog">
              <el-icon><ArrowLeft /></el-icon>
              返回列表
            </el-button>
            <el-divider direction="vertical" />
            <span class="page-title">{{ dialog.title }}</span>
          </div>
          <div class="right">
            <el-button type="primary" @click="handleSubmit">
              <el-icon><Promotion /></el-icon>
              保存帖子
            </el-button>
          </div>
        </div>

        <!-- 主体内容区 -->
        <div class="editor-container">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-width="0"
          >
            <el-row :gutter="24">
              <!-- 左侧编辑区 -->
              <el-col :span="18">
                <div class="content-main">
                  <!-- 标题输入 -->
                  <div class="title-wrapper">
                    <el-input
                      v-model="formData.title"
                      placeholder="请输入帖子标题"
                      maxlength="60"
                      show-word-limit
                      class="title-input"
                    />
                  </div>

                  <!-- Markdown编辑器 -->
                  <div class="editor-wrapper">
                    <MdEditor
                      ref="mdRef"
                      v-model="formData.content"
                      @on-upload-img="onUploadImg"
                      placeholder="开始创作你的帖子..."
                      preview-theme="github"
                      style="height: 100%"
                    />
                  </div>
                </div>
              </el-col>

              <!-- 右侧配置区 -->
              <el-col :span="6">
                <div class="content-aside">
                  <!-- 文章属性卡片 -->
                  <el-card class="config-card" shadow="never">
                    <template #header>
                      <div class="card-header">
                        <span>帖子属性</span>
                      </div>
                    </template>

                    <!-- 帖子标签 -->
                    <div class="form-item">
                      <div class="item-label required">帖子标签</div>
                      <el-select
                        v-model="formData.tagNameList"
                        multiple
                        filterable
                        :multiple-limit="5"
                        placeholder="请选择帖子标签（最多5个）"
                        class="tag-select"
                        @change="handleTagChange"
                      >
                        <el-option
                          v-for="item in tagList"
                          :key="item.id"
                          :label="item.name"
                          :value="item.name"
                        >
                          <span class="tag-option">{{ item.name }}</span>
                        </el-option>
                      </el-select>
                    </div>

                    <!-- 帖子描述 -->
                    <div class="form-item">
                      <div class="item-label required">帖子描述</div>
                      <el-input
                        v-model="formData.description"
                        type="textarea"
                        :rows="4"
                        placeholder="请输入帖子描述"
                        maxlength="100"
                        show-word-limit
                      />
                    </div>
                  </el-card>

                  <!-- 封面设置卡片 -->
                  <el-card class="config-card cover-card" shadow="never">
                    <template #header>
                      <div class="card-header">
                        <span>封面图</span>
                        <el-button type="primary" link @click="randomImg">
                          <el-icon><Picture /></el-icon>
                          随机图片
                        </el-button>
                      </div>
                    </template>
                    <div class="cover-container">
                      <el-upload
                        class="cover-uploader"
                        :show-file-list="false"
                        accept="image/*"
                        :before-upload="uploadBefore"
                        :http-request="uploadSectionFile"
                      >
                        <div
                          class="cover-preview"
                          :class="{ 'has-image': formData.coverUrl }"
                        >
                          <template v-if="formData.coverUrl">
                            <el-image
                              :src="formData.coverUrl"
                              fit="cover"
                              class="cover-image"
                            >
                              <template #error>
                                <div class="image-error">
                                  <el-icon><Picture /></el-icon>
                                </div>
                              </template>
                            </el-image>
                            <div class="cover-mask">
                              <el-icon><Upload /></el-icon>
                              <span>点击更换</span>
                            </div>
                          </template>
                          <template v-else>
                            <div class="upload-placeholder">
                              <el-icon><Plus /></el-icon>
                              <span>点击上传封面</span>
                            </div>
                          </template>
                        </div>
                      </el-upload>
                      <div class="upload-tip">
                        <el-icon><InfoFilled /></el-icon>
                        <span
                          >建议尺寸：1200x675px，支持 jpg、png、gif 格式</span
                        >
                      </div>
                    </div>
                  </el-card>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-dialog) {
  margin: 0 !important;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    height: 100vh;
  }
}

.article-editor {
  height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.editor-header {
  height: 56px;
  padding: 0 24px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 10;

  .left {
    display: flex;
    align-items: center;
    gap: 16px;

    .page-title {
      font-size: 16px;
      font-weight: 500;
      color: #1f2329;
    }
  }

  .right {
    display: flex;
    gap: 12px;
  }
}

.editor-container {
  flex: 1;
  padding: 16px 24px;
  height: calc(100vh - 56px);
  overflow: hidden;

  .el-row {
    height: 100%;

    .el-col {
      height: 100%;
    }
  }
}

.content-main {
  height: calc(100vh - 88px);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .title-wrapper {
    padding: 16px 24px;
    border-bottom: 1px solid #f0f0f0;
    position: relative;
    flex-shrink: 0;

    .title-input {
      :deep(.el-input__wrapper) {
        padding: 0;
        box-shadow: none;
        background-color: transparent;
      }

      :deep(.el-input__inner) {
        font-size: 24px;
        font-weight: 500;
        height: 40px;
        padding: 0;
        border: none;
        color: #1f2329;

        &::placeholder {
          color: #bfbfbf;
          font-weight: normal;
        }
      }

      :deep(.el-input__count) {
        background: transparent;
        font-size: 12px;
        color: #bfbfbf;
        height: auto;
        padding: 0;
        line-height: 1;
        position: absolute;
        right: 0;
        bottom: -20px;
      }
    }
  }

  .editor-wrapper {
    flex: 1;
    height: calc(100% - 73px);
    overflow: hidden;
    padding: 16px;

    :deep(.md-editor) {
      height: 100%;
      border: none;

      .md-editor-toolbar {
        padding: 8px 16px;
        border-bottom: 1px solid #f0f0f0;
      }

      .md-editor-content {
        height: calc(100% - 48px);
      }
    }
  }
}

.content-aside {
  height: calc(100vh - 88px);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding-right: 8px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  .config-card {
    background-color: #fff;
    border-radius: 8px;
    margin-bottom: 12px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);

    :deep(.el-card__header) {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.el-card__body) {
      padding: 12px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-size: 14px;
        font-weight: 500;
        color: #1f2329;
      }
    }
  }

  .form-item {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .item-label {
      margin-bottom: 8px;
      font-size: 14px;
      color: #1f2329;

      &.required::before {
        content: "*";
        color: #ff4d4f;
        margin-right: 4px;
      }
    }
  }
}

.cover-card {
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.cover-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-uploader {
  .cover-preview {
    width: 100%;
    aspect-ratio: 16/9;
    border-radius: 8px;
    overflow: hidden;
    border: 1px dashed var(--el-border-color);
    cursor: pointer;
    transition: all 0.3s;
    background-color: var(--el-fill-color-blank);
    position: relative;

    &:hover {
      border-color: var(--el-color-primary);
      background-color: var(--el-fill-color-light);

      .cover-mask {
        opacity: 1;
      }

      .upload-placeholder {
        color: var(--el-color-primary);
      }
    }

    &.has-image {
      border-style: solid;
      border-color: var(--el-border-color-light);

      &:hover {
        border-color: var(--el-color-primary);
      }
    }

    .cover-image {
      width: 100%;
      height: 100%;
      display: block;
    }

    .cover-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.3s;

      .el-icon {
        font-size: 24px;
        margin-bottom: 8px;
      }

      span {
        font-size: 14px;
      }
    }

    .upload-placeholder {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-secondary);
      transition: color 0.3s;

      .el-icon {
        font-size: 24px;
        margin-bottom: 8px;
      }

      span {
        font-size: 14px;
      }
    }

    .image-error {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 24px;
        margin-bottom: 8px;
      }
    }
  }
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;

  .el-icon {
    font-size: 14px;
  }
}

:deep(.el-select),
:deep(.el-input) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px #d9d9d9 inset;

    &:hover {
      box-shadow: 0 0 0 1px #4096ff inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #1677ff inset;
    }
  }
}

/* Markdown 编辑器样式优化 */
:deep(.md-editor) {
  border: none;
  border-radius: 0;
}

:deep(.md-editor-toolbar) {
  border-bottom: 1px #f0f0f0;
  padding: 8px 16px;

  .md-editor-toolbar-item {
    margin: 0 2px;
    padding: 4px;
    border-radius: 4px;

    &:hover {
      background-color: #f5f5f5;
    }

    svg {
      width: 16px;
      height: 16px;
    }
  }

  .md-editor-toolbar-divider {
    margin: 0 8px;
    width: 1px;
    background-color: #e8e8e8;
  }
}

/* 添加标签选择的样式 */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 2px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.tag-checkbox {
  margin-right: 0 !important;
  margin-bottom: 0 !important;

  :deep(.el-checkbox__input) {
    display: none;
  }

  :deep(.el-checkbox__label) {
    padding: 0;
  }

  &:deep(.el-checkbox__inner) {
    display: none;
  }

  &.is-checked {
    :deep(.el-checkbox__label) {
      color: var(--el-color-primary);
    }
    border-color: var(--el-color-primary);
  }
}

/* 添加标签选择相关样式 */
.tag-select {
  width: 100%;

  :deep(.el-select__tags) {
    flex-wrap: wrap;
  }
}

:deep(.tag-select-dropdown) {
  .el-select-dropdown__wrap {
    max-height: 300px;
  }

  .tag-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 8px;
    padding: 12px;
  }

  .el-select-dropdown__item {
    padding: 0;
    height: auto;
    line-height: 1;

    &.selected {
      font-weight: normal;
    }

    &:hover {
      background-color: transparent;
    }
  }

  .tag-item {
    padding: 6px 12px;
    text-align: center;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }
  }

  .el-select-dropdown__item.selected .tag-item {
    color: var(--el-color-primary);
    border-color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }
}

/* 已选标签样式 */
:deep(.el-select__tags) {
  .el-tag {
    background-color: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
    margin: 2px 4px 2px 0;

    .el-tag__close {
      color: var(--el-color-primary);

      &:hover {
        background-color: var(--el-color-primary);
        color: white;
      }
    }
  }
}

/* 文章列表样式优化 */
.article-title-cell {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  cursor: pointer;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  max-height: 42px;

  &:hover {
    color: var(--el-color-primary);
  }
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 4px 0;
  max-height: 64px;
  overflow: hidden;

  .tag-item {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .empty-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.article-description {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  max-height: 60px;
  margin: 4px 0;
}

.article-description-popover {
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
  max-height: 200px;
  overflow-y: auto;
  padding: 8px 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--el-border-color-light);
    border-radius: 2px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.cover-wrapper {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  background-color: var(--el-fill-color-light);

  .article-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);

    .el-icon {
      font-size: 20px;
    }
  }
}

.image-placeholder {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);

  .el-icon {
    font-size: 20px;
  }
}

:deep(.el-table) {
  --el-table-row-height: 120px;

  .el-table__cell {
    padding: 8px;
  }

  .cell {
    overflow: hidden;
  }
}
</style>
