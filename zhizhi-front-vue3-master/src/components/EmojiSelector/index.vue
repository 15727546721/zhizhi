<template>
  <div class="zh-emoji-selector" ref="triggerRef">
    <el-button 
      class="zh-emoji-trigger"
      :class="{ active: visible }"
      @click.stop="togglePicker"
    >
      <span v-if="icon" class="zh-emoji-icon" :style="iconStyle">{{ icon }}</span>
      <span v-if="text" class="zh-emoji-text">{{ text }}</span>
    </el-button>

    <Teleport to="body">
      <Transition name="zh-fade">
        <div 
          v-show="visible"
          ref="popperRef"
          class="zh-emoji-popper"
          :style="popperStyle"
        >
          <!-- 懒加载：只在第一次打开时渲染 -->
          <EmojiPicker
            v-if="hasOpened"
            :native="true"
            @select="handleSelect"
            :group-names="optionsName"
            :display-recent="true"
            :hide-search="true"
            :disable-skin-tones="true"
            theme="auto"
            :pickerStyle="{
            }"
          />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts" name="ZhEmojiSelector">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { createPopper, type Instance as PopperInstance } from '@popperjs/core'
import EmojiPicker from 'vue3-emoji-picker'
import 'vue3-emoji-picker/css'

type Placement =
  | 'top-start'
  | 'top'
  | 'top-end'
  | 'bottom-start'
  | 'bottom'
  | 'bottom-end'

interface Props {
  modelValue?: boolean
  size?: number | string
  icon?: string
  text?: string
  placement?: Placement
  offset?: [number, number]
  zIndex?: number
}

interface EmojiData {
  i: string
  n: string[]
  r: string
  t: string
  u: string
}

const optionsName: Record<string, string> = {
  smileys_people: '微笑与人物',
  animals_nature: '动物与自然',
  food_drink: '食物与饮料',
  activities: '活动',
  travel_places: '旅行与地点',
  objects: '物体',
  symbols: '符号',
  flags: '旗帜',
  recent: '最近使用'
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  size: 20,
  icon: '😊',
  text: '',
  placement: 'bottom-start',
  offset: () => [0, 8],
  zIndex: 2000
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [emoji: EmojiData]
}>()

const visible = ref(props.modelValue)
const hasOpened = ref(false) // 懒加载标记
const triggerRef = ref<HTMLElement | null>(null)
const popperRef = ref<HTMLElement | null>(null)
let popperInstance: PopperInstance | null = null

const initPopper = () => {
  if (!triggerRef.value || !popperRef.value) return
  
  // 避免重复创建
  if (popperInstance) {
    popperInstance.update()
    return
  }

  popperInstance = createPopper(triggerRef.value, popperRef.value, {
    placement: props.placement,
    modifiers: [
      { name: 'offset', options: { offset: props.offset } },
      { name: 'preventOverflow', options: { padding: 8 } }
    ]
  })
}

const updatePopper = () => {
  if (popperInstance) {
    popperInstance.update()
  }
}

const iconStyle = computed(() => ({
  fontSize: `${props.size}px`
}))

const popperStyle = computed(() => ({
  zIndex: props.zIndex
}))

const togglePicker = () => {
  visible.value = !visible.value
}

const handleSelect = (emoji: EmojiData) => {
  emit('select', emoji)
  visible.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  if (
    visible.value &&
    !triggerRef.value?.contains(event.target as Node) &&
    !popperRef.value?.contains(event.target as Node)
  ) {
    visible.value = false
  }
}

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
  if (newVal) {
    // 标记已打开过，触发懒加载
    hasOpened.value = true
    nextTick(() => {
      initPopper()
    })
  }
})

watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
  }
)

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('resize', updatePopper)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('resize', updatePopper)
  if (popperInstance) {
    popperInstance.destroy()
  }
})
</script>

<style scoped>
.zh-emoji-selector {
  display: inline-block;
  position: relative;
}

.zh-emoji-trigger {
  padding: 4px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.zh-emoji-trigger:hover,
.zh-emoji-trigger.active {
  background-color: var(--el-color-primary-light-9);
}

.zh-emoji-icon {
  line-height: 1;
  display: inline-block;
  user-select: none;
}

.zh-emoji-text {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.zh-emoji-popper {
  position: fixed;
  top: 0;
  left: 0;
  pointer-events: auto;
}

/* 添加淡入淡出动画 */
.zh-fade-enter-active,
.zh-fade-leave-active {
  transition: opacity 0.2s ease;
}

.zh-fade-enter-from,
.zh-fade-leave-to {
  opacity: 0;
}
</style> 