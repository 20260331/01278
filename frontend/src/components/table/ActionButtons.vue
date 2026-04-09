<template>
  <div class="flex items-center gap-2">
    <button 
      v-if="showEdit" 
      @click="$emit('edit')" 
      class="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
      title="编辑"
    >
      <iconify-icon icon="lucide:edit-3" width="16"></iconify-icon>
    </button>
    <button 
      v-if="showView" 
      @click="$emit('view')" 
      class="p-2 text-slate-400 hover:text-indigo-500 hover:bg-indigo-50 rounded-lg transition-colors"
      title="查看"
    >
      <iconify-icon icon="lucide:eye" width="16"></iconify-icon>
    </button>
    <button 
      v-if="showToggle" 
      @click="$emit('toggle')" 
      class="p-2 rounded-lg transition-colors"
      :class="toggleActive ? 'text-slate-400 hover:text-amber-500 hover:bg-amber-50' : 'text-slate-400 hover:text-emerald-500 hover:bg-emerald-50'"
      :title="toggleActive ? '禁用' : '启用'"
    >
      <iconify-icon :icon="toggleActive ? 'lucide:pause-circle' : 'lucide:play-circle'" width="16"></iconify-icon>
    </button>
    <button 
      v-if="showDelete" 
      @click="$emit('delete')" 
      class="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors"
      title="删除"
    >
      <iconify-icon icon="lucide:trash-2" width="16"></iconify-icon>
    </button>
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  showEdit: { type: Boolean, default: true },
  showView: { type: Boolean, default: false },
  showToggle: { type: Boolean, default: false },
  showDelete: { type: Boolean, default: true },
  toggleActive: { type: Boolean, default: true }
})

defineEmits(['edit', 'view', 'toggle', 'delete'])
</script>
