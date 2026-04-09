<template>
  <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
    <div class="flex flex-wrap gap-4 items-center">
      <slot></slot>
      <el-button type="primary" @click="$emit('search')" class="!rounded-lg">
        <iconify-icon icon="lucide:search" width="16" class="mr-1"></iconify-icon>
        搜索
      </el-button>
      <el-button @click="$emit('reset')" class="!rounded-lg">
        <iconify-icon icon="lucide:rotate-ccw" width="16" class="mr-1"></iconify-icon>
        重置
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineEmits(['search', 'reset'])
</script>
