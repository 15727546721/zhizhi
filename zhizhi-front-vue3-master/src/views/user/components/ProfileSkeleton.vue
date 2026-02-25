<template>
  <div class="profile-skeleton">
    <div class="skeleton-header">
      <el-skeleton :rows="0" animated>
        <template #template>
          <div class="skeleton-avatar">
            <el-skeleton-item variant="circle" :size="80" />
          </div>
          <div class="skeleton-content">
            <el-skeleton-item variant="h1" style="width: 200px; margin-bottom: 16px;" />
            <el-skeleton-item variant="text" style="width: 300px; margin-bottom: 8px;" />
            <el-skeleton-item variant="text" style="width: 250px;" />
          </div>
          <div class="skeleton-actions">
            <el-skeleton-item variant="button" style="width: 100px; height: 36px; margin-right: 12px;" />
            <el-skeleton-item variant="button" style="width: 100px; height: 36px;" />
          </div>
        </template>
      </el-skeleton>
    </div>
    <div class="skeleton-stats">
      <el-skeleton :rows="0" animated>
        <template #template>
          <div class="skeleton-stat-item" v-for="i in 4" :key="i">
            <el-skeleton-item variant="text" style="width: 40px; height: 32px; margin-bottom: 8px;" />
            <el-skeleton-item variant="text" style="width: 30px; height: 16px;" />
          </div>
        </template>
      </el-skeleton>
    </div>
    <div class="skeleton-tabs">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.profile-skeleton {
  max-width: 1000px;
  margin: 0 auto;
}

.skeleton-header {
  background: linear-gradient(to right, #fff, #f8faff);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.skeleton-avatar {
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  padding-top: 8px;
}

.skeleton-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.skeleton-stats {
  background: linear-gradient(to right, #fff, #f8faff);
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-around;
  gap: 24px;
}

.skeleton-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.skeleton-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}
</style>
