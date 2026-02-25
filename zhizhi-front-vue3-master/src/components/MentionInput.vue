<template>
  <div class="mention-input-container" ref="containerRef">
    <el-input
      ref="inputRef"
      v-model="localValue"
      :type="type"
      :rows="rows"
      :placeholder="placeholder"
      :disabled="disabled"
      :resize="resize"
      @input="handleInput"
      @keydown="handleKeydown"
      @blur="handleBlur"
      @focus="handleFocus"
    />
    
    <!-- @用户选择弹窗 -->
    <div 
      v-show="showSuggestions && suggestions.length > 0" 
      class="mention-suggestions"
      :style="suggestionsStyle"
    >
      <div class="suggestions-header">
        <span>选择要@的用户（仅限关注）</span>
        <span class="suggestions-hint">↑↓选择 Enter确认 Esc取消</span>
      </div>
      <div class="suggestions-list">
        <div
          v-for="(user, index) in suggestions"
          :key="user.id"
          :class="['suggestion-item', { active: index === activeIndex }]"
          @click="selectUser(user)"
          @mouseenter="activeIndex = index"
        >
          <el-avatar :size="32" :src="user.avatar" />
          <div class="user-info">
            <span class="nickname">{{ user.nickname || user.username }}</span>
            <span class="username">@{{ user.username }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import type { InputInstance } from 'element-plus'
import { searchFollowingUsers } from '@/api/follow'

interface MentionUser {
  id: number
  userId: number
  username: string
  nickname: string
  avatar: string
}

interface Props {
  modelValue?: string
  type?: string
  rows?: number
  placeholder?: string
  disabled?: boolean
  resize?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  type: 'textarea',
  rows: 3,
  placeholder: '输入内容，@可以提及用户',
  disabled: false,
  resize: 'none'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  mention: [data: { userId: number; username: string; nickname: string }]
  enter: [event: KeyboardEvent]
}>()

const containerRef = ref<HTMLDivElement | null>(null)
const inputRef = ref<InputInstance | null>(null)
const localValue = ref(props.modelValue)
const showSuggestions = ref(false)
const suggestions = ref<MentionUser[]>([])
const activeIndex = ref(0)
const mentionStart = ref(-1)
const searchKeyword = ref('')
const loading = ref(false)

// 弹窗位置样式
const suggestionsStyle = computed(() => {
  return {
    position: 'absolute' as const,
    zIndex: 1000
  }
})

// 同步外部值
watch(() => props.modelValue, (newVal) => {
  if (newVal !== localValue.value) {
    localValue.value = newVal
  }
})

// 处理输入
const handleInput = async (value: string | number) => {
  const strValue = String(value)
  localValue.value = strValue
  emit('update:modelValue', strValue)
  
  // 检测@符号
  const cursorPos = getCursorPosition()
  const textBeforeCursor = strValue.substring(0, cursorPos)
  
  // 查找最后一个@符号
  const lastAtIndex = textBeforeCursor.lastIndexOf('@')
  
  if (lastAtIndex !== -1) {
    // 检查@前面是否是空格或开头
    const charBefore = lastAtIndex > 0 ? textBeforeCursor[lastAtIndex - 1] : ' '
    if (charBefore === ' ' || charBefore === '\n' || lastAtIndex === 0) {
      // 获取@后面的搜索词
      const keyword = textBeforeCursor.substring(lastAtIndex + 1)
      
      // 检查搜索词中是否包含空格（说明@已经结束）
      if (!keyword.includes(' ') && !keyword.includes('\n')) {
        mentionStart.value = lastAtIndex
        searchKeyword.value = keyword
        
        // 无论关键词是否为空都搜索（空关键词返回全部关注用户）
        await searchMentionUsers(keyword)
        return
      }
    }
  }
  
  // 没有检测到有效的@，关闭弹窗
  closeSuggestions()
}

// 搜索用户
const searchMentionUsers = async (keyword: string) => {
  loading.value = true
  try {
    const response = await searchFollowingUsers(keyword, 8)
    if (response && response.data) {
      // 结果中 userId 字段是 id
      suggestions.value = (response.data || []).map(u => ({
        id: (u as any).userId || u.id,
        userId: (u as any).userId || u.id,
        username: u.username,
        nickname: u.nickname || '',
        avatar: u.avatar || ''
      }))
      showSuggestions.value = suggestions.value.length > 0
      activeIndex.value = 0
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    suggestions.value = []
    showSuggestions.value = false
  } finally {
    loading.value = false
  }
}

// 选择用户
const selectUser = (user: MentionUser) => {
  if (mentionStart.value === -1) return
  
  const beforeMention = localValue.value.substring(0, mentionStart.value)
  const afterMention = localValue.value.substring(mentionStart.value + 1 + searchKeyword.value.length)
  
  // 插入@nickname格式（用户看到的是昵称，后端通过mentionUserIds处理）
  const mentionText = `@${user.nickname || user.username} `
  localValue.value = beforeMention + mentionText + afterMention
  
  emit('update:modelValue', localValue.value)
  emit('mention', {
    userId: user.id,
    username: user.username,
    nickname: user.nickname
  })
  
  closeSuggestions()
  
  // 设置光标位置到插入的@后面
  nextTick(() => {
    const newCursorPos = beforeMention.length + mentionText.length
    setCursorPosition(newCursorPos)
  })
}

// 关闭建议弹窗
const closeSuggestions = () => {
  showSuggestions.value = false
  suggestions.value = []
  activeIndex.value = 0
  mentionStart.value = -1
  searchKeyword.value = ''
}

// 键盘事件处理
const handleKeydown = (event: KeyboardEvent) => {
  if (showSuggestions.value && suggestions.value.length > 0) {
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault()
        activeIndex.value = (activeIndex.value + 1) % suggestions.value.length
        break
      case 'ArrowUp':
        event.preventDefault()
        activeIndex.value = (activeIndex.value - 1 + suggestions.value.length) % suggestions.value.length
        break
      case 'Enter':
        if (!event.shiftKey) {
          event.preventDefault()
          selectUser(suggestions.value[activeIndex.value])
        }
        break
      case 'Escape':
        event.preventDefault()
        closeSuggestions()
        break
      case 'Tab':
        event.preventDefault()
        selectUser(suggestions.value[activeIndex.value])
        break
    }
  } else if (event.key === 'Enter' && !event.shiftKey) {
    // 没有弹窗时，Enter键触发提交
    event.preventDefault()
    emit('enter', event)
  }
}

// 失去焦点
const handleBlur = () => {
  // 延迟关闭，以便点击选项时能触发
  setTimeout(() => {
    closeSuggestions()
  }, 200)
}

// 获取焦点
const handleFocus = () => {
  // 如果有正在搜索的关键词，重新显示建议
  if (searchKeyword.value && suggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

// 获取光标位置
const getCursorPosition = (): number => {
  const el = inputRef.value?.$el?.querySelector('textarea') || inputRef.value?.$el?.querySelector('input')
  return el?.selectionStart || 0
}

// 设置光标位置
const setCursorPosition = (pos: number) => {
  const el = inputRef.value?.$el?.querySelector('textarea') || inputRef.value?.$el?.querySelector('input')
  if (el) {
    el.focus()
    el.setSelectionRange(pos, pos)
  }
}

// 暴露方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur(),
  clear: () => {
    localValue.value = ''
    emit('update:modelValue', '')
  }
})
</script>

<style scoped>
.mention-input-container {
  position: relative;
  width: 100%;
}

.mention-suggestions {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 100%;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-height: 320px;
  overflow: hidden;
}

.suggestions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
  color: #909399;
}

.suggestions-hint {
  font-size: 11px;
  color: #c0c4cc;
}

.suggestions-list {
  max-height: 260px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover,
.suggestion-item.active {
  background-color: #f5f7fa;
}

.suggestion-item .user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.suggestion-item .nickname {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.suggestion-item .username {
  font-size: 12px;
  color: #909399;
}
</style>
