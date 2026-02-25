<template>
  <div class="posts-tab">
    <div v-loading="loading" class="posts-list">
      <PostListItem
        v-for="post in posts"
        :key="post.id"
        :post="post"
        :is-own-profile="isOwnProfile"
        @click="handlePostClick"
        @edit="handleEdit"
        @delete="handleDelete"
      />
      <EmptyState
        v-if="!loading && posts.length === 0"
        description="还没有发布任何帖子"
        :show-action="isOwnProfile"
        action-text="去发布"
        @action="handleCreatePost"
      />
    </div>
    
    <div v-if="posts.length > 0" class="load-more">
      <el-button
        v-if="hasMore && !loading"
        link
        @click="loadMore"
        :loading="loading"
      >
        加载更多
      </el-button>
      <div v-else-if="!hasMore && posts.length > 0" class="no-more">
        没有更多了
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserPosts } from '../../composables/useUserPosts'
import PostListItem from '../list-items/PostListItem.vue'
import EmptyState from '../list-items/EmptyState.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePost } from '@/api/post'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  },
  isOwnProfile: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['post-deleted'])

const router = useRouter()

const {
  posts,
  loading,
  hasMore,
  loadPosts,
  loadMore,
  refresh
} = useUserPosts(props.userId, props.isOwnProfile)

// 监听 userId 变化
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    refresh()
  }
}, { immediate: false })

// 初始化加载
onMounted(() => {
  loadPosts(true)
})

const handlePostClick = (post) => {
  router.push({
    name: 'PostDetail',
    params: { id: post.id }
  })
}

const handleEdit = (post) => {
  router.push({
    name: 'PostEdit',
    params: { id: post.id }
  })
}

const handleDelete = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deletePost(post.id)
    if (res.code === 20000) {
      ElMessage.success('删除成功')
      emit('post-deleted', post)
      refresh()
    } else {
      ElMessage.error(res.info || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 删除失败
    }
  }
}

const handleCreatePost = () => {
  router.push({
    name: 'PostCreate'
  })
}

// 暴露刷新方法
defineExpose({
  refresh
})
</script>

<style scoped>
.posts-tab {
  padding: 20px;
  min-height: 400px;
}

.posts-list {
  min-height: 200px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

.no-more {
  color: #999;
  font-size: 14px;
  padding: 20px 0;
}
</style>

