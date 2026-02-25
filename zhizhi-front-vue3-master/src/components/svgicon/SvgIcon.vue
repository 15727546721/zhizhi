<template>
  <svg :class="svgClass" aria-hidden="true" :style="{marginRight:`${mr}px`}">
    <use :xlink:href="iconName"/>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  name: string
  color?: string
  mr?: string
}

const props = withDefaults(defineProps<Props>(), {
  name: '',
  color: '',
  mr: '0'
})

const iconName = computed(() => `#icon-${props.name}`)
const svgClass = computed(() => {
  if (props.name) {
    return `svg-icon icon-${props.name}`
  }
  return 'svg-icon'
})
</script>

<style lang='scss'>
.svg-icon {
  width: 1em;
  height: 1em;
  fill: currentColor;
  vertical-align: middle;
}
</style>
