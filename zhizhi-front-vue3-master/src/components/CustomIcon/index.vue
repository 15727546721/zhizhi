<template>
  <svg 
    class="custom-icon" 
    :class="{ 'is-active': active }"
    :style="iconStyle"
    viewBox="0 0 24 24"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path :d="iconPath" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getIconPath } from './icons'

interface Props {
  name: string
  active?: boolean
  size?: number | string
  color?: string
  activeColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  active: false,
  size: 16,
  color: '',
  activeColor: '#1890ff'
})

const iconPath = computed(() => {
  const path = getIconPath(props.name)
  if (!path) {
    console.warn(`[CustomIcon] 未找到图标: ${props.name}，请在 icons.ts 中添加`)
  }
  return path
})

const iconStyle = computed(() => {
  const size = typeof props.size === 'number' ? `${props.size}px` : props.size
  return {
    width: size,
    height: size,
    color: props.active ? props.activeColor : props.color || 'inherit'
  }
})
</script>

<style scoped>
.custom-icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.custom-icon.is-active {
  color: #1890ff;
}
</style>