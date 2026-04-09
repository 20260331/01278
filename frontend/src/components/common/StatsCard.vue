<template>
  <div class="group bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-xl hover:shadow-slate-200/50 hover:-translate-y-1 transition-all duration-300">
    <div class="flex items-center justify-between mb-4">
      <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110" :class="iconBgClass">
        <iconify-icon :icon="icon" width="24" :class="iconClass"></iconify-icon>
      </div>
      <span v-if="trend !== undefined" class="text-xs font-semibold px-2.5 py-1 rounded-full" :class="trendClass">
        {{ trend > 0 ? '+' : '' }}{{ trend }}%
      </span>
    </div>
    <p class="text-3xl font-serif font-medium text-slate-900 mb-1 tracking-tight">{{ value }}</p>
    <p class="text-sm text-slate-500">{{ label }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  icon: { type: String, required: true },
  value: { type: [String, Number], required: true },
  label: { type: String, required: true },
  color: { type: String, default: 'blue' },
  trend: { type: Number }
})

const iconBgClass = computed(() => {
  const colors = {
    blue: 'bg-blue-50',
    indigo: 'bg-indigo-50',
    emerald: 'bg-emerald-50',
    amber: 'bg-amber-50',
    rose: 'bg-rose-50'
  }
  return colors[props.color] || colors.blue
})

const iconClass = computed(() => {
  const colors = {
    blue: 'text-blue-500',
    indigo: 'text-indigo-500',
    emerald: 'text-emerald-500',
    amber: 'text-amber-500',
    rose: 'text-rose-500'
  }
  return colors[props.color] || colors.blue
})

const trendClass = computed(() => {
  if (props.trend > 0) {
    return 'bg-emerald-50 text-emerald-600'
  } else if (props.trend < 0) {
    return 'bg-rose-50 text-rose-600'
  }
  return 'bg-slate-100 text-slate-600'
})
</script>
