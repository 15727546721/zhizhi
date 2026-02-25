<template>
  <div class="error-container">
    <el-result
      v-if="error.includes('用户不存在')"
      icon="error"
      title="用户不存在"
      sub-title="该用户可能已被删除或不存在"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
    <el-result
      v-else-if="error.includes('封禁')"
      icon="warning"
      title="用户已被封禁"
      sub-title="该用户已被封禁，无法查看其主页"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
    <el-result
      v-else
      icon="error"
      title="加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重试</el-button>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  error: string
}>()

defineEmits<{
  (e: 'retry'): void
}>()
</script>

<style scoped>
.error-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}
</style>
