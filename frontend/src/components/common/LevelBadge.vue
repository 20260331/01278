<template>
  <span 
    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
    :class="levelClass"
  >
    <iconify-icon v-if="showIcon && level >= 3" icon="lucide:crown" width="12"></iconify-icon>
    {{ levelText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  level: { type: Number, default: 1 },
  showIcon: { type: Boolean, default: true },
  levelMap: {
    type: Object,
    default: () => ({
      1: { text: '普通', class: 'bg-slate-100 text-slate-600' },
      2: { text: '银卡', class: 'bg-slate-200 text-slate-700' },
      3: { text: '金卡', class: 'bg-amber-100 text-amber-700' },
      4: { text: '钻石', class: 'bg-blue-100 text-blue-700' }
    })
  }
})

const levelConfig = computed(() => props.levelMap[props.level] || props.levelMap[1])
const levelText = computed(() => levelConfig.value.text)
const levelClass = computed(() => levelConfig.value.class)
</script>
