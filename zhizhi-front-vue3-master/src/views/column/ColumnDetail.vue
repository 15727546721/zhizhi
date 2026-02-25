<template>
  <div v-if="column" class="column-detail">
    <!-- 专栏头部 -->
    <div class="column-header">
      <div class="header-bg" :style="{ backgroundImage: `url(${column.coverUrl})` }"></div>
      <div class="header-content">
        <div class="column-info">
          <h1 class="column-name">{{ column.name }}</h1>
          <p v-if="column.description" class="column-desc">{{ column.description }}</p>

          <div class="column-meta">
            <router-link :to="`/user/${column.userId}`" class="author-link">
              <img :src="column.userAvatar" :alt="column.userName" class="author-avatar" />
              <span class="author-name">{{ column.userName }}</span>
            </router-link>

            <div class="stats">
              <span class="stat-item">
                <el-icon><Document /></el-icon>
                {{ column.postCount }} 篇文章
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ column.subscribeCount }} 订阅
              </span>
            </div>
          </div>
        </div>

        <div class="column-actions">
          <el-button
            v-if="column.isOwner"
            type="primary"
            @click="router.push(`/columns/${column.id}/manage`)"
          >
            <el-icon><Setting /></el-icon>
            管理专栏
          </el-button>
          <el-button
            v-else-if="!column.isSubscribed"
            type="primary"
            @click="handleSubscribe"
          >
            <el-icon><Plus /></el-icon>
            订阅
          </el-button>
          <el-button v-else @click="handleUnsubscribe">
            <el-icon><Check /></el-icon>
            已订阅
          </el-button>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="column-posts">
      <div class="posts-header">
        <h2>专栏文章</h2>
        <span class="post-count">共 {{ column.postCount }} 篇</span>
      </div>

      <el-skeleton v-if="loading" :rows="5" animated />

      <div v-else-if="posts.length > 0" class="posts-list">
        <div v-for="(post, index) in posts" :key="post.postId" class="post-item">
          <div class="post-index">{{ (currentPage - 1) * pageSize + index + 1 }}</div>
          <div class="post-content">
            <router-link :to="`/posts/${post.postId}`" class="post-title">
              {{ post.title }}
            </router-link>
            <p v-if="post.description" class="post-desc">{{ post.description }}</p>
            <div class="post-meta">
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ post.viewCount }}
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount }}
              </span>
              <span class="meta-item">
                <el-icon><Star /></el-icon>
                {{ post.likeCount }}
              </span>
              <span class="meta-item time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <img
            v-if="post.coverUrl"
            :src="post.coverUrl"
            :alt="post.title"
            class="post-cover"
          />
        </div>
      </div>

      <el-empty v-else description="暂无文章" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        class="pagination"
        @current-change="handlePageChange"
      />
    </div>
  </div>

  <el-skeleton v-else :rows="8" animated />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Document,
  Star,
  Setting,
  Plus,
  Check,
  View,
  ChatDotRound,
} from '@element-plus/icons-vue'
import type { ColumnDetailVO, ColumnPostVO } from '@/types/column'
import { useColumnStore } from '@/stores/module/column'
import * as columnApi from '@/api/column'
import { formatTime } from '@/utils/time'

const route = useRoute()
const router = useRouter()
const columnStore = useColumnStore()

const column = ref<ColumnDetailVO | null>(null)
const posts = ref<ColumnPostVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => {
  loadColumnDetail()
  loadPosts()
})

const loadColumnDetail = async () => {
  const columnId = Number(route.params.id)
  try {
    const detail = await columnStore.fetchColumnDetail(columnId)
    if (detail) {
      column.value = detail
    }
  } catch (error) {
    router.push('/columns')
  }
}

const loadPosts = async () => {
  const columnId = Number(route.params.id)
  try {
    loading.value = true
    const res = await columnApi.getColumnPosts(columnId, currentPage.value, pageSize.value)
    if (res.code === 20000 && res.data) {
      // 后端返回的是 PageResponse 结构：{ pageNo, pageSize, total, data }
      posts.value = res.data.data
      total.value = res.data.total
    }
  } catch (error) {
    // 加载失败
  } finally {
    loading.value = false
  }
}

const handleSubscribe = async () => {
  if (!column.value) return
  await columnStore.subscribeColumn(column.value.id)
  column.value.isSubscribed = true
  column.value.subscribeCount++
}

const handleUnsubscribe = async () => {
  if (!column.value) return
  await columnStore.unsubscribeColumn(column.value.id)
  column.value.isSubscribed = false
  column.value.subscribeCount--
}

const handlePageChange = () => {
  loadPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped lang="scss">
.column-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.column-header {
  position: relative;
  margin-bottom: 32px;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .header-bg {
    height: 200px;
    background-size: cover;
    background-position: center;
    background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    filter: blur(8px);
    opacity: 0.3;
  }

  .header-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 32px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .column-info {
    flex: 1;
  }

  .column-name {
    font-size: 32px;
    font-weight: 700;
    color: #1f2329;
    margin: 0 0 12px 0;
  }

  .column-desc {
    font-size: 16px;
    color: #646a73;
    line-height: 1.6;
    margin: 0 0 20px 0;
  }

  .column-meta {
    display: flex;
    align-items: center;
    gap: 24px;

    .author-link {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;

      .author-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
      }

      .author-name {
        font-size: 14px;
        color: #1f2329;
        font-weight: 500;

        &:hover {
          color: #409eff;
        }
      }
    }

    .stats {
      display: flex;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 14px;
        color: #646a73;

        .el-icon {
          font-size: 16px;
        }
      }
    }
  }

  .column-actions {
    flex-shrink: 0;
  }
}

.column-posts {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .posts-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #ebeef5;

    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #1f2329;
      margin: 0;
    }

    .post-count {
      font-size: 14px;
      color: #909399;
    }
  }

  .posts-list {
    .post-item {
      display: flex;
      gap: 16px;
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .post-index {
        flex-shrink: 0;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        border-radius: 4px;
        font-size: 14px;
        font-weight: 600;
        color: #909399;
      }

      .post-content {
        flex: 1;
        min-width: 0;
      }

      .post-title {
        font-size: 16px;
        font-weight: 500;
        color: #1f2329;
        text-decoration: none;
        display: block;
        margin-bottom: 8px;

        &:hover {
          color: #409eff;
        }
      }

      .post-desc {
        font-size: 14px;
        color: #646a73;
        line-height: 1.6;
        margin: 0 0 8px 0;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .post-meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #8a919f;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;

          &.time {
            margin-left: auto;
          }
        }
      }

      .post-cover {
        flex-shrink: 0;
        width: 120px;
        height: 80px;
        border-radius: 4px;
        object-fit: cover;
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

@media (max-width: 768px) {
  .column-header {
    .header-content {
      flex-direction: column;
      gap: 16px;
    }

    .column-actions {
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  .column-posts {
    .post-item {
      .post-cover {
        width: 80px;
        height: 60px;
      }
    }
  }
}
</style>
