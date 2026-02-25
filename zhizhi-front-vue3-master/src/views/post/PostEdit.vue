<template>
  <div class="post-edit">
    <div class="content-layout">
      <!-- 主编辑区域 -->
      <div class="main-content">
        <div class="edit-card">
          <div class="card-header">
            <div class="title-area">
              <h2>{{ isEdit ? '编辑帖子' : '发布新帖子' }}</h2>
              <!-- 自动保存状态指示器 -->
              <span class="auto-save-status" v-if="autoSaveStatus || lastSaveTime">
                <template v-if="autoSaveStatus === 'saving'">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>正在保存...</span>
                </template>
                <template v-else-if="autoSaveStatus === 'saved'">
                  <el-icon class="saved-icon"><CircleCheck /></el-icon>
                  <span>已自动保存</span>
                </template>
                <template v-else-if="autoSaveStatus === 'error'">
                  <el-icon class="error-icon"><CircleClose /></el-icon>
                  <span>保存失败（已备份到本地）</span>
                </template>
                <template v-else-if="lastSaveTime">
                  <span class="last-save-time">上次保存: {{ formatTime(lastSaveTime) }}</span>
                </template>
              </span>
            </div>
            <div class="header-actions">
              <el-button link @click="goBack">
                <el-icon><ArrowLeft /></el-icon>
                返回
              </el-button>
              <el-button @click="togglePreview" type="primary" plain>
                <el-icon><View /></el-icon>
                {{ previewVisible ? '关闭预览' : '预览' }}
              </el-button>
            </div>
          </div>
          
          <el-form 
            ref="postFormRef" 
            :model="postForm" 
            :rules="rules" 
            label-position="top"
            class="post-form"
          >
            <!-- 标题 -->
            <el-form-item label="标题" prop="title">
              <el-input 
                v-model="postForm.title" 
                placeholder="请输入帖子标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 描述 -->
            <el-form-item label="描述" prop="description">
              <el-input
                v-model="postForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入帖子描述（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 内容 -->
            <el-form-item label="内容" prop="content">
              <post-content-editor
                v-model="postForm.content"
                v-model:mode="contentMode"
                :rows="15"
                :maxlength="5000"
                :placeholder="'请输入帖子内容'"
                :preview="false"
                :editor-height="'480px'"
                :upload-images="uploadImages"
              />
            </el-form-item>
            
            <!-- 标签 -->
            <el-form-item label="标签">
              <div class="tag-input-area">
                <!-- 已选标签展示 -->
                <div class="selected-tags" v-if="postForm.tags.length > 0">
                  <span class="selected-label">已选标签：</span>
                  <el-tag
                    v-for="tag in postForm.tags"
                    :key="tag"
                    closable
                    @close="removeTag(tag)"
                    class="tag-item"
                    effect="dark"
                    round
                  >
                    <el-icon class="tag-icon"><PriceTag /></el-icon>
                    {{ tag }}
                  </el-tag>
                </div>
                <div v-else class="no-tags-tip">
                  请从下方列表选择标签
                </div>
                
                <!-- 标签选择器 -->
                <div class="tag-selector">
                  <div class="selector-header">
                    <span class="selector-title">选择标签</span>
                    <span class="tag-count">{{ postForm.tags.length }}/5</span>
                  </div>
                  <div class="tag-options">
                    <div 
                      v-for="tag in tags" 
                      :key="tag.id"
                      class="tag-option"
                      :class="{ 'is-selected': postForm.tags.includes(tag.name) }"
                      @click="toggleTag(tag)"
                    >
                      <el-icon class="option-icon"><PriceTag /></el-icon>
                      <span class="option-name">{{ tag.name }}</span>
                      <el-icon v-if="postForm.tags.includes(tag.name)" class="check-icon"><Check /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
            
            <!-- 专栏选择 -->
            <el-form-item label="添加到专栏（可选）">
              <div class="column-select-area">
                <!-- 已选专栏展示 -->
                <div class="selected-columns" v-if="selectedColumns.length > 0">
                  <span class="selected-label">已选专栏：</span>
                  <el-tag
                    v-for="column in selectedColumns"
                    :key="column.id"
                    closable
                    @close="removeColumn(column.id)"
                    class="column-tag"
                    type="success"
                    round
                  >
                    <el-icon class="tag-icon"><Collection /></el-icon>
                    {{ column.name }}
                  </el-tag>
                </div>
                <div v-else class="no-columns-tip">
                  可将文章添加到专栏（最多3个）
                </div>
                
                <el-button 
                  type="primary" 
                  plain 
                  size="small"
                  @click="showColumnSelectDialog = true"
                  :disabled="selectedColumns.length >= 3"
                >
                  <el-icon><Plus /></el-icon>
                  选择专栏
                </el-button>
              </div>
            </el-form-item>
            
            <!-- 封面图 -->
            <el-form-item label="封面图">
              <div class="cover-upload">
                <el-upload
                  class="cover-uploader"
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleCoverUpload"
                >
                  <img v-if="postForm.coverUrl" :src="postForm.coverUrl" class="cover" />
                  <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
                  <div v-if="!postForm.coverUrl" class="cover-text">上传封面图</div>
                </el-upload>
                <el-button 
                  v-if="postForm.coverUrl" 
                  @click="removeCover" 
                  type="danger" 
                  size="small"
                  plain
                >
                  删除封面
                </el-button>
              </div>
            </el-form-item>
            
            <!-- 操作按钮 -->
            <div class="form-actions">
              <el-button @click="saveAsDraft" :loading="draftLoading">
                存为草稿
              </el-button>
              <el-button type="primary" @click="submitPost" :loading="submitLoading">
                {{ isEdit ? '更新帖子' : '发布帖子' }}
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
      
      <!-- 右侧信息栏 -->
      <div class="sidebar">
        <div class="sidebar-widget">
          <h3 class="widget-title">发帖提示</h3>
          <div class="tips-content">
            <p><strong>标题要求：</strong>简洁明了，能准确概括内容</p>
            <p><strong>内容规范：</strong>请遵守社区规范，文明发言</p>
            <p><strong>标签建议：</strong>添加相关标签便于他人查找</p>
            <p><strong>封面图片：</strong>建议使用清晰、相关的图片</p>
          </div>
        </div>
        
        <div class="sidebar-widget">
          <h3 class="widget-title">写作技巧</h3>
          <ul class="tips-list">
            <li>使用段落分隔，使内容更易读</li>
            <li>添加相关链接或参考资料</li>
            <li>使用表情符号增加趣味性</li>
            <li>添加图片或视频丰富内容</li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 预览模态框 -->
    <el-dialog
      v-model="previewVisible"
      title="帖子预览"
      width="80%"
      class="post-preview-dialog"
      :before-close="togglePreview"
    >
      <post-preview 
        :post-data="postForm" 
        :mode="contentMode"
        @close="togglePreview"
      />
    </el-dialog>

    <!-- 专栏选择对话框 -->
    <ColumnSelectDialog
      v-model="showColumnSelectDialog"
      :selected="postForm.columnIds"
      @confirm="handleColumnSelect"
      @create="handleCreateColumn"
    />

    <!-- 专栏创建对话框 -->
    <ColumnFormDialog
      v-model="showColumnCreateDialog"
      @success="handleColumnCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus, View, PriceTag, Check, Loading, CircleCheck, CircleClose, Collection } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { 
  createPost, 
  updatePost, 
  saveDraft, 
  publishDraft, 
  getPostDetail,
  uploadPostCover,
  PostCreateRequest,
  PostUpdateRequest,
  PostStatus
} from '@/api/post'
// import { getCategoryPage } from '@/api/category' // 已移除，使用标签代替分类
import { getTagPage } from '@/api/tag'
import PostContentEditor from './components/PostContentEditor.vue'
import PostPreview from './components/PostPreview.vue'
import ColumnSelectDialog from '@/components/column/ColumnSelectDialog.vue'
import ColumnFormDialog from '@/components/column/ColumnFormDialog.vue'
import type { ColumnVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import { validateApiResponse } from '@/utils/typeGuards'

// 类型定义
interface TagOption {
  id: number
  name: string
}

interface PostFormData {
  id: number | null
  title: string
  content: string
  description: string
  tags: string[]
  tagIds: number[]
  columnIds: number[]
  coverUrl: string
  status: string
}

interface DraftBackup {
  id: number | null
  title: string
  content: string
  description: string
  tagIds: number[]
  tags: string[]
  coverUrl: string
  timestamp: number
}

// 响应类型定义
interface SaveDraftResponse {
  id?: number
}

interface PostDetailResponse {
  id: number
  title: string
  content: string
  description: string
  tags?: Array<{ id: number; name: string }>
  coverUrl?: string
  status: string
}

interface UploadCoverResponse {
  urls?: string[]
}

defineOptions({
  name: 'PostEdit'
})

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = ref(!!route.params.id)
const postId = ref(route.params.id ? parseInt(route.params.id as string) : null)

// 表单引用
const postFormRef = ref<FormInstance | null>(null)

// 表单数据
const postForm = ref<PostFormData>({
  id: null,
  title: '',
  content: '',
  description: '',
  tags: [],
  tagIds: [],
  columnIds: [],
  coverUrl: '',
  status: PostStatus.DRAFT
})

// 标签输入
const newTag = ref('')
const selectedTag = ref<TagOption | null>(null)

// 加载状态
const draftLoading = ref(false)
const submitLoading = ref(false)
const loading = ref(false)

// 标签选项
const tags = ref<TagOption[]>([])

// 专栏相关
const columnStore = useColumnStore()
const showColumnSelectDialog = ref(false)
const showColumnCreateDialog = ref(false)
const selectedColumns = ref<ColumnVO[]>([])

// 内容编辑模式：text | markdown
const contentMode = ref<'text' | 'markdown'>('text')

// 预览可见性
const previewVisible = ref(false)

// ==================== 自动保存相关 ====================
const autoSaveTimer = ref<ReturnType<typeof setInterval> | null>(null)
const debounceTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const lastSavedContent = ref('')  // 上次保存的内容快照
const autoSaveStatus = ref<'saving' | 'saved' | 'error' | ''>('')    // 保存状态
const lastSaveTime = ref<number | null>(null)    // 上次保存时间
const AUTO_SAVE_INTERVAL = 30000  // 30秒定时自动保存
const DEBOUNCE_DELAY = 3000       // 3秒防抖延迟
const LOCAL_STORAGE_KEY = 'zhizhi_draft_backup'  // 本地存储key

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度应在1-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 1, message: '内容至少1个字符', trigger: 'blur' }
  ]
}

