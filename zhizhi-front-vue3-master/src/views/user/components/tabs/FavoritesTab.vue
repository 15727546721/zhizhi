<template>
  <div class="favorites-tab">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="folders.length === 0" class="empty">
      <el-empty :description="isOwnProfile ? '暂无收藏夹' : '该用户暂未公开收藏夹'" />
    </div>
    <div v-else class="folders-grid">
      <div
        v-for="folder in folders"
        :key="folder.id"
        class="folder-card"
        @click="viewFolder(folder)"
      >
        <div class="folder-cover">
          <img v-if="folder.coverUrl" :src="folder.coverUrl" alt="封面" />
          <div v-else class="folder-cover-placeholder">
            <el-icon :size="48"><Folder /></el-icon>
          </div>
        </div>
        <div class="folder-info">
          <div class="folder-name">
            {{ folder.name }}
            <el-tag v-if="folder.isDefault" size="small" type="info">默认</el-tag>
          </div>
          <div class="folder-desc">{{ folder.description || '暂无描述' }}</div>
          <div class="folder-meta">
            <span class="folder-count">{{ folder.itemCount }} 个内容</span>
            <span v-if="folder.isPublic" class="folder-public">
              <el-icon><View /></el-icon>
              公开
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Folder, View } from '@element-plus/icons-vue'
import { folderApi, type FavoriteFolder } from '@/api/favorites'

interface Props {
  userId: number | string
  isOwnProfile: boolean
}

const props = defineProps<Props>()
const router = useRouter()

const folders = ref<FavoriteFolder[]>([])
const loading = ref(false)

const loadFolders = async () => {
  loading.value = true
  try {
    const res = props.isOwnProfile
      ? await folderApi.getMyFolders()
      : await folderApi.getUserPublicFolders(Number(props.userId))
    
    if (res.code === 20000 && res.data) {
      folders.value = res.data
    }
  } catch (error) {
    // 加载失败
  } finally {
    loading.value = false
  }
}

const viewFolder = (folder: FavoriteFolder) => {
  if (props.isOwnProfile) {
    // 跳转到收藏管理页,并通过query参数指定收藏夹
    router.push({ path: '/favorites', query: { folderId: folder.id.toString() } })
  } else {
    router.push(`/user/${props.userId}/favorites/${folder.id}`)
  }
}

onMounted(() => {
  loadFolders()
})
</script>

<style scoped>
.favorites-tab {
  padding: 24px;
  min-height: 400px;
}

.loading,
.empty {
  padding: 60px 20px;
}

.folders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.folder-card {
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.folder-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.folder-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.folder-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.folder-cover-placeholder {
  color: #ddd;
}

.folder-info {
  padding: 16px;
}

.folder-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.folder-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.folder-count {
  color: #999;
}

.folder-public {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
}
</style>
