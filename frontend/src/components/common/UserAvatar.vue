<template>
  <div class="flex items-center gap-3">
    <div 
      class="rounded-full flex items-center justify-center flex-shrink-0"
      :class="[sizeClass, bgClass]"
    >
      <img v-if="avatar" :src="avatar" :alt="name" class="w-full h-full rounded-full object-cover" />
      <span v-else class="text-white font-medium" :class="textSizeClass">
        {{ initial }}
      </span>
    </div>
    <div v-if="showInfo">
      <p class="font-medium text-slate-900">{{ name }}</p>
      <p v-if="subtitle" class="text-xs text-slate-500">{{ subtitle }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
  avatar: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  size: { type: String, default: 'md' }, // sm, md, lg
  showInfo: { type: Boolean, default: true },
  bgColor: { type: String, default: 'slate-900' }
})

const initial = computed(() => (props.name || 'U').charAt(0).toUpperCase())

const sizeClass = computed(() => {
  const sizes = {
    sm: 'w-8 h-8',
    md: 'w-10 h-10',
    lg: 'w-12 h-12'
  }
  return sizes[props.size] || sizes.md
})

const textSizeClass = computed(() => {
  const sizes = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base'
  }
  return sizes[props.size] || sizes.md
})

const bgClass = computed(() => `bg-${props.bgColor}`)
</script>
