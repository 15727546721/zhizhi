<template>
  <div id="comments-section">
    <!-- 评论头部 -->
    <CommentHeader 
      :count="totalCommentCount" 
      :current-sort="currentSort"
      @sort-change="handleSortChange"
    />
    
    <!-- 评论编辑器 -->
    <CommentEditor @submit="handleCommentSubmit" />
    
    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="!comments.length">
        <el-empty description="暂无评论" :image-size="40">
          <template #description>
            <p>暂时还没有人评论～</p>
          </template>
        </el-empty>
      </div>
      <div v-else>
        <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :author-id="authorId"
          @like="handleCommentLike"
          @reply="handleCommentReply"
          @delete="handleCommentDelete"
          @report="handleCommentReport"
        >
          <!-- 回复编辑器 -->
          <template #editor>
            <ReplyEditor
              v-if="replyingTo?.comment === comment"
              :reply-to="comment"
              :parent-id="comment.id"
              @submit="handleReplySubmit"
              @cancel="cancelReply"
            />
          </template>

          <!-- 回复列表 -->
          <template #replies>
            <div class="reply-list" v-if="comment.children?.length">
              <ReplyItem
                v-for="reply in getVisibleReplies(comment)"
                :key="reply.id"
                :reply="reply"
                :author-id="authorId"
                @like="handleCommentLike"
                @reply="(r) => handleReplyToReply(r, comment)"
                @delete="handleReplyDelete"
                @report="handleCommentReport"
                @view-conversation="(r) => openConversation(r)"
              >
                <template #editor>
                  <ReplyEditor
                    v-if="replyingTo?.comment === reply"
                    :reply-to="reply"
                    :parent-id="comment.id"
                    @submit="handleReplySubmit"
                    @cancel="cancelReply"
                  />
                </template>
              </ReplyItem>

              <!-- 展开/收起/加载更多 -->
              <div v-if="getHiddenReplyCount(comment) > 0 && !isReplyExpanded(comment.id) && !hasMoreReplies(comment)" class="expand-more-replies">
                <div class="expand-more-btn" @click="toggleReplies(comment.id)">
                  展开 {{ getHiddenReplyCount(comment) }} 条回复
                  <span class="arrow-icon">∨</span>
                </div>
              </div>
              <div v-else-if="hasMoreReplies(comment)" class="expand-more-replies">
                <div class="expand-more-btn" @click="fetchCommentReplies(comment.id)">
                  查看全部 {{ comment.replyCount > 0 ? comment.replyCount : '' }} 条回复
                  <span class="arrow-icon">∨</span>
                </div>
              </div>
              <div v-else-if="isReplyExpanded(comment.id) && comment.children?.length > PREVIEW_REPLY_COUNT" class="expand-more-replies">
                <div class="expand-more-btn collapse" @click="toggleReplies(comment.id)">
                  收起回复
                  <span class="arrow-icon">∧</span>
                </div>
              </div>
            </div>
          </template>
        </CommentItem>
      </div>
    </div>
    
    <!-- 加载更多 -->
    <div class="load-more-container" ref="loadMoreRef" v-if="comments.length > 0">
      <div v-if="loading" class="loading-state">
        <Loading class="loading-icon" />
        <span>加载中...</span>
      </div>
      <div v-else-if="hasMoreComments" class="scroll-load-hint">
        <span>下拉加载更多...</span>
      </div>
      <div v-else class="no-more">
        没有更多评论了
      </div>
    </div>
    
    <!-- 对话链弹窗 -->
    <ConversationDialog
      v-model="showConversationDialog"
      :reply-id="conversationReplyId"
      :author-id="authorId"
    />
    
    <!-- 举报弹窗 -->
    <ReportDialog ref="reportDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import { 
  getCommentPage,
  getReplyPage, 
  createComment, 
  replyComment, 
  deleteComment,
  CommentType
} from '@/api/comment'
import { like, unlike } from '@/api/like'
import { formatTime } from '@/utils/time'
import ReportDialog from '@/components/ReportDialog.vue'
import { ReportTargetType } from '@/api/report'

import CommentHeader from './CommentHeader.vue'
import CommentEditor from './CommentEditor.vue'
import CommentItem from './CommentItem.vue'
import ReplyItem from './ReplyItem.vue'
import ReplyEditor from './ReplyEditor.vue'
import ConversationDialog from './ConversationDialog.vue'
import type { CommentItem as CommentItemType, ReplyingTo } from './types'

