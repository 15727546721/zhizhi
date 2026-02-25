<template>
  <div class="user-favorite-folder">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <div v-if="folder" class="folder-header">
        <h2>{{ folder.name }}</h2>
        <p v-if="folder.description" class="folder-description">{{ folder.description }}</p>
        <div class="folder-meta">
          <span>{{ folder.itemCount }} 个内容</span>
          <span v-if="folder.isPublic" class="public-tag">
            <el-icon><View /></el-icon>
            公开
          </span>
        </div>
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error">
      <el-empty :description="error" />
    </div>
    <div v-else-if="favorites.length === 0" class="empty">
      <el-empty description="该收藏夹暂无内容" />
    </div>
    <div v-else class="favorites-list">
      <PostList :posts="favorites" />
      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, View } from '@element-plus/icons-vue'
import { folderApi, type FavoriteFolder } from '@/api/favorites'
import PostList from '@/views/home/components/PostList.vue'
import type { FavoriteItem } from '@/types/api'

const route = useRoute()
const router = useRouter()

const userId = computed(() => Number(route.params.userId))
const folderId = computed(() => Number(route.params.folderId))

const folder = ref<FavoriteFolder | null>(null)
const favorites = ref<FavoriteItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const pageNo = ref(1)
const pageSize = 10
const total = ref(0)

const hasMore = computed(() => favorites.value.length < total.value)

const loadFolder = async () => {
  try {
    const res = await folderApi.getFolderDetail(folderId.value)
    if (res.code === 20000 && res.data) {
      folder.value = res.data
    } else {
      error.value = res.info || '加载收藏夹失败'
    }
  } catch (err) {
    error.value = '加载收藏夹失败'
  }
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await folderApi.getFolderFavorites(folderId.value, {
      pageNo: pageNo.value,
      pageSize,
      type: 'POST'
    })
    if (res.code === 20000 && res.data) {
      const items = res.data.list || []
      favorites.value = items.map((item: FavoriteItem) => item.postItem || item)
      total.value = res.data.total || 0
    } else {
      error.value = res.info || '加载收藏内容失败'
    }
  } catch (err) {
    error.value = '加载收藏内容失败'
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  loadingMore.value = true
  pageNo.value++
  try {
    const res = await folderApi.getFolderFavorites(folderId.value, {
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

const goBack = () => {
  router.push(`/user/${userId.value}`)
}

onMounted(async () => {
  await loadFolder()
  await loadFavorites()
})
</script>

<style scoped>
.user-favorite-folder {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.folder-header {
  margin-top: 16px;
}

.folder-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.folder-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.folder-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.public-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
}

.loading,
.error,
.empty {
  padding: 60px 20px;
}

.favorites-list {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.load-more {
  text-align: center;
  padding: 20px;
}
</style>
