import { ref, computed } from 'vue'
import { getMyPosts, getUserPosts } from '@/api/post'
import { ElMessage } from 'element-plus'

/**
 * 用户帖子数据管理 Composable
 * @param {string|number} userId - 用户ID
 * @param {boolean} isOwnProfile - 是否自己的主页
 */
export function useUserPosts(userId, isOwnProfile) {
  const posts = ref([])
  const loading = ref(false)
  const pageNo = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const error = ref(null)

  const hasMore = computed(() => {
    return posts.value.length < total.value
  })

  // 加载帖子列表
  const loadPosts = async (reset = false) => {
    if (loading.value) return

    try {
      loading.value = true
      error.value = null

      if (reset) {
        pageNo.value = 1
        posts.value = []
      }

      // 根据是否自己的主页调用不同的API
      const actualUserId = typeof userId === 'object' && userId.value !== undefined 
        ? userId.value 
        : userId
      
      let response
      if (isOwnProfile) {
        // 获取自己的帖子列表
        response = await getMyPosts({
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          status: 'PUBLISHED' // 只显示已发布的
        })
      } else {
        // 获取指定用户的帖子列表
        response = await getUserPosts(actualUserId, {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          status: 'PUBLISHED' // 只显示已发布的
        })
      }

      if (response.code === 20000) {
        const responseData = response.data
        let newPosts = []
        let totalCount = 0

        // 后端返回格式：PageResponse<List<PostListResponse>>
        // responseData 是 PageResponse 对象，包含 pageNo, pageSize, total, data
        if (responseData && responseData.data && Array.isArray(responseData.data)) {
          newPosts = responseData.data
          totalCount = responseData.total || 0
        } else if (Array.isArray(responseData)) {
          // 容错：直接返回数组的情况
          newPosts = responseData
          totalCount = responseData.length
        } else if (responseData && responseData.list) {
          // 容错：其他格式
          newPosts = responseData.list
          totalCount = responseData.total || responseData.list.length
        }

        // 转换数据格式：PostListResponse 包含 postItem（扁平化）或 post/user
        const formattedPosts = newPosts.map(item => {
          // 优先使用扁平化结构 postItem
          if (item.postItem) {
            return {
              id: item.postItem.id,
              title: item.postItem.title,
              description: item.postItem.description,
              content: item.postItem.content,
              coverUrl: item.postItem.coverUrl,
              type: item.postItem.type,
              status: item.postItem.status,
              likeCount: item.postItem.likeCount || 0,
              commentCount: item.postItem.commentCount || 0,
              viewCount: item.postItem.viewCount || 0,
              createTime: item.postItem.createTime,
              updateTime: item.postItem.updateTime,
              publishTime: item.postItem.publishTime,
              tagNameList: item.postItem.tagNameList || [],
              userId: item.postItem.userId,
              nickname: item.postItem.nickname || '匿名用户',
              avatar: item.postItem.avatar,
              user: {
                id: item.postItem.userId,
                nickname: item.postItem.nickname,
                avatar: item.postItem.avatar
              }
            }
          }
          
          // 处理嵌套结构 post/user
          const post = item.post || item
          const user = item.user || {}
          
          // 处理值对象：如果 title 是对象，获取其 value 属性
          let title = post.title
          if (title && typeof title === 'object' && title.value !== undefined) {
            title = title.value
          }
          
          // 处理值对象：如果 content 是对象，获取其 value 属性
          let content = post.content
          if (content && typeof content === 'object' && content.value !== undefined) {
            content = content.value
          }
          
          // 处理值对象：如果 type 是对象，获取其 code 属性
          let type = post.type
          if (type && typeof type === 'object' && type.code !== undefined) {
            type = type.code
          }
          
          // 处理值对象：如果 status 是对象，获取其 code 属性
          let status = post.status
          if (status && typeof status === 'object' && status.code !== undefined) {
            status = status.code
          }
          
          return {
            id: post.id,
            title: title,
            description: post.description,
            content: content,
            coverUrl: post.coverUrl || post.cover,
            type: type,
            status: status,
            likeCount: post.likeCount || 0,
            commentCount: post.commentCount || 0,
            viewCount: post.viewCount || 0,
            createTime: post.createTime || post.create_time,
            updateTime: post.updateTime || post.update_time,
            publishTime: post.publishTime || post.publish_time,
            tags: post.tags || [],
            tagNameList: post.tags ? (Array.isArray(post.tags) ? post.tags.map(t => typeof t === 'string' ? t : (t.name || t)) : []) : (post.tagNameList || []),
            userId: post.userId,
            user: user,
            nickname: user.nickname || user.name || user.username || '匿名用户',
            avatar: user.avatar
          }
        })

        if (reset) {
          posts.value = formattedPosts
        } else {
          posts.value.push(...formattedPosts)
        }
        total.value = totalCount
        if (!reset) {
          pageNo.value++
        }
      } else {
        error.value = response.info || '获取帖子列表失败'
        ElMessage.error(error.value)
      }
    } catch (err) {
      error.value = err.message || '获取帖子列表失败'
      ElMessage.error('获取帖子列表失败')
    } finally {
      loading.value = false
    }
  }

  // 加载更多
  const loadMore = () => {
    if (hasMore.value && !loading.value) {
      loadPosts(false)
    }
  }

  // 刷新
  const refresh = () => {
    loadPosts(true)
  }

  return {
    posts,
    loading,
    error,
    hasMore,
    total,
    loadPosts,
    loadMore,
    refresh
  }
}