const props = withDefaults(defineProps<{
  postId: string | number
  authorId?: string | number
  isQuestion?: boolean
}>(), {
  authorId: 0,
  isQuestion: false
})

const emit = defineEmits<{
  (e: 'update:comments', comments: CommentItemType[]): void
  (e: 'comment-count-change', count: number): void
}>()

const userStore = useUserStore()

// 状态
const comments = ref<CommentItemType[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const hasMoreComments = ref(true)
const loading = ref(false)
const currentSort = ref('NEW')
const serverTotalCount = ref(0)

// 回复相关
const replyingTo = ref<ReplyingTo | null>(null)
const PREVIEW_REPLY_COUNT = 2
const expandedReplies = ref<Set<number>>(new Set())
const currentReplyPageMap = ref<Map<number, number>>(new Map())

// 对话弹窗
const showConversationDialog = ref(false)
const conversationReplyId = ref<number | null>(null)

// 举报弹窗
const reportDialogRef = ref<InstanceType<typeof ReportDialog> | null>(null)

// 滚动加载
const loadMoreRef = ref<HTMLElement | null>(null)
let intersectionObserver: IntersectionObserver | null = null

const totalCommentCount = computed(() => serverTotalCount.value)

// 热门回复置顶
const sortRepliesByHot = (replies: CommentItemType[]): CommentItemType[] => {
  if (!replies?.length) return replies
  const hotReplies = replies.filter(r => (r.likeCount || 0) >= 10)
  const normalReplies = replies.filter(r => (r.likeCount || 0) < 10)
  hotReplies.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  return [...hotReplies, ...normalReplies]
}

// 格式化评论数据
const formatCommentData = (comment: any): CommentItemType => ({
  id: comment.id,
  userId: comment.userId,
  targetType: comment.targetType,
  targetId: comment.targetId,
  parentId: comment.parentId,
  content: comment.content,
  createTime: comment.createTime,
  updateTime: comment.updateTime,
  formattedTime: formatTime(comment.createTime),
  likeCount: comment.likeCount || 0,
  replyCount: comment.replyCount || 0,
  nickname: comment.nickname || comment.user?.nickname,
  avatar: comment.avatar || comment.user?.avatar,
  username: comment.nickname || comment.user?.nickname,
  user: comment.user || { nickname: comment.nickname, avatar: comment.avatar, username: comment.nickname },
  replyUser: comment.replyUser,
  imageUrls: comment.imageUrls || comment.images || [],
  isLiked: comment.isLiked || false,
  isAuthorLiked: comment.isAuthorLiked || false,
  children: sortRepliesByHot((comment.replies ?? comment.children ?? []).map((reply: any) => formatReplyData(reply))),
  hotScore: comment.hotScore,
  isHot: comment.isHot || false,
  likeLoading: false
})

const formatReplyData = (reply: any): CommentItemType => ({
  id: reply.id,
  userId: reply.userId || reply.user?.id,
  content: reply.content,
  createTime: reply.createTime,
  updateTime: reply.updateTime,
  formattedTime: formatTime(reply.createTime),
  nickname: reply.nickname || reply.user?.nickname,
  avatar: reply.avatar || reply.user?.avatar,
  username: reply.nickname || reply.user?.nickname,
  user: reply.user || { nickname: reply.nickname, avatar: reply.avatar, username: reply.nickname },
  replyUser: reply.replyUser || { nickname: reply.replyToNickname },
  replyUserId: reply.replyToUserId || reply.replyUserId || reply.replyUser?.id,
  likeCount: reply.likeCount || 0,
  imageUrls: reply.imageUrls || reply.images || [],
  isLiked: reply.isLiked || false,
  isAuthorLiked: reply.isAuthorLiked || false,
  isHot: (reply.likeCount || 0) >= 10,
  likeLoading: false
})

// 获取评论列表
const fetchComments = async () => {
  if (loading.value || (!hasMoreComments.value && currentPage.value > 1)) return

  loading.value = true
  try {
    const response = await getCommentPage({
      targetType: CommentType.POST,
      targetId: Number(props.postId),
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      sortType: currentSort.value
    }) as any

    const pageData = response.data
    const commentList = pageData?.data || pageData || []
    serverTotalCount.value = pageData?.total || 0

    const formattedComments = commentList.map(formatCommentData)

    if (currentPage.value === 1) {
      comments.value = formattedComments
    } else {
      comments.value = [...comments.value, ...formattedComments]
    }

    hasMoreComments.value = formattedComments.length >= pageSize.value
    currentPage.value += 1
    emit('comment-count-change', totalCommentCount.value)
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 排序切换
const handleSortChange = (sortValue: string) => {
  if (currentSort.value !== sortValue) {
    currentSort.value = sortValue
    currentPage.value = 1
    comments.value = []
    hasMoreComments.value = true
    fetchComments()
  }
}

// 发表评论
const handleCommentSubmit = async (data: { content: string; imageUrls: string[]; mentionUserIds: number[] }) => {
  if (!props.postId || isNaN(Number(props.postId))) {
    ElMessage.error('帖子ID无效')
    return
  }

  try {
    const response = await createComment({
      type: CommentType.POST,
      content: data.content,
      targetId: Number(props.postId),
      imageUrls: data.imageUrls,
      mentionUserIds: data.mentionUserIds
    }) as any

    if (response.code === 20000) {
      currentPage.value = 1
      comments.value = []
      await fetchComments()
      ElMessage.success('评论发表成功')
    } else {
      ElMessage.error(response.info || response.message || '评论发表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发表评论失败')
  }
}

// 点赞
const handleCommentLike = async (comment: CommentItemType) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  if (comment.likeLoading) return
  comment.likeLoading = true

  try {
    const action = comment.isLiked ? unlike : like
    const response = await action({ targetId: comment.id, type: 'COMMENT' }) as any

    if (response.code === 20000) {
      const newLikedStatus = !comment.isLiked
      comment.isLiked = newLikedStatus
      comment.likeCount = newLikedStatus 
        ? (comment.likeCount || 0) + 1 
        : Math.max(0, (comment.likeCount || 0) - 1)

      const currentUserId = Number(userStore.userInfo?.id)
      const postAuthorId = props.authorId ? Number(props.authorId) : null
      if (postAuthorId && currentUserId === postAuthorId) {
        comment.isAuthorLiked = newLikedStatus
      }

      ElMessage.success(newLikedStatus ? '点赞成功' : '已取消点赞')
    } else {
      ElMessage.error(response.info || '操作失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '点赞操作失败')
  } finally {
    comment.likeLoading = false
  }
}

// 回复评论
const handleCommentReply = (comment: CommentItemType) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  replyingTo.value = { comment, parentComment: null, parentId: comment.id }
}

// 回复回复
const handleReplyToReply = (reply: CommentItemType, parentComment: CommentItemType) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  replyingTo.value = { comment: reply, parentComment, parentId: parentComment.id }
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 提交回复
const handleReplySubmit = async (data: { content: string; imageUrls: string[]; parentId: number; replyUserId: number }) => {
  try {
    const response = await replyComment({
      type: CommentType.POST,
      content: data.content,
      targetId: Number(props.postId),
      parentId: data.parentId,
      replyUserId: data.replyUserId,
      imageUrls: data.imageUrls
    }) as any

    if (response.code === 20000) {
      ElMessage.success('回复成功')
      replyingTo.value = null
      currentPage.value = 1
      comments.value = []
      await fetchComments()
    } else {
      ElMessage.error(response.info || response.message || '回复失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发表回复失败')
  }
}

// 删除评论
const handleCommentDelete = async (comment: CommentItemType) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteComment(comment.id) as any
    if (response.code === 20000) {
      ElMessage.success('删除成功')
      currentPage.value = 1
      comments.value = []
      await fetchComments()
    } else {
      ElMessage.error(response.info || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除评论失败')
    }
  }
}

// 删除回复
const handleReplyDelete = async (reply: CommentItemType) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '删除提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteComment(reply.id) as any
    if (response.code === 20000) {
      ElMessage.success('删除成功')
      currentPage.value = 1
      comments.value = []
      await fetchComments()
    } else {
      ElMessage.error(response.info || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除回复失败')
    }
  }
}

// 举报
const handleCommentReport = (comment: CommentItemType) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  
  const content = comment.content || ''
  const preview = content.length > 50 ? content.substring(0, 50) + '...' : content
  reportDialogRef.value?.open(ReportTargetType.COMMENT, comment.id, preview)
}

// 回复展开/收起
const isReplyExpanded = (commentId: number) => expandedReplies.value.has(commentId)

const toggleReplies = (commentId: number) => {
  if (expandedReplies.value.has(commentId)) {
    expandedReplies.value.delete(commentId)
  } else {
    expandedReplies.value.add(commentId)
  }
}

const getVisibleReplies = (comment: CommentItemType): CommentItemType[] => {
  if (!comment.children?.length) return []
  return isReplyExpanded(comment.id) ? comment.children : comment.children.slice(0, PREVIEW_REPLY_COUNT)
}

const getHiddenReplyCount = (comment: CommentItemType): number => {
  return Math.max(0, (comment.children?.length || 0) - PREVIEW_REPLY_COUNT)
}

const hasMoreReplies = (comment: CommentItemType): boolean => {
  return (comment.replyCount || 0) > (comment.children?.length || 0)
}

// 加载更多回复
const fetchCommentReplies = async (commentId: number) => {
  try {
    const response = await getReplyPage({
      parentId: commentId,
      sortType: 'time',
      pageNo: 1,
      pageSize: 100
    }) as any

    // 处理分页结构：response.data 可能是 { data: [...], total: ... } 或直接是数组
    const replyList = response?.data?.data || response?.data || []
    
    if (Array.isArray(replyList)) {
      const commentIndex = comments.value.findIndex(c => c.id === commentId)
      if (commentIndex !== -1) {
        comments.value[commentIndex].children = replyList.map(formatReplyData)
        expandedReplies.value.add(commentId)
      }
    }
  } catch (error) {
    ElMessage.error('获取回复失败')
  }
}

// 对话链
const openConversation = (reply: CommentItemType) => {
  if (!reply.replyUserId || !reply.id) return
  conversationReplyId.value = reply.id
  showConversationDialog.value = true
}

// 滚动加载
const initIntersectionObserver = () => {
  if (intersectionObserver) intersectionObserver.disconnect()
  if (!loadMoreRef.value) return

  intersectionObserver = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !loading.value && hasMoreComments.value) {
        fetchComments()
      }
    },
    { root: null, rootMargin: '100px', threshold: 0.1 }
  )
  intersectionObserver.observe(loadMoreRef.value)
}

