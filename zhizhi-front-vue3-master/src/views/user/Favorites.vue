<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>
    
    <div class="content-wrapper">
      <!-- 左侧收藏夹列表 -->
      <div class="folder-sidebar">
        <div class="folder-header">
          <span>收藏夹</span>
          <el-button type="primary" link @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建
          </el-button>
        </div>
        
        <div class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            class="folder-item"
            :class="{ active: currentFolderId === folder.id }"
            @click="selectFolder(folder.id)"
          >
            <div class="folder-main">
              <div class="folder-info">
                <span class="folder-name">{{ folder.name }}</span>
                <el-tag v-if="folder.isDefault" size="small" type="info">默认</el-tag>
              </div>
              <span class="folder-count">{{ folder.itemCount }}</span>
            </div>
            <el-dropdown trigger="click" @command="handleFolderAction($event, folder)">
              <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item v-if="!folder.isDefault" command="move" divided>迁移内容到...</el-dropdown-item>
                  <el-dropdown-item v-if="!folder.isDefault" command="merge">合并到...</el-dropdown-item>
                  <el-dropdown-item v-if="!folder.isDefault" command="delete" divided>删除</el-dropdown-item>
                  <template v-else>
                    <el-dropdown-item command="move" divided>迁移内容到...</el-dropdown-item>
                    <el-dropdown-item disabled divided>
                      <span class="disabled-tip">默认收藏夹不能删除</span>
                    </el-dropdown-item>
                  </template>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <!-- 右侧收藏内容 -->
      <div class="favorites-content">
        <div v-if="loading" class="loading">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="favorites.length === 0" class="empty">
          <el-empty description="暂无收藏内容" />
        </div>
        <div v-else class="favorites-list">
          <PostList :posts="favorites" />
          <div v-if="hasMore" class="load-more">
            <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建/编辑收藏夹对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingFolder ? '编辑收藏夹' : '新建收藏夹'"
      width="400px"
      @close="resetForm"
    >
      <el-form :model="folderForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="folderForm.name" placeholder="请输入收藏夹名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="folderForm.description" type="textarea" placeholder="请输入描述（可选）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="公开">
          <el-switch v-model="folderForm.isPublic" />
          <span class="form-tip">公开后其他用户可以查看</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFolder" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 选择目标收藏夹对话框 -->
    <el-dialog
      v-model="showSelectFolderDialog"
      :title="selectFolderAction === 'move' ? '迁移内容到' : '合并到'"
      width="400px"
    >
      <div class="select-folder-content">
        <p class="select-tip">
          {{ selectFolderAction === 'move' 
            ? `将"${selectedSourceFolder?.name}"中的 ${selectedSourceFolder?.itemCount} 个内容移动到:` 
            : `将"${selectedSourceFolder?.name}"合并到:` }}
        </p>
        <el-radio-group v-model="targetFolderId" class="folder-radio-group">
          <el-radio 
            v-for="folder in availableTargetFolders" 
            :key="folder.id" 
            :label="folder.id"
            class="folder-radio"
          >
            <div class="folder-radio-content">
              <span class="folder-radio-name">{{ folder.name }}</span>
              <el-tag v-if="folder.isDefault" size="small" type="info">默认</el-tag>
              <span class="folder-radio-count">{{ folder.itemCount }} 项</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="showSelectFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmFolderAction" :loading="actionLoading">
          {{ selectFolderAction === 'move' ? '迁移' : '合并' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { folderApi, getMyFavorites, type FavoriteFolder } from '@/api/favorites'
import PostList from '@/views/home/components/PostList.vue'
import type { FavoriteItem } from '@/types/api'

interface FolderForm {
  name: string
  description: string
  isPublic: boolean
}

const route = useRoute()

const folders = ref<FavoriteFolder[]>([])
const currentFolderId = ref<number | null>(null)
const favorites = ref<FavoriteItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNo = ref(1)
const pageSize = 10
const total = ref(0)

const showCreateDialog = ref(false)
const editingFolder = ref<FavoriteFolder | null>(null)
const folderForm = ref<FolderForm>({
  name: '',
  description: '',
  isPublic: false
})
const saving = ref(false)

// 选择目标收藏夹对话框
const showSelectFolderDialog = ref(false)
const selectFolderAction = ref<'move' | 'merge'>('move')
const selectedSourceFolder = ref<FavoriteFolder | null>(null)
const targetFolderId = ref<number | null>(null)
const actionLoading = ref(false)

const availableTargetFolders = computed(() => {
  if (!selectedSourceFolder.value) return []
  return folders.value.filter(f => f.id !== selectedSourceFolder.value?.id)
})

const hasMore = computed(() => favorites.value.length < total.value)

onMounted(() => {
  loadFolders()
})

const loadFolders = async () => {
  try {
    const res = await folderApi.getMyFolders()
    if (res.code === 20000 && res.data) {
      folders.value = res.data
      // 检查URL参数中是否指定了收藏夹ID
      const queryFolderId = route.query.folderId
      if (queryFolderId) {
        const targetFolder = folders.value.find(f => f.id === Number(queryFolderId))
        if (targetFolder) {
          selectFolder(targetFolder.id)
          return
        }
      }
      // 默认选中第一个收藏夹
      if (folders.value.length > 0 && !currentFolderId.value) {
        selectFolder(folders.value[0].id)
      }
    }
  } catch (error) {
    // 加载失败
  }
}

// 监听路由query变化
watch(() => route.query.folderId, (newFolderId) => {
  if (newFolderId && folders.value.length > 0) {
    const targetFolder = folders.value.find(f => f.id === Number(newFolderId))
    if (targetFolder && currentFolderId.value !== targetFolder.id) {
      selectFolder(targetFolder.id)
    }
  }
})

const selectFolder = (folderId: number) => {
  currentFolderId.value = folderId
  pageNo.value = 1
  favorites.value = []
  loadFavorites()
}

const loadFavorites = async () => {
  if (!currentFolderId.value) return
  
  loading.value = true
  try {
    // 使用按收藏夹查询的 API
    const res = await folderApi.getFolderFavorites(currentFolderId.value, {
      pageNo: pageNo.value,
      pageSize,
      type: 'POST'
    })
    if (res.code === 20000 && res.data) {
      const items = res.data.list || []
      favorites.value = items.map((item: FavoriteItem) => item.postItem || item)
      total.value = res.data.total || 0
    }
  } catch (error) {
    // 加载失败
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value || !currentFolderId.value) return
  
  loadingMore.value = true
  pageNo.value++
  try {
    const res = await folderApi.getFolderFavorites(currentFolderId.value, {
      pageNo: pageNo.value,
      pageSize,
      type: 'POST'
    })
    if (res.code === 20000 && res.data) {
      const items = res.data.list || []
      favorites.value.push(...items.map((item: FavoriteItem) => item.postItem || item))
    }
  } catch (error) {
    pageNo.value--
  } finally {
    loadingMore.value = false
  }
}

const handleFolderAction = (command: string, folder: FavoriteFolder) => {
  if (command === 'edit') {
    editingFolder.value = folder
    folderForm.value = {
      name: folder.name,
      description: folder.description || '',
      isPublic: folder.isPublic
    }
    showCreateDialog.value = true
  } else if (command === 'move') {
    // 迁移内容
    selectedSourceFolder.value = folder
    selectFolderAction.value = 'move'
    targetFolderId.value = null
    showSelectFolderDialog.value = true
  } else if (command === 'merge') {
    // 合并收藏夹
    selectedSourceFolder.value = folder
    selectFolderAction.value = 'merge'
    targetFolderId.value = null
    showSelectFolderDialog.value = true
  } else if (command === 'delete') {
    // 构建更详细的确认信息
    let confirmMessage = `确定要删除收藏夹"${folder.name}"吗？`
    
    if (folder.itemCount > 0) {
      confirmMessage = `收藏夹"${folder.name}"中有 ${folder.itemCount} 个收藏内容。\n\n删除后，这些内容将自动移至"默认收藏夹"。\n\n此操作不可撤销，确定要删除吗？`
    }
    
    if (folder.isPublic) {
      confirmMessage += '\n\n注意：此收藏夹是公开的，删除后其他用户将无法访问。'
    }
    
    // 如果收藏夹内容较多,要求输入名称确认
    if (folder.itemCount > 50) {
      ElMessageBox.prompt(
        `${confirmMessage}\n\n为了安全起见，请输入收藏夹名称"${folder.name}"以确认删除：`,
        '删除收藏夹',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          inputPattern: new RegExp(`^${folder.name}$`),
          inputErrorMessage: '收藏夹名称不匹配',
          type: 'warning',
          dangerouslyUseHTMLString: false
        }
      ).then(async () => {
        await performDelete(folder)
      }).catch((error) => {
        // 用户取消操作，不需要处理
      })
    } else {
      ElMessageBox.confirm(
        confirmMessage,
        '删除收藏夹',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        }
      ).then(async () => {
        await performDelete(folder)
      }).catch((error) => {
        // 用户取消操作，不需要处理
      })
    }
  }
}