// 添加标签
const addTag = (): void => {
  if (!newTag.value.trim()) return
  
  const tag = newTag.value.trim()
  if (postForm.value.tags.includes(tag)) {
    ElMessage.warning('该标签已存在')
    return
  }
  
  if (postForm.value.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个标签')
    return
  }
  
  postForm.value.tags.push(tag)
  newTag.value = ''
}

// 删除标签
const removeTag = (tag: string): void => {
  postForm.value.tags = postForm.value.tags.filter(t => t !== tag)
}

// 选择标签
const selectTag = (tag: TagOption | null): void => {
  if (!tag) return
  
  if (postForm.value.tags.includes(tag.name)) {
    ElMessage.warning('该标签已存在')
    return
  }
  
  if (postForm.value.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个标签')
    return
  }
  
  postForm.value.tags.push(tag.name)
  postForm.value.tagIds.push(tag.id)
  selectedTag.value = null // 清空选择
}

// 切换标签选中状态
const toggleTag = (tag: TagOption): void => {
  const index = postForm.value.tags.indexOf(tag.name)
  if (index > -1) {
    // 已选中，取消选择
    postForm.value.tags.splice(index, 1)
    const idIndex = postForm.value.tagIds.indexOf(tag.id)
    if (idIndex > -1) {
      postForm.value.tagIds.splice(idIndex, 1)
    }
  } else {
    // 未选中，添加
    if (postForm.value.tags.length >= 5) {
      ElMessage.warning('最多只能添加5个标签')
      return
    }
    postForm.value.tags.push(tag.name)
    postForm.value.tagIds.push(tag.id)
  }
}

