<template>
  <div
    v-show="!hidden && total > 0"
    class="pagination-container"
    :style="{ textAlign: textAlign }"
  >
    <el-pagination
      class="mt15"
      :background="background"
      :current-page="currentPageModel"
      :page-size="pageSizeModel"
      :page-sizes="pageSizes"
      :layout="layout"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  currentPage?: number
  pageSize?: number
  total?: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  textAlign?: 'left' | 'center' | 'right'
  hidden?: boolean
}>(), {
  currentPage: 1,
  pageSize: 10,
  total: 0,
  pageSizes: () => [10, 20, 30, 40, 50],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  textAlign: 'right',
  hidden: false
})

const emit = defineEmits<{
  (e: 'update:currentPage', val: number): void
  (e: 'update:pageSize', val: number): void
  (e: 'pagination', payload: { page: number; pageSize: number }): void
}>()

// 双向绑定计算属性
const currentPageModel = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

const pageSizeModel = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

// 页码改变
const handleCurrentChange = (val: number) => {
  currentPageModel.value = val
  emit('pagination', { page: val, pageSize: props.pageSize })
}

// 每页条数改变
const handleSizeChange = (val: number) => {
  // 如果当前页码在缩小每页条数后超出总页数，重置为第一页
  if (currentPageModel.value * val > props.total) {
    currentPageModel.value = 1
  }
  pageSizeModel.value = val
  emit('pagination', { page: currentPageModel.value, pageSize: val })
}
</script>

<style scoped>
.pagination-container {
  padding: 16px 0;
}
</style>
