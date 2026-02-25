<template>
  <div class="user-stats">
    <div class="stat-item clickable" @click="handleStatClick('posts')">
      <div class="stat-value">{{ stats.postCount || 0 }}</div>
      <div class="stat-label">帖子</div>
    </div>
    <div :class="['stat-item', { clickable: isOwnProfile }]" @click="handleStatClick('likes')">
      <div class="stat-value">{{ stats.likeCount || 0 }}</div>
      <div class="stat-label">获赞</div>
    </div>
    <div class="stat-item clickable" @click="handleStatClick('follows')">
      <div class="stat-value">{{ stats.followCount || 0 }}</div>
      <div class="stat-label">关注</div>
    </div>
    <div class="stat-item clickable" @click="handleStatClick('fans')">
      <div class="stat-value">{{ stats.fansCount || 0 }}</div>
      <div class="stat-label">粉丝</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  stats: {
    postCount?: number
    likeCount?: number
    followCount?: number
    fansCount?: number
    commentCount?: number
  }
  isOwnProfile?: boolean
}>()

const emit = defineEmits<{
  (e: 'stat-click', type: string): void
}>()

const handleStatClick = (statType: string) => {
  emit('stat-click', statType)
}
</script>

<style scoped>
.user-stats {
  display: flex;
  align-items: center;
  padding: 24px 0 0;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  margin-top: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.stat-item {
  flex: 1;
  min-width: 100px;
  text-align: center;
  padding: 8px 16px;
  position: relative;
  cursor: default;
  transition: all 0.3s ease;
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 24px;
  background: linear-gradient(to bottom, transparent, #e0e6ed, transparent);
}

.stat-item.clickable {
  cursor: pointer;
}

.stat-item.clickable:hover {
  transform: translateY(-2px);
  background: linear-gradient(to bottom, transparent, rgba(64, 158, 255, 0.04));
  border-radius: 8px;
}

.stat-item.clickable:hover .stat-value {
  background: linear-gradient(135deg, var(--el-color-primary), #409eff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.stat-label {
  font-size: 13px;
  color: #8a919f;
  margin-top: 6px;
  transition: all 0.3s ease;
}

@media screen and (max-width: 768px) {
  .user-stats {
    padding: 20px 0 0;
    gap: 4px;
  }

  .stat-item {
    min-width: auto;
    padding: 8px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-label {
    font-size: 12px;
  }

  .stat-item:not(:last-child)::after {
    display: none;
  }
}
</style>
