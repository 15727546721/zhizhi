<template>
  <div class="column-manage">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>管理专栏</h1>
    </div>

    <el-skeleton v-if="loading" :rows="5" animated />

    <div v-else-if="column" class="manage-content">
      <!-- 基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-button type="primary" @click="showEditDialog = true">编辑</el-button>
          </div>
        </template>
        <div class="column-info">
          <el-image
            v-if="column.coverUrl"
            :src="column.coverUrl"
            class="cover"
            fit="cover"
          />
          <div class="info-text">
            <h2>{{ column.name }}</h2>
            <p>{{ column.description }}</p>
            <div class="stats">
              <span>文章数: {{ column.postCount }}</span>
              <span>订阅数: {{ column.subscribeCount }}</span>
              <span>状态: {{ column.status === 1 ? '已发布' : '草稿' }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 文章管理 -->
      <el-card class="posts-card">
        <template #header>
          <div class="card-header">
            <span>文章管理 ({{ posts.length }})</span>
            <el-button type="primary" @click="showAddDialog = true">添加文章</el-button>
          </div>
        </template>

        <el-empty v-if="posts.length === 0" description="暂无文章" />

        <div v-else class="posts-list">
          <draggable
            v-model="posts"
            item-key="postId"
            handle=".drag-handle"
            @end="handleDragEnd"
          >
            <template #item="{ element }">
              <div class="post-item">
                <el-icon class="drag-handle"><Rank /></el-icon>
                <div class="post-info">
                  <h3>{{ element.title }}</h3>
                  <div class="post-meta">
                    <span>排序: {{ element.sort }}</span>
                    <span>浏览: {{ element.viewCount }}</span>
                    <span>点赞: {{ element.likeCount }}</span>
                  </div>
                </div>
                <div class="post-actions">
                  <el-button
                    type="primary"
                    link
                    @click="viewPost(element.postId)"
                  >
                    查看
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    @click="removePost(element.postId)"
                  >
                    移除
                  </el-button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
      </el-card>
    </div>

    <!-- 编辑专栏对话框 -->
    <ColumnFormDialog
      v-if="showEditDialog"
      :visible="showEditDialog"
      :column="column"
      @close="showEditDialog = false"
      @success="handleEditSuccess"
    />

    <!-- 添加文章对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加文章到专栏"
      width="600px"
    >
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="选择文章">
          <el-select
            v-model="addForm.postId"
            placeholder="请选择文章"
            filterable
            remote
            :remote-method="searchMyPosts"
            :loading="searchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="post in myPosts"
              :key="post.id"
              :label="post.title"
              :value="post.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number
            v-model="addForm.sort"
            :min="0"
            :max="9999"
            placeholder="留空自动排序"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddPost" :loading="addLoading">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import ColumnFormDialog from '@/components/column/ColumnFormDialog.vue'
import * as columnApi from '@/api/column'
import * as postApi from '@/api/post'
import type { ColumnDetailVO, ColumnPostVO } from '@/types/column'
import type { Post } from '@/types/post'

const route = useRoute()
const router = useRouter()

const columnId = ref(Number(route.params.id))
const loading = ref(false)
const column = ref<ColumnDetailVO | null>(null)
const posts = ref<ColumnPostVO[]>([])

const showEditDialog = ref(false)
const showAddDialog = ref(false)
const addForm = ref({
  postId: null as number | null,
  sort: undefined as number | undefined
})
const myPosts = ref<Post[]>([])
const searchLoading = ref(false)
const addLoading = ref(false)

onMounted(() => {
  loadColumnData()
  loadMyPosts('')
})

const loadColumnData = async () => {
  try {
    loading.value = true
    
    // 加载专栏详情
    const detailRes = await columnApi.getColumnDetail(columnId.value) as any
    if (detailRes.code === 20000 && detailRes.data) {
      column.value = detailRes.data
    }
    
    // 加载文章列表
    const postsRes = await columnApi.getColumnPosts(columnId.value, 1, 100) as any
    if (postsRes.code === 20000 && postsRes.data) {
      // 后端返回的是 PageResponse 结构：{ pageNo, pageSize, total, data }
      posts.value = postsRes.data.data || []
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadMyPosts = async (keyword: string) => {
  try {
    searchLoading.value = true
    const res = await postApi.getMyPosts({
      pageNo: 1,
      pageSize: 50,
      status: 'PUBLISHED' // 只显示已发布的文章
    }) as any
    
    if (res.code === 20000 && res.data) {
      // 后端返回的是 PageResponse 结构：{ pageNo, pageSize, total, data }
      myPosts.value = res.data.data || []
      
      // 过滤掉已在专栏中的文章
      const existingPostIds = posts.value.map(p => p.postId)
      myPosts.value = myPosts.value.filter(p => !existingPostIds.includes(p.id))
      
      // 如果有关键词，进行过滤
      if (keyword) {
        myPosts.value = myPosts.value.filter(p => 
          p.title.toLowerCase().includes(keyword.toLowerCase())
        )
      }
    }
  } catch (error) {
    // 加载失败
  } finally {
    searchLoading.value = false
  }
}

const searchMyPosts = (keyword: string) => {
  loadMyPosts(keyword)
}

const handleAddPost = async () => {
  if (!addForm.value.postId) {
    ElMessage.warning('请选择文章')
    return
  }
  
  try {
    addLoading.value = true
    const res = await columnApi.addPostToColumn(
      columnId.value,
      addForm.value.postId,
      addForm.value.sort
    ) as any
    
    if (res.code === 20000) {
      ElMessage.success('添加成功')
      showAddDialog.value = false
      addForm.value = { postId: null, sort: undefined }
      loadColumnData()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    addLoading.value = false
  }
}

const removePost = async (postId: number) => {
  try {
    await ElMessageBox.confirm('确定要从专栏中移除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await columnApi.removePostFromColumn(columnId.value, postId) as any
    if (res.code === 20000) {
      ElMessage.success('移除成功')
      loadColumnData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const handleDragEnd = async () => {
  try {
    // 更新排序值
    const sortList = posts.value.map((post, index) => ({
      postId: post.postId,
      sort: index + 1
    }))
    
    const res = await columnApi.updatePostsSort(columnId.value, sortList) as any
    if (res.code === 20000) {
      ElMessage.success('排序已更新')
    }
  } catch (error) {
    // 更新失败
    ElMessage.error('排序失败')
    loadColumnData() // 重新加载恢复原顺序
  }
}

const handleEditSuccess = () => {
  showEditDialog.value = false
  loadColumnData()
}

const viewPost = (postId: number) => {
  router.push(`/post/${postId}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.column-manage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.manage-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card {
  .column-info {
    display: flex;
    gap: 20px;

    .cover {
      width: 200px;
      height: 120px;
      border-radius: 8px;
      flex-shrink: 0;
    }

    .info-text {
      flex: 1;

      h2 {
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 12px 0;
      }

      p {
        color: #646a73;
        margin: 0 0 16px 0;
      }

      .stats {
        display: flex;
        gap: 24px;
        font-size: 14px;
        color: #8a919f;

        span {
          display: flex;
          align-items: center;
        }
      }
    }
  }
}

.posts-card {
  .posts-list {
    .post-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      border: 1px solid #e4e6eb;
      border-radius: 8px;
      margin-bottom: 12px;
      background: #fff;
      transition: all 0.3s;

      &:hover {
        border-color: #1e80ff;
        box-shadow: 0 2px 8px rgba(30, 128, 255, 0.1);
      }

      .drag-handle {
        font-size: 20px;
        color: #8a919f;
        cursor: move;

        &:hover {
          color: #1e80ff;
        }
      }

      .post-info {
        flex: 1;

        h3 {
          font-size: 16px;
          font-weight: 500;
          margin: 0 0 8px 0;
          color: #1f2329;
        }

        .post-meta {
          display: flex;
          gap: 16px;
          font-size: 13px;
          color: #8a919f;
        }
      }

      .post-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
}

@media (max-width: 768px) {
  .column-manage {
    padding: 16px;
  }

  .info-card .column-info {
    flex-direction: column;

    .cover {
      width: 100%;
    }
  }

  .post-item {
    flex-wrap: wrap;

    .post-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
