<template>
  <div class="my-feedback-page">
    <div class="page-header">
      <h2>我的反馈</h2>
      <el-button type="primary" @click="showFeedbackDialog = true">
        <el-icon><Plus /></el-icon>
        提交反馈
      </el-button>
    </div>

    <!-- 反馈列表 -->
    <div class="feedback-list" v-loading="loading">
      <template v-if="feedbackList.length > 0">
        <div 
          class="feedback-item" 
          v-for="item in feedbackList" 
          :key="item.id"
          @click="showDetail(item)"
        >
          <div class="item-header">
            <el-tag :type="getTypeTagType(item.type)" size="small">
              {{ item.typeName }}
            </el-tag>
            <el-tag :type="getStatusTagType(item.status)" size="small">
              {{ item.statusName }}
            </el-tag>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="item-title">{{ item.title }}</div>
          <div class="item-content">{{ item.content }}</div>
          <div class="item-reply" v-if="item.reply">
            <el-icon><ChatDotRound /></el-icon>
            <span class="reply-label">官方回复：</span>
            <span class="reply-text">{{ item.reply }}</span>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="暂无反馈记录" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNo"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadFeedbackList"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="反馈详情" width="600px">
      <div class="detail-content" v-if="currentFeedback">
        <div class="detail-row">
          <span class="label">类型：</span>
          <el-tag :type="getTypeTagType(currentFeedback.type)" size="small">
            {{ currentFeedback.typeName }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">状态：</span>
          <el-tag :type="getStatusTagType(currentFeedback.status)" size="small">
            {{ currentFeedback.statusName }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">提交时间：</span>
          <span>{{ formatTime(currentFeedback.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">标题：</span>
          <span>{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-row">
          <span class="label">内容：</span>
          <div class="content-text">{{ currentFeedback.content }}</div>
        </div>
        <div class="detail-row" v-if="currentFeedback.contact">
          <span class="label">联系方式：</span>
          <span>{{ currentFeedback.contact }}</span>
        </div>
        
        <!-- 官方回复 -->
        <div class="reply-section" v-if="currentFeedback.reply">
          <div class="reply-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>官方回复</span>
            <span class="reply-time">{{ formatTime(currentFeedback.replyTime) }}</span>
          </div>
          <div class="reply-content">{{ currentFeedback.reply }}</div>
        </div>
        <div class="no-reply" v-else>
          <el-icon><Clock /></el-icon>
          <span>等待官方回复中...</span>
        </div>
      </div>
    </el-dialog>

    <!-- 提交反馈弹窗 -->
    <FeedbackDialog v-model="showFeedbackDialog" @success="handleFeedbackSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, ChatDotRound, Clock } from '@element-plus/icons-vue'
import { getMyFeedbackList, FeedbackStatusOptions } from '@/api/feedback'
import FeedbackDialog from '@/components/FeedbackDialog.vue'

const loading = ref(false)
const feedbackList = ref([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

const showDetailDialog = ref(false)
const currentFeedback = ref(null)
const showFeedbackDialog = ref(false)

// 加载反馈列表
const loadFeedbackList = async () => {
  loading.value = true
  try {
    const res = await getMyFeedbackList({ pageNo: pageNo.value, pageSize: pageSize.value })
    if (res.code === 20000) {
      feedbackList.value = res.data?.data || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    // 加载失败
  } finally {
    loading.value = false
  }
}

// 显示详情
const showDetail = (item) => {
  currentFeedback.value = item
  showDetailDialog.value = true
}

// 反馈成功后刷新列表
const handleFeedbackSuccess = () => {
  pageNo.value = 1
  loadFeedbackList()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 30) return `${days}天前`
    
    // 超过30天显示具体日期
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch {
    return time
  }
}

// 获取类型标签颜色
const getTypeTagType = (type) => {
  const types = ['danger', 'primary', 'warning', 'info']
  return types[type] || 'info'
}

// 获取状态标签颜色
const getStatusTagType = (status) => {
  const option = FeedbackStatusOptions.find(o => o.value === status)
  return option?.type || 'info'
}

onMounted(() => {
  loadFeedbackList()
})
</script>

<style scoped>
.my-feedback-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.feedback-list {
  min-height: 300px;
}

.feedback-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.feedback-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.item-header .time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.item-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-reply {
  margin-top: 12px;
  padding: 10px 12px;
  background: #f0f9eb;
  border-radius: 6px;
  font-size: 13px;
  color: #67c23a;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.item-reply .reply-label {
  font-weight: 500;
  flex-shrink: 0;
}

.item-reply .reply-text {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 详情弹窗 */
.detail-content {
  padding: 8px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
}

.detail-row .label {
  width: 80px;
  flex-shrink: 0;
  color: #909399;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.6;
}

.reply-section {
  margin-top: 24px;
  padding: 16px;
  background: #f0f9eb;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #67c23a;
  font-weight: 500;
  margin-bottom: 12px;
}

.reply-header .reply-time {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.reply-content {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}

.no-reply {
  margin-top: 24px;
  padding: 16px;
  background: #fdf6ec;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e6a23c;
}
</style>
