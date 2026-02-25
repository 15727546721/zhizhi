<template>
  <el-popover
    :width="340"
    trigger="hover"
    :show-after="showDelay"
    popper-class="user-hover-card-popper"
    @show="handleShow"
  >
    <template #reference>
      <slot></slot>
    </template>
    
    <div v-loading="loading" class="user-hover-card">
        <!-- Banner -->
        <div class="card-banner"></div>
        
        <div class="card-content">
            <div class="user-header">
                <UserAvatar :size="64" :src="userInfo.avatar" :username="userInfo.username" :nickname="userInfo.nickname" custom-class="user-avatar" />
                <div class="action-buttons" v-if="!isSelf">
                     <el-button size="small" @click="handleMessage">私信</el-button>
                     <el-button 
                        size="small" 
                        :type="isFollowing ? 'default' : 'primary'"
                        @click="handleFollow"
                     >
                        {{ isFollowing ? '已关注' : '+ 关注' }}
                     </el-button>
                </div>
            </div>
            
            <div class="user-info">
                <div class="nickname-row">
                    <span class="nickname">{{ userInfo.nickname || userInfo.username || '未知用户' }}</span>
                    <el-tag v-if="userInfo.level" size="small" effect="dark" type="success" class="level-tag">Lv.{{ userInfo.level }}</el-tag>
                    <el-tag v-if="userInfo.id <= 10" size="small" effect="plain" type="danger" class="level-tag">管理员</el-tag>
                </div>
                
                <div class="bio">
                    {{ userInfo.description || '作者有点忙，还没有简介' }}
                </div>
                
                <div class="tags">
                    <div class="tag-item" v-if="userInfo.gender && userInfo.gender !== 0">
                        <el-icon><Male v-if="userInfo.gender === 1"/><Female v-else-if="userInfo.gender === 2"/></el-icon>
                        <span>{{ formatGender(userInfo.gender) }}</span>
                    </div>
                     
                     <div class="tag-item" v-if="userInfo.position">
                        <el-icon><Suitcase/></el-icon>
                        <span>{{ userInfo.position }}</span>
                     </div>
                     
                     <div class="tag-item" v-if="userInfo.company">
                        <el-icon><OfficeBuilding/></el-icon>
                        <span>{{ userInfo.company }}</span>
                     </div>
                </div>
                
                <div class="stats-row">
                     <div class="stat-item">
                        <span class="label">关注</span>
                        <span class="value">{{ stats.followCount || 0 }}</span>
                     </div>
                     <div class="stat-item">
                        <span class="label">粉丝</span>
                        <span class="value">{{ stats.fansCount || 0 }}</span>
                     </div>
                     <!-- 排名暂无数据支持，先隐藏 -->
                     <!-- <div class="stat-item">
                        <span class="label">排名</span>
                        <span class="value">{{ stats.ranking || '-' }}</span>
                     </div> -->
                </div>
            </div>
        </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile } from '@/api/user'
import { followUser, unfollowUser } from '@/api/follow'
import { ElMessage, ElPopover, ElAvatar, ElButton, ElTag, ElIcon } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import { Male, Female, Suitcase, OfficeBuilding } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar.vue'
import type { UserProfile } from '@/types'
import { validateApiResponse } from '@/utils/typeGuards'

interface UserInfo {
  id?: number
  avatar?: string
  username?: string
  nickname?: string
  level?: number
  gender?: number
  position?: string
  company?: string
  description?: string
}

interface Stats {
  followCount?: number
  fansCount?: number
  ranking?: number
}

interface Props {
  userId: string | number
  showDelay?: number
}

const props = withDefaults(defineProps<Props>(), {
  showDelay: 300
})

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const userInfo = ref<UserInfo>({})
const stats = ref<Stats>({})
const isFollowing = ref(false)
const isSelf = computed(() => {
  return userStore.userInfo?.id == props.userId
})

// Check if we have data loaded
const dataLoaded = ref(false)

const handleShow = async () => {
  if (dataLoaded.value) return
  
  loading.value = true
  try {
    const res = await getUserProfile(Number(props.userId))
    
    // 使用类型守卫验证响应
    const profileData = validateApiResponse<UserProfile>(res)
    
    if (profileData) {
      // 映射增强类型到组件内部类型
      userInfo.value = {
        id: profileData.basicInfo.id,
        avatar: profileData.basicInfo.avatar,
        username: profileData.basicInfo.username,
        nickname: profileData.basicInfo.nickname,
        gender: profileData.basicInfo.gender ?? undefined,
        description: profileData.basicInfo.bio ?? undefined
      }
      
      stats.value = {
        followCount: profileData.stats.followCount,
        fansCount: profileData.stats.fansCount
      }
      
      isFollowing.value = profileData.isFollowing
      dataLoaded.value = true
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleFollow = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const action = isFollowing.value ? unfollowUser : followUser
    const res = await action(Number(props.userId))
    if (res.code === 20000) {
        isFollowing.value = !isFollowing.value
        // Update stats locally
        if (isFollowing.value) {
            stats.value.fansCount = (stats.value.fansCount || 0) + 1
        } else {
             stats.value.fansCount = Math.max((stats.value.fansCount || 0) - 1, 0)
        }
        ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
    }
  } catch (err) {
    console.error('关注操作失败:', err)
  }
}

const handleMessage = () => {
   if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }
  // Navigate to message page
  router.push(`/private-messages?userId=${props.userId}`)
}

const formatGender = (gender: number): string => {
    if (gender === 1) return '男'
    if (gender === 2) return '女'
    return '未知'
}
</script>

<style scoped>
.user-hover-card {
    position: relative;
}
.card-banner {
    height: 80px;
    background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
    border-radius: 4px 4px 0 0;
    margin: -12px -12px 0;
    position: relative;
    overflow: hidden;
}

/* 添加一些简单的背景纹理 */
.card-banner::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 20%),
        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 25%);
}

.card-content {
    position: relative;
    padding-top: 0;
}
.user-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-top: -30px;
    margin-bottom: 12px;
    padding: 0 4px;
    position: relative;
    z-index: 1;
}
.user-avatar {
    border: 4px solid #fff;
    background: #fff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    transition: transform 0.3s ease;
}
.user-avatar:hover {
    transform: scale(1.05);
}
.action-buttons {
    margin-bottom: 5px;
    display: flex;
    gap: 8px;
}
.nickname-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}
.nickname {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}
.bio {
    font-size: 13px;
    color: #909399;
    margin-bottom: 12px;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 16px;
}
.tag-item {
    display: flex;
    align-items: center;
    gap: 4px;
    background: #f5f7fa;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    color: #606266;
    transition: all 0.2s;
}
.tag-item:hover {
    background: #ecf5ff;
    color: #409eff;
}
.stats-row {
    display: flex;
    gap: 24px;
    border-top: 1px solid #EBEEF5;
    padding-top: 16px;
}
.stat-item {
    display: flex;
    gap: 6px;
    align-items: center;
    font-size: 14px;
}
.stat-item .label {
    color: #606266;
}
.stat-item .value {
    font-weight: bold;
    color: #303133;
    font-family: 'Helvetica Neue', Helvetica, sans-serif;
    font-size: 16px;
}
</style>