// 专栏相关方法
const handleColumnSelect = (columnIds: number[]) => {
  postForm.value.columnIds = columnIds
  // 更新selectedColumns显示
  selectedColumns.value = columnStore.myColumns.filter(c => columnIds.includes(c.id))
}

const removeColumn = (columnId: number) => {
  postForm.value.columnIds = postForm.value.columnIds.filter(id => id !== columnId)
  selectedColumns.value = selectedColumns.value.filter(c => c.id !== columnId)
}

const handleCreateColumn = () => {
  showColumnSelectDialog.value = false
  showColumnCreateDialog.value = true
}

const handleColumnCreated = () => {
  // 专栏创建成功后,重新打开选择对话框
  showColumnCreateDialog.value = false
  showColumnSelectDialog.value = true
}

// 处理封面上传
const handleCoverUpload = async (uploadFile: UploadFile): Promise<void> => {
  try {
    if (!uploadFile.raw) return
    const response = await uploadPostCover(uploadFile.raw)
    const data = validateApiResponse<UploadCoverResponse>(response)
    if (data && data.urls && data.urls.length > 0) {
      postForm.value.coverUrl = data.urls[0]
      ElMessage.success('封面上传成功')
    }
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
}

// 删除封面
const removeCover = (): void => {
  postForm.value.coverUrl = ''
  ElMessage.success('封面已删除')
}

// 返回上一页
const goBack = (): void => {
  router.go(-1)
}

// 切换预览
const togglePreview = (): void => {
  previewVisible.value = !previewVisible.value
}

// 获取当前内容快照（用于检测变化）
const getContentSnapshot = (): string => {
  return JSON.stringify({
    title: postForm.value.title,
    content: postForm.value.content,
    description: postForm.value.description,
    tagIds: postForm.value.tagIds,
    coverUrl: postForm.value.coverUrl
  })
}

// 检查内容是否有变化
const hasContentChanged = (): boolean => {
  return getContentSnapshot() !== lastSavedContent.value
}

// 保存到本地存储（备份）
const saveToLocalStorage = (): void => {
  try {
    const backupData: DraftBackup = {
      id: postForm.value.id,
      title: postForm.value.title,
      content: postForm.value.content,
      description: postForm.value.description,
      tagIds: postForm.value.tagIds,
      tags: postForm.value.tags,
      coverUrl: postForm.value.coverUrl,
      timestamp: Date.now()
    }
    localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(backupData))
  } catch (e) {
    // 保存失败
  }
}

