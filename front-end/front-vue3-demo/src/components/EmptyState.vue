<template>
  <div class="flex flex-col items-center justify-center py-16 text-slate-400 dark:text-slate-500">
    <el-empty :description="description" :image-size="imageSize">
      <template v-if="$slots.default" #default>
        <slot />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    description?: string
    imageSize?: number
  }>(),
  {
    description: '暂无数据',
    imageSize: 120,
  },
)
</script>
