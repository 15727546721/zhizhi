<template>
  <div class="info-content" v-if="userInfo">
    <div class="info-section">
      <div class="section-card">
        <h3 class="section-title"><span>基本信息</span></h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">性别</span>
            <span class="value">{{ genderText }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ userInfo.email || '暂无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">个人简介</span>
            <span class="value description-value">{{ userInfo.description || '这个人很懒，什么都没写~' }}</span>
          </div>
        </div>
      </div>
      
      <!-- 安全设置（仅自己可见） -->
      <div class="section-card" v-if="isOwnProfile">
        <h3 class="section-title"><span>安全设置</span></h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">账户密码</span>
            <div class="value">
              <span style="margin-right: 12px;">******</span>
              <el-button type="primary" size="small" @click="$emit('change-password')">
                修改密码
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else-if="!loading" class="info-content">
    <el-empty description="暂无数据" />
  </div>
  <div v-else class="info-content">
    <el-skeleton :rows="8" animated />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserInfo } from '@/types'

const props = defineProps<{
  userInfo: UserInfo | null
  isOwnProfile: boolean
  loading?: boolean
}>()

defineEmits<{
  (e: 'change-password'): void
}>()

const genderText = computed(() => {
  if (props.userInfo?.gender === 1) return '男'
  if (props.userInfo?.gender === 2) return '女'
  return '暂无'
})
</script>

<style scoped>
.info-content {
  padding: 32px;
  min-height: 400px;
  background: #fff;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
}

.section-card {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 13px;
  border: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.section-card:hover {
  border-color: var(--el-color-primary-light-7);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f7fa;
}

.section-title span {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  position: relative;
  padding-left: 12px;
}

.section-title span::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(to bottom, var(--el-color-primary), #409eff);
  border-radius: 2px;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  width: 100%;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8faff;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #f0f7ff;
  transform: translateX(4px);
}

.info-item .label {
  width: 90px;
  font-size: 14px;
  color: #8a919f;
  flex-shrink: 0;
}

.info-item .value {
  flex: 1;
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
}

.info-item .description-value {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: #606266;
}

@media screen and (max-width: 768px) {
  .info-content {
    padding: 20px 16px;
  }

  .section-card {
    padding: 20px;
  }

  .info-list {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .section-title {
    margin-bottom: 20px;
  }

  .section-title span {
    font-size: 16px;
  }

  .info-item {
    padding: 10px 14px;
  }
}
</style>
