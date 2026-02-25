<template>
  <div class="user-avatar-wrapper" :class="customClass">
    <!-- 有有效头像URL时 -->
    <el-avatar 
      v-if="hasValidSrc && !imageError"
      :size="size" 
      :src="props.src" 
      class="user-avatar"
      @error="handleError"
    />
    
    <!-- 无头像时显示首字母 -->
    <div 
      v-else
      class="user-avatar user-avatar--fallback"
      :style="fallbackContainerStyle"
    >
      <span class="avatar-fallback" :style="fallbackTextStyle">{{ fallbackText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface Props {
  src?: string
  username?: string | number
  nickname?: string | number
  size?: number | string
  customClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  src: '',
  username: '',
  nickname: '',
  size: 40,
  customClass: ''
})

const imageError = ref(false)

watch(
  () => props.src,
  () => {
    imageError.value = false
  }
)

const hasValidSrc = computed(() => {
  return props.src && props.src.trim() !== ''
})

const fallbackText = computed(() => {
  const name = String(props.nickname || props.username || '?')
  if (!name.trim()) return '?'

  const firstChar = name.trim().charAt(0)
  if (/[\u4e00-\u9fa5]/.test(firstChar)) {
    return firstChar
  }
  return firstChar.toUpperCase()
})

const backgroundColor = computed(() => {
  const name = String(props.nickname || props.username || 'guest')
  const colors = [
    '#667eea',
    '#f093fb',
    '#4facfe',
    '#43e97b',
    '#fa709a',
    '#30cfd0',
    '#a8edea',
    '#ff9a9e',
    '#6a11cb',
    '#00b4db'
  ]

  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }

  return colors[Math.abs(hash) % colors.length]
})

const fallbackContainerStyle = computed(() => {
  const sizeNum = typeof props.size === 'number' ? props.size : parseInt(props.size) || 40
  return {
    width: `${sizeNum}px`,
    height: `${sizeNum}px`,
    backgroundColor: backgroundColor.value
  }
})

const fallbackTextStyle = computed(() => {
  const sizeNum = typeof props.size === 'number' ? props.size : parseInt(props.size) || 40
  return {
    fontSize: `${Math.max(sizeNum * 0.45, 12)}px`
  }
})

const handleError = () => {
  imageError.value = true
  return true
}
</script>

<style scoped>
.user-avatar-wrapper {
  display: inline-block;
  flex-shrink: 0;
}

.user-avatar {
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar:hover {
  transform: scale(1.05);
}

/* fallback容器样式 */
.user-avatar--fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-avatar--fallback:hover {
  transform: scale(1.05);
}

.avatar-fallback {
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  user-select: none;
}
</style>
