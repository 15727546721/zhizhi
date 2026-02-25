import { ref, watch, type Ref } from 'vue'

/**
 * 防抖 Hook
 * @param initialValue - 初始值
 * @param delay - 延迟时间（毫秒）
 */
export function useDebounce<T>(initialValue: T, delay = 300) {
  const value = ref(initialValue) as Ref<T>
  const debouncedValue = ref(initialValue) as Ref<T>
  let timer: ReturnType<typeof setTimeout> | null = null

  watch(value, (newValue) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  })

  return {
    value,
    debouncedValue
  }
}

/**
 * 防抖函数 Hook
 * @param fn - 要防抖的函数
 * @param delay - 延迟时间（毫秒）
 */
export function useDebounceFn<T extends (...args: any[]) => any>(fn: T, delay = 300) {
  let timer: ReturnType<typeof setTimeout> | null = null

  const run = (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
    }, delay)
  }

  const cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return { run, cancel }
}

/**
 * 节流函数 Hook
 * @param fn - 要节流的函数
 * @param interval - 间隔时间（毫秒）
 */
export function useThrottleFn<T extends (...args: any[]) => any>(fn: T, interval = 1000) {
  let lastTime = 0
  let timer: ReturnType<typeof setTimeout> | null = null

  const run = (...args: Parameters<T>) => {
    const now = Date.now()
    const remaining = interval - (now - lastTime)

    if (remaining <= 0) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      lastTime = now
      fn(...args)
    } else if (!timer) {
      timer = setTimeout(() => {
        lastTime = Date.now()
        timer = null
        fn(...args)
      }, remaining)
    }
  }

  const cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return { run, cancel }
}
