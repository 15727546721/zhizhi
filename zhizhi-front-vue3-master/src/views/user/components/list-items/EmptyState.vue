<template>
  <div class="empty-state">
    <el-empty :description="description" :image-size="imageSize">
      <template v-if="showAction && actionText" #default>
        <el-button v-if="actionText" type="primary" @click="handleAction">
          {{ actionText }}
        </el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  imageSize: {
    type: Number,
    default: 100
  },
  showAction: {
    type: Boolean,
    default: false
  },
  actionText: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['action'])

const handleAction = () => {
  emit('action')
}
</script>

<style scoped>
.empty-state {
  padding: 40px 20px;
  text-align: center;
}
</style>

