<template>
  <span 
    class="inline-flex items-center gap-1.5 text-sm"
    :class="statusClass"
  >
    <span class="w-2 h-2 rounded-full" :class="dotClass"></span>
    {{ text }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: [Number, String, Boolean], required: true },
  activeValue: { type: [Number, String, Boolean], default: 1 },
  activeText: { type: String, default: '正常' },
  inactiveText: { type: String, default: '禁用' },
  activeColor: { type: String, default: 'emerald' },
  inactiveColor: { type: String, default: 'slate' }
})

const isActive = computed(() => props.status === props.activeValue)

const text = computed(() => isActive.value ? props.activeText : props.inactiveText)

const statusClass = computed(() => {
  const colors = {
    emerald: 'text-emerald-600',
    blue: 'text-blue-600',
    amber: 'text-amber-600',
    rose: 'text-rose-600',
    slate: 'text-slate-400'
  }
  return colors[isActive.value ? props.activeColor : props.inactiveColor]
})

const dotClass = computed(() => {
  const colors = {
    emerald: 'bg-emerald-500',
    blue: 'bg-blue-500',
    amber: 'bg-amber-500',
    rose: 'bg-rose-500',
    slate: 'bg-slate-300'
  }
  return colors[isActive.value ? props.activeColor : props.inactiveColor]
})
</script>
