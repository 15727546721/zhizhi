<template>
  <div class="base-layout">
    <app-header v-if="!hideHeader" class="header" :class="{ 'header-hidden': isHeaderHidden }" />
    <div class="main-wrapper" :class="{ 'no-header': hideHeader }">
      <slot></slot>
    </div>
    <floating-buttons />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './AppHeader.vue'
import FloatingButtons from '@/components/common/FloatingButtons.vue'

const route = useRoute()
const hideHeader = computed(() => route.meta.hideHeader as boolean | undefined)
const isHeaderHidden = ref(false)
let lastScrollTop = 0

const handleScroll = () => {
  const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop
  isHeaderHidden.value = currentScrollTop > lastScrollTop && currentScrollTop > 60
  lastScrollTop = currentScrollTop
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.base-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  /* 移除overflow: hidden，避免影响弹窗显示 */
}

.header {
  position: fixed;
  top: var(--announcement-bar-height); /* 放在公告条下方 */
  left: 0;
  right: 0;
  height: var(--header-height);
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, top 0.3s ease; /* 添加top的过渡效果 */
  z-index: 1000;
}

.header-hidden {
  transform: translateY(-100%);
}

.main-wrapper {
  padding-top: calc(var(--header-height) + var(--announcement-bar-height)); /* 为header和公告条都留出空间 */
  flex: 1;
  width: 100%;
  background-color: #f4f5f5;
  box-sizing: border-box;
  position: relative;
  min-height: 0; /* 确保flex子元素可以正常滚动 */
}

.main-wrapper.no-header {
  padding-top: 0;
}
</style>
