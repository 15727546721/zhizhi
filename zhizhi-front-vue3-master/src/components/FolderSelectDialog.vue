<template>
  <el-dialog
    v-model="visible"
    title="收藏到"
    width="480px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="folder-select-dialog"
  >
    <div class="dialog-content">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>
      <template v-else>
        <!-- 收藏夹列表 -->
        <div v-if="folders.length > 0" class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            class="folder-item"
            :class="{ selected: selectedFolderId === folder.id }"
            @click="selectFolder(folder.id)"
          >
            <div class="folder-icon">
              <el-icon :size="20">
                <Folder />
              </el-icon>
            </div>
            <div class="folder-info">
              <div class="folder-name-row">
                <span class="folder-name">{{ folder.name }}</span>
                <el-tag v-if="folder.isDefault" size="small" type="info" effect="plain">默认</el-tag>
                <el-tag v-if="folder.isPublic" size="small" type="success" effect="plain">
                  <el-icon :size="12"><View /></el-icon>
                  公开
                </el-tag>
              </div>
              <span class="folder-count">{{ folder.itemCount }} 个内容</span>
            </div>
            <div class="folder-check">
              <el-icon v-if="selectedFolderId === folder.id" class="check-icon" :size="20">
                <CircleCheck />
              </el-icon>
            </div>
          </div>
        </div>
        
        <!-- 无收藏夹提示 -->
        <div v-else class="empty-folders">
          <el-empty description="暂无收藏夹，请先创建一个" :image-size="80" />
        </div>
        
        <!-- 新建收藏夹按钮 -->
        <div v-if="!showCreateForm" class="create-folder-btn" @click="showCreateForm = true">
          <el-icon :size="18"><Plus /></el-icon>
          <span>新建收藏夹</span>
        </div>
        
        <!-- 新建收藏夹表单 -->
        <transition name="slide-fade">
          <div v-if="showCreateForm" class="create-form">
            <div class="create-form-header">
              <span class="create-form-title">新建收藏夹</span>
              <el-icon class="close-icon" @click="showCreateForm = false">
                <Close />
              </el-icon>
            </div>
            <el-input
              v-model="newFolderName"
              placeholder="请输入收藏夹名称"
              maxlength="50"
              show-word-limit
              size="large"
            />
            <div class="create-options">
              <el-checkbox v-model="newFolderPublic">
                <span class="checkbox-label">
                  <el-icon :size="14"><View /></el-icon>
                  公开收藏夹
                </span>
              </el-checkbox>
              <span class="option-tip">公开后其他用户可以查看</span>
            </div>
            <div class="create-actions">
              <el-button @click="showCreateForm = false">取消</el-button>
              <el-button type="primary" @click="createFolder" :loading="creating">
                创建并收藏
              </el-button>
            </div>
          </div>
        </transition>
      </template>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedFolderId">
          确定收藏
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Plus, Folder, View, Close } from '@element-plus/icons-vue'
import { folderApi, type FavoriteFolder } from '@/api/favorites'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [folderId: number]
}>()

const visible = ref(props.modelValue)
const loading = ref(false)
const folders = ref<FavoriteFolder[]>([])
const selectedFolderId = ref<number | null>(null)
const showCreateForm = ref(false)
const newFolderName = ref('')
const newFolderPublic = ref(false)
const creating = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadFolders()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadFolders = async () => {
  loading.value = true
  try {
    const res = await folderApi.getMyFolders()
    if (res.code === 20000 && res.data) {
      folders.value = res.data
      // 默认选中默认收藏夹
      const defaultFolder = folders.value.find(f => f.isDefault)
      if (defaultFolder) {
        selectedFolderId.value = defaultFolder.id
      }
    }
  } catch (error) {
    console.error('加载收藏夹失败:', error)
    ElMessage.error('加载收藏夹失败')
  } finally {
    loading.value = false
  }
}

const selectFolder = (id: number) => {
  selectedFolderId.value = id
}

const createFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  
  creating.value = true
  try {
    const res = await folderApi.createFolder({
      name: newFolderName.value.trim(),
      isPublic: newFolderPublic.value
    })
    if (res.code === 20000 && res.data) {
      folders.value.push(res.data)
      selectedFolderId.value = res.data.id
      showCreateForm.value = false
      newFolderName.value = ''
      newFolderPublic.value = false
      ElMessage.success('创建成功')
    } else {
      ElMessage.error(res.info || '创建失败')
    }
  } catch (error) {
    console.error('创建收藏夹失败:', error)
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const handleClose = () => {
  visible.value = false
  showCreateForm.value = false
  newFolderName.value = ''
}

const handleConfirm = () => {
  if (selectedFolderId.value) {
    emit('confirm', selectedFolderId.value)
    handleClose()
  }
}
</script>

<style scoped>
.folder-select-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.folder-select-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
}

.folder-select-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-content {
  min-height: 200px;
  max-height: 500px;
}

.loading {
  padding: 40px 24px;
}

.empty-folders {
  padding: 40px 24px;
  text-align: center;
}

.folder-list {
  max-height: 360px;
  overflow-y: auto;
  padding: 12px 0;
}

.folder-list::-webkit-scrollbar {
  width: 6px;
}

.folder-list::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.folder-list::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.folder-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: #409eff;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.folder-item:hover {
  background-color: #f5f7fa;
}

.folder-item.selected {
  background-color: #ecf5ff;
}

.folder-item.selected::before {
  opacity: 1;
}

.folder-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: #f0f2f5;
  color: #606266;
  transition: all 0.2s ease;
}

.folder-item.selected .folder-icon {
  background-color: #409eff;
  color: #fff;
}

.folder-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.folder-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.folder-name {
  font-size: 15px;
  font-weight: 500;
  color: #1f2329;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-count {
  font-size: 13px;
  color: #8a8f99;
}

.folder-check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.check-icon {
  color: #409eff;
  animation: checkIn 0.3s ease;
}

@keyframes checkIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.create-folder-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  margin: 8px 24px;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: 1px dashed #d9ecff;
  border-radius: 8px;
  background-color: #f4f9ff;
  transition: all 0.2s ease;
}

.create-folder-btn:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.create-form {
  padding: 20px 24px;
  margin: 12px 24px;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background-color: #fafafa;
}

.create-form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.create-form-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
}

.close-icon {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s ease;
}

.close-icon:hover {
  color: #606266;
}

.create-options {
  margin-top: 12px;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
}

.checkbox-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.option-tip {
  display: block;
  margin-top: 8px;
  margin-left: 24px;
  font-size: 12px;
  color: #909399;
}

.create-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画效果 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateY(-10px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