// 从本地存储恢复
const loadFromLocalStorage = (): DraftBackup | null => {
  try {
    const backup = localStorage.getItem(LOCAL_STORAGE_KEY)
    if (backup) {
      return JSON.parse(backup) as DraftBackup
    }
  } catch (e) {
    // 读取失败
  }
  return null
}

// 清除本地存储备份
const clearLocalStorage = (): void => {
  try {
    localStorage.removeItem(LOCAL_STORAGE_KEY)
  } catch (e) {
    // 清除失败
  }
}

// 检查并恢复本地草稿
const checkAndRestoreLocalDraft = async (): Promise<void> => {
  // 编辑模式下不恢复本地草稿
  if (isEdit.value) return
  
  const backup = loadFromLocalStorage()
  if (!backup) return
  
  // 检查备份是否过期（超过24小时）
  const expireTime = 24 * 60 * 60 * 1000
  if (Date.now() - backup.timestamp > expireTime) {
    clearLocalStorage()
    return
  }
  
  // 检查备份是否有内容
  if (!backup.title && !backup.content) {
    clearLocalStorage()
    return
  }
  
  // 提示用户是否恢复
  try {
    await ElMessageBox.confirm(
      `检测到您有未保存的草稿（${formatTime(backup.timestamp)}），是否恢复？`,
      '恢复草稿',
      {
        confirmButtonText: '恢复',
        cancelButtonText: '放弃',
        type: 'info'
      }
    )
    
    // 恢复草稿
    postForm.value.id = backup.id || null
    postForm.value.title = backup.title || ''
    postForm.value.content = backup.content || ''
    postForm.value.description = backup.description || ''
    postForm.value.tagIds = backup.tagIds || []
    postForm.value.tags = backup.tags || []
    postForm.value.coverUrl = backup.coverUrl || ''
    
    ElMessage.success('草稿已恢复')
  } catch {
    // 用户选择放弃，清除本地备份
    clearLocalStorage()
  }
}