// 执行删除操作
const performDelete = async (folder: FavoriteFolder) => {
  try {
    const res = await folderApi.deleteFolder(folder.id)
    if (res.code === 20000) {
      ElMessage.success('删除成功')
      const wasCurrentFolder = currentFolderId.value === folder.id
      await loadFolders()
      // 如果删除的是当前选中的收藏夹,自动选择默认收藏夹
      if (wasCurrentFolder && folders.value.length > 0) {
        const defaultFolder = folders.value.find(f => f.isDefault)
        if (defaultFolder) {
          selectFolder(defaultFolder.id)
        } else {
          selectFolder(folders.value[0].id)
        }
      }
    } else {
      ElMessage.error(res.info || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败，请稍后重试')
  }
}

const saveFolder = async () => {
  if (!folderForm.value.name.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  
  saving.value = true
  try {
    if (editingFolder.value) {
      const res = await folderApi.updateFolder(editingFolder.value.id, folderForm.value)
      if (res.code === 20000) {
        ElMessage.success('更新成功')
        showCreateDialog.value = false
        await loadFolders()
        // 如果编辑的是当前选中的收藏夹,刷新内容
        if (currentFolderId.value === editingFolder.value.id) {
          await loadFavorites()
        }
      } else {
        ElMessage.error(res.info || '更新失败')
      }
    } else {
      const res = await folderApi.createFolder(folderForm.value)
      if (res.code === 20000) {
        ElMessage.success('创建成功')
        showCreateDialog.value = false
        await loadFolders()
      } else {
        ElMessage.error(res.info || '创建失败')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  editingFolder.value = null
  folderForm.value = {
    name: '',
    description: '',
    isPublic: false
  }
}

// 确认迁移或合并操作
const confirmFolderAction = async () => {
  if (!targetFolderId.value || !selectedSourceFolder.value) {
    ElMessage.warning('请选择目标收藏夹')
    return
  }
  
  const sourceName = selectedSourceFolder.value.name
  const targetFolder = folders.value.find(f => f.id === targetFolderId.value)
  const targetName = targetFolder?.name || ''
  
  if (selectFolderAction.value === 'move') {
    // 迁移确认
    ElMessageBox.confirm(
      `确定将"${sourceName}"的内容移动到"${targetName}"吗？\n\n源收藏夹将变为空，但不会被删除。`,
      '确认迁移',
      { type: 'info' }
    ).then(async () => {
      await performMove()
    }).catch((error) => {
      // 用户取消操作，不需要处理
      if (error !== 'cancel') {
        // 迁移失败
      }
    })
  } else {
    // 合并确认
    ElMessageBox.confirm(
      `确定将"${sourceName}"合并到"${targetName}"吗？\n\n• "${sourceName}"的内容将移动到"${targetName}"\n• "${sourceName}"将被删除\n• 此操作不可撤销`,
      '确认合并',
      { type: 'warning' }
    ).then(async () => {
      await performMerge()
    }).catch((error) => {
      // 用户取消操作，不需要处理
      if (error !== 'cancel') {
        // 合并失败
      }
    })
  }
}

// 执行迁移
const performMove = async () => {
  if (!selectedSourceFolder.value || !targetFolderId.value) return
  
  actionLoading.value = true
  try {
    const res = await folderApi.moveFolderContents(selectedSourceFolder.value.id, targetFolderId.value)
    if (res.code === 20000) {
      ElMessage.success(res.info || '迁移成功')
      showSelectFolderDialog.value = false
      await loadFolders()
      // 如果当前选中的是源收藏夹，刷新内容
      if (currentFolderId.value === selectedSourceFolder.value.id) {
        await loadFavorites()
      }
    } else {
      ElMessage.error(res.info || '迁移失败')
    }
  } catch (error) {
    ElMessage.error('迁移失败，请稍后重试')
  } finally {
    actionLoading.value = false
  }
}

// 执行合并
const performMerge = async () => {
  if (!selectedSourceFolder.value || !targetFolderId.value) return
  
  actionLoading.value = true
  try {
    const res = await folderApi.mergeFolders(selectedSourceFolder.value.id, targetFolderId.value)
    if (res.code === 20000) {
      ElMessage.success(res.info || '合并成功')
      showSelectFolderDialog.value = false
      const wasCurrentFolder = currentFolderId.value === selectedSourceFolder.value.id
      await loadFolders()
      // 如果删除的是当前选中的收藏夹，切换到目标收藏夹
      if (wasCurrentFolder) {
        selectFolder(targetFolderId.value)
      } else if (currentFolderId.value === targetFolderId.value) {
        // 如果当前选中的是目标收藏夹，刷新内容
        await loadFavorites()
      }
    } else {
      ElMessage.error(res.info || '合并失败')
    }
  } catch (error) {
    ElMessage.error('合并失败，请稍后重试')
  } finally {
    actionLoading.value = false
  }
}
</script>

<style scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

.folder-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  height: fit-content;
}

.folder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 500;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.folder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.folder-item:hover {
  background-color: #f5f5f5;
}

.folder-item.active {
  background-color: #ecf5ff;
}

.folder-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  margin-right: 8px;
}

.folder-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.folder-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-count {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  min-width: 30px;
  text-align: right;
}

.more-icon {
  color: #999;
  opacity: 0;
  transition: opacity 0.2s;
}

.folder-item:hover .more-icon {
  opacity: 1;
}

.disabled-tip {
  font-size: 12px;
  color: #c0c4cc;
  font-style: italic;
}

.favorites-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.loading, .empty {
  padding: 40px;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.select-folder-content {
  padding: 10px 0;
}

.select-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  line-height: 1.6;
}

.folder-radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.folder-radio {
  width: 100%;
  margin: 0;
  padding: 12px;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  transition: all 0.3s;
}

.folder-radio:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.folder-radio.is-checked {
  border-color: #409eff;
  background: #ecf5ff;
}

.folder-radio-content {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.folder-radio-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.folder-radio-count {
  font-size: 12px;
  color: #999;
}
</style>
