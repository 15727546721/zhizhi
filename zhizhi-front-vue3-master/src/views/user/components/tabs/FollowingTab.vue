<template>
  <div class="following-tab">
    <div v-loading="loading" class="following-list">
      <UserCard
        v-for="user in list"
        :key="user.id || user.userId"
        :user="user"
        :is-own-profile="isOwnProfile"
        @follow-change="handleFollowChange"
      />
      <EmptyState
        v-if="!loading && list.length === 0"
        description="还没有关注任何人"
      />
    </div>
    
    <div v-if="list.length > 0" class="load-more">
      <el-button
        v-if="hasMore && !loading"
        link
        @click="loadMore"
        :loading="loading"
      >
        加载更多
      </el-button>
      <div v-else-if="!hasMore && list.length > 0" class="no-more">
        没有更多了
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useUserFollow } from '../../composables/useUserFollow'
import UserCard from '../list-items/UserCard.vue'
import EmptyState from '../list-items/EmptyState.vue'

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

const {
  list,
  loading,
  hasMore,
  loadList,
  loadMore,
  refresh
} = useUserFollow(props.userId, 'following', props.isOwnProfile)

// 监听 userId 变化
watch(() => props.userId, (newUserId) => {
  if (newUserId) {
    refresh()
  }
}, { immediate: false })

// 初始化加载
onMounted(() => {
  loadList(true)
})

const handleFollowChange = (data) => {
  // 更新列表中的关注状态
  const user = list.value.find(u => (u.userId || u.id) === data.userId)
  if (user) {
    user.isFollowing = data.isFollowing
  }
}

// 暴露刷新方法
defineExpose({
  refresh
})
</script>

<style scoped>
.following-tab {
  padding: 20px;
  min-height: 400px;
}

.following-list {
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