// 格式化时间
const formatTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 执行自动保存（服务端）
const performAutoSave = async (): Promise<void> => {
  // 检查是否有内容可保存
  if (!postForm.value.title.trim() && !postForm.value.content.trim()) {
    return
  }
  
  // 检查内容是否有变化
  if (!hasContentChanged()) {
    return
  }
  
  autoSaveStatus.value = 'saving'
  
  try {
    const requestData = {
      id: postForm.value.id,  // 如果有ID则更新，否则创建
      title: postForm.value.title,
      content: postForm.value.content,
      description: postForm.value.description,
      tagIds: postForm.value.tagIds,
      coverUrl: postForm.value.coverUrl
    }
    
    const response = await saveDraft(requestData as PostCreateRequest)
    const data = validateApiResponse<SaveDraftResponse>(response)
    
    // 如果是新创建的草稿，保存返回的ID
    if (!postForm.value.id && data && data.id) {
      postForm.value.id = data.id
    }
    
    // 更新保存快照和时间
    lastSavedContent.value = getContentSnapshot()
    lastSaveTime.value = Date.now()
    autoSaveStatus.value = 'saved'
    
    // 服务端保存成功后清除本地备份
    clearLocalStorage()
    
    // 3秒后清除状态
    setTimeout(() => {
      if (autoSaveStatus.value === 'saved') {
        autoSaveStatus.value = ''
      }
    }, 3000)
  } catch (error) {
    autoSaveStatus.value = 'error'
    
    // 服务端保存失败时，保存到本地
    saveToLocalStorage()
  }
}

// 防抖保存（内容变化时触发）
const debouncedSave = (): void => {
  // 先保存到本地（即时备份）
  if (postForm.value.title.trim() || postForm.value.content.trim()) {
    saveToLocalStorage()
  }
  
  // 清除之前的防抖定时器
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }
  
  // 设置新的防抖定时器
  debounceTimer.value = setTimeout(() => {
    performAutoSave()
  }, DEBOUNCE_DELAY)
}

// 监听表单变化，触发防抖保存
watch(
  () => [postForm.value.title, postForm.value.content, postForm.value.description],
  () => {
    debouncedSave()
  },
  { deep: true }
)

// 监听标签变化
watch(
  () => postForm.value.tagIds,
  () => {
    debouncedSave()
  },
  { deep: true }
)

// 启动定时自动保存
const startAutoSave = (): void => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value)
  }
  autoSaveTimer.value = setInterval(performAutoSave, AUTO_SAVE_INTERVAL)
}

// 停止自动保存定时器
const stopAutoSave = (): void => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value)
    autoSaveTimer.value = null
  }
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
    debounceTimer.value = null
  }
}