watch(() => comments.value.length, (newLen, oldLen) => {
  if (oldLen === 0 && newLen > 0) {
    nextTick(() => initIntersectionObserver())
  }
})

watch(totalCommentCount, (newCount) => {
  emit('comment-count-change', newCount)
})

onMounted(() => {
  fetchComments()
  nextTick(() => initIntersectionObserver())
})

onUnmounted(() => {
  if (intersectionObserver) {
    intersectionObserver.disconnect()
    intersectionObserver = null
  }
  currentReplyPageMap.value.clear()
  expandedReplies.value.clear()
})
</script>

<style scoped>
#comments-section {
  background: #ffffff;
  border-radius: 4px;
  padding: 24px;
  margin-top: 20px;
}

.comment-list {
  margin-top: 24px;
}

.reply-list {
  margin-top: 16px;
  padding: 0 0 0 16px;
  background: transparent;
}

.expand-more-replies {
  padding: 0;
  margin-top: 8px;
  margin-left: 52px;
}

.expand-more-btn {
  color: #409eff;
  font-size: 14px;
  background: transparent;
  border: none;
  padding: 4px 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
  font-weight: 500;
}

.expand-more-btn:hover {
  color: #66b1ff;
  opacity: 0.8;
}

.expand-more-btn .arrow-icon {
  font-size: 12px;
  transition: transform 0.2s;
}

.expand-more-btn:hover .arrow-icon {
  transform: translateY(2px);
}

.expand-more-btn.collapse {
  color: #999;
}

.expand-more-btn.collapse:hover {
  color: #666;
}

.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  margin-top: 10px;
}

.loading-state {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8a919f;
  font-size: 14px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.scroll-load-hint {
  color: #8a919f;
  font-size: 14px;
  padding: 8px 0;
}

.no-more {
  color: #8a919f;
  font-size: 14px;
}

@media screen and (max-width: 768px) {
  .reply-list {
    padding-left: 12px;
  }
}
</style>
