<template>
  <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
    <el-table :data="data" v-loading="loading" style="width: 100%">
      <slot></slot>
    </el-table>
    <div v-if="showPagination" class="p-4 flex justify-between items-center border-t border-slate-100">
      <p class="text-sm text-slate-500">共 {{ total }} 条记录</p>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        layout="sizes, prev, pager, next"
        @change="handleChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  current: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  pageSizes: { type: Array, default: () => [10, 20, 50] },
  showPagination: { type: Boolean, default: true }
})

const emit = defineEmits(['update:current', 'update:size', 'change'])

const currentPage = computed({
  get: () => props.current,
  set: (val) => emit('update:current', val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val)
})

const handleChange = () => {
  emit('change')
}
</script>
