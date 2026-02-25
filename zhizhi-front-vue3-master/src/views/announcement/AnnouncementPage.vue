<template>
  <div class="announcement-page">
    <div class="announcement-container">
      <h1 class="page-title">平台公告</h1>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="announcements.length === 0" class="empty-container">
        <el-empty description="暂无公告" />
      </div>
      
      <!-- 公告列表 -->
      <div v-else class="announcement-list">
        <div class="announcement-item" v-for="announcement in announcements" :key="announcement.id">
          <div class="announcement-header">
            <div class="title-wrapper">
              <el-tag v-if="announcement.isTop" type="danger" size="small" class="top-tag">置顶</el-tag>
              <el-tag :type="getTypeTag(announcement.type).type" size="small">{{ getTypeTag(announcement.type).label }}</el-tag>
              <h3 class="announcement-title">{{ announcement.title }}</h3>
            </div>
            <span class="announcement-date">{{ formatDate(announcement.publishTime) }}</span>
          </div>
          <div class="announcement-content">
            {{ announcement.content }}
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-container">
        <el-pagination
          v-model:current-page="pageNo"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncementList, getTypeTag } from '@/api/announcement'

const announcements = ref([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 获取公告数据
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList({ pageNo: pageNo.value, pageSize: pageSize.value })
    if (res.code === 20000 && res.data) {
      announcements.value = res.data.data || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    if (!error.silent) {
      ElMessage.error('获取公告数据失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

// 分页切换
const handlePageChange = (page) => {
  pageNo.value = page
  fetchAnnouncements()
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcement-page {
  min-height: 100vh;
  background-color: #f4f5f5;
  padding: 40px 0;
}

.announcement-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #252933;
  margin-bottom: 32px;
  text-align: center;
}

.announcement-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.announcement-item {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.announcement-item:hover {
  background-color: #fafafa;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.announcement-title {
  font-size: 18px;
  font-weight: 500;
  color: #252933;
  margin: 0;
  flex: 1;
}

.announcement-date {
  font-size: 14px;
  color: #909399;
  margin-left: 16px;
  white-space: nowrap;
}

.announcement-content {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
}

/* 加载和空状态容器 */
.loading-container,
.empty-container {
  background: #fff;
  border-radius: 8px;
  padding: 40px 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 标题包装器 */
.title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

/* 置顶标签 */
.top-tag {
  flex-shrink: 0;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .announcement-page {
    padding: 24px 0;
  }
  
  .announcement-container {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
  
  .announcement-item {
    padding: 20px 16px;
  }
  
  .announcement-header {
    flex-direction: column;
    gap: 8px;
  }
  
  .announcement-date {
    margin-left: 0;
  }
  
  .title-wrapper {
    flex-wrap: wrap;
  }
}
</style>