// 保存为草稿（手动）
const saveAsDraft = async (): Promise<void> => {
  if (!postForm.value.title.trim() && !postForm.value.content.trim()) {
    ElMessage.warning('请填写标题或内容')
    return
  }
  
  draftLoading.value = true
  
  try {
    const requestData = {
      id: postForm.value.id,
      title: postForm.value.title,
      content: postForm.value.content,
      description: postForm.value.description,
      tagIds: postForm.value.tagIds,
      coverUrl: postForm.value.coverUrl
    }
    
    const response = await saveDraft(requestData as PostCreateRequest)
    const data = validateApiResponse<SaveDraftResponse>(response)
    
    // 如果是新创建的草稿，保存返回的ID
    if (!postForm.value.id && data && data.id) {
      postForm.value.id = data.id
    }
    
    // 更新保存快照
    lastSavedContent.value = getContentSnapshot()
    
    // 清除本地备份
    clearLocalStorage()
    
    ElMessage.success('草稿保存成功')
    // 跳转到草稿箱
    router.push('/draft')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    draftLoading.value = false
  }
}

// 提交帖子
const submitPost = async (): Promise<void> => {
  if (!postFormRef.value) return
  
  postFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitLoading.value = true
    
    try {
      let response
      if (isEdit.value) {
        // 更新帖子
        const requestData = {
          id: postForm.value.id,
          title: postForm.value.title,
          content: postForm.value.content,
          description: postForm.value.description,
          tagIds: postForm.value.tagIds,
          coverUrl: postForm.value.coverUrl,
          columnIds: postForm.value.columnIds
        }
        response = await updatePost(postId.value!, requestData as PostUpdateRequest)
      } else {
        // 创建新帖子
        const requestData = {
          title: postForm.value.title,
          content: postForm.value.content,
          description: postForm.value.description,
          tagIds: postForm.value.tagIds,
          coverUrl: postForm.value.coverUrl,
          columnIds: postForm.value.columnIds
        }
        response = await createPost(requestData as PostCreateRequest)
      }

      // 发布成功后清除本地备份
      clearLocalStorage()
      
      ElMessage.success(`${isEdit.value ? '更新' : '发布'}成功`)
      router.push('/')
    } catch (error) {
      ElMessage.error(`${isEdit.value ? '更新' : '发布'}失败`)
    } finally {
      submitLoading.value = false
    }
  })
}

// 加载标签数据（分类已移除）
const loadCategoriesAndTags = async (): Promise<void> => {
  try {
    const tagsRes = await getTagPage()
    tags.value = (tagsRes?.data as TagOption[]) || []
  } catch (error) {
    // 加载失败
  }
}

// 初始化数据（编辑模式下）
const initEditData = async (): Promise<void> => {
  if (isEdit.value && postId.value) {
    loading.value = true
    try {
      const response = await getPostDetail(postId.value)
      const postData = validateApiResponse<PostDetailResponse>(response)
      
      if (!postData) {
        ElMessage.error('获取帖子详情失败')
        router.push('/')
        return
      }
      
      // 处理标签数据：后端返回的是 TagVO 数组 [{id, name}, ...]
      // 前端需要分别存储 tags (名称数组) 和 tagIds (ID数组)
      let tagNames: string[] = []
      let tagIds: number[] = []
      if (postData.tags && Array.isArray(postData.tags)) {
        tagNames = postData.tags.map((tag) => tag.name)
        tagIds = postData.tags.map((tag) => tag.id)
      }
      
      postForm.value = {
        id: postData.id,
        title: postData.title || '',
        content: postData.content || '',
        description: postData.description || '',
        tags: tagNames,
        tagIds: tagIds,
        columnIds: [],
        coverUrl: postData.coverUrl || '',
        status: postData.status || PostStatus.DRAFT
      }
    } catch (error) {
      ElMessage.error('获取帖子详情失败')
      router.push('/')
    } finally {
      loading.value = false
    }
  }
}

