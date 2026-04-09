<template>
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">{{ title }}</h1>
      <p v-if="subtitle" class="text-sm text-slate-500">{{ subtitle }}</p>
    </div>
    <div class="flex items-center gap-3">
      <slot name="actions">
        <button 
          v-if="showAddButton" 
          @click="$emit('add')" 
          class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200"
        >
          <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
          {{ addButtonText }}
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  showAddButton: { type: Boolean, default: false },
  addButtonText: { type: String, default: '新增' }
})

defineEmits(['add'])
</script>