onMounted(async () => {
  await loadCategoriesAndTags()
  await initEditData()
  
  if (route.query.title) {
    postForm.value.title = route.query.title as string
  }
  
  // 检查并恢复本地草稿（仅新建模式）
  if (!isEdit.value) {
    await checkAndRestoreLocalDraft()
  }
  
  // 初始化保存快照并启动自动保存
  lastSavedContent.value = getContentSnapshot()
  startAutoSave()
})

// 组件卸载时停止自动保存
onUnmounted(() => {
  stopAutoSave()
})

// 页面离开前提示（如果有未保存的内容）
onBeforeUnmount(() => {
  // 如果有未保存的变更，保存到本地
  if (hasContentChanged() && (postForm.value.title.trim() || postForm.value.content.trim())) {
    saveToLocalStorage()
  }
})

// 图片上传（用于 Markdown 编辑器）
const uploadImages = async (files: File[]): Promise<string[]> => {
  const urls: string[] = []
  for (const file of files) {
    try {
      const res = await uploadPostCover(file)
      const data = validateApiResponse<UploadCoverResponse>(res)
      if (data && data.urls) {
        urls.push(...data.urls)
      }
    } catch (error) {
      // 上传失败
    }
  }
  return urls
}

// ... (其他代码保持不变)
</script>

<style scoped>
.post-edit {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - var(--header-height));
  box-sizing: border-box;
}

.content-layout {
  display: flex;
  gap: 20px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}

.edit-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

/* 标题区域 */
.title-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 自动保存状态 */
.auto-save-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  padding: 4px 10px;
  background: #f4f4f5;
  border-radius: 4px;
}

.auto-save-status .is-loading {
  animation: rotate 1s linear infinite;
  color: #409eff;
}

.auto-save-status .saved-icon {
  color: #67c23a;
}

.auto-save-status .error-icon {
  color: #f56c6c;
}

.auto-save-status .last-save-time {
  color: #909399;
  font-size: 12px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.post-form {
  width: 100%;
  max-width: 100%;
}

.post-form :deep(.el-form-item__content) {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.post-form :deep(.el-form-item__content > *) {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 标签选择区域 */
.tag-input-area {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

/* 已选标签展示 */
.selected-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e4e7ed;
}

.selected-label {
  font-size: 14px;
  color: #606266;
  margin-right: 4px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 13px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.tag-item .tag-icon {
  font-size: 14px;
}

.no-tags-tip {
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
}

/* 标签选择器 */
.tag-selector {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.selector-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.tag-count {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px 0;
}

.tag-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
}

.tag-option:hover {
  color: #409eff;
  background: #ecf5ff;
  border-color: #b3d8ff;
  transform: translateY(-1px);
}

.tag-option.is-selected {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.tag-option.is-selected:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.tag-option .option-icon {
  font-size: 14px;
}

.tag-option .option-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-option .check-icon {
  font-size: 14px;
  margin-left: 2px;
}

/* 封面上传 */
.cover-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 200px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.cover-text {
  font-size: 14px;
  color: #606266;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

/* 右侧信息栏 */
.sidebar {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + 20px);
  align-self: flex-start;
  box-sizing: border-box;
}

.sidebar-widget {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.widget-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #2c3e50;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.tips-content p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.6;
}

.tips-content strong {
  color: #303133;
}

.tips-list {
  padding-left: 20px;
}

.tips-list li {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
}

/* 预览对话框样式 */
.post-preview-dialog {
  max-height: 80vh;
}

.post-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 70vh;
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .post-edit {
    max-width: 960px;
    padding: 20px 16px;
  }
}

@media screen and (max-width: 992px) {
  .post-edit {
    max-width: 720px;
  }
}

@media screen and (max-width: 768px) {
  .content-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    position: static;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>


/* 专栏选择区域样式 */
.column-select-area {
  .selected-columns {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
  }

  .selected-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }

  .column-tag {
    display: flex;
    align-items: center;
    gap: 4px;

    .tag-icon {
      font-size: 14px;
    }
  }

  .no-columns-tip {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    color: #909399;
    font-size: 14px;
    margin-bottom: 12px;
  }
}
