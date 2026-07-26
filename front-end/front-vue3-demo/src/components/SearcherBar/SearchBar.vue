<template>
  <div class="flex flex-wrap items-center gap-4 p-4 mb-4 bg-white rounded-lg shadow-sm">
    <template v-for="item in config" :key="item.field">
      <!-- 输入框 -->
      <div v-if="item.type === 'input'" class="flex items-center gap-2">
        <label class="text-sm text-gray-600 whitespace-nowrap">{{ item.label }}</label>
        <el-input
          v-model="form[item.field]"
          :placeholder="item.placeholder ?? `请输入${item.label}`"
          :clearable="item.clearable ?? true"
          class="!w-44"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 下拉选择 -->
      <div v-else-if="item.type === 'select'" class="flex items-center gap-2">
        <label class="text-sm text-gray-600 whitespace-nowrap">{{ item.label }}</label>
        <el-select
          v-model="form[item.field]"
          :placeholder="item.placeholder ?? `请选择${item.label}`"
          :clearable="item.clearable ?? true"
          :filterable="item.filterable ?? false"
          class="!w-44"
        >
          <el-option
            v-for="opt in item.options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>

      <!-- 日期范围 -->
      <div v-else-if="item.type === 'daterange'" class="flex items-center gap-2">
        <label class="text-sm text-gray-600 whitespace-nowrap">{{ item.label }}</label>
        <el-date-picker
          v-model="dateRange[item.field]"
          type="daterange"
          :value-format="item.valueFormat ?? 'YYYY-MM-DD'"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :clearable="item.clearable ?? true"
          class="!w-60"
        />
      </div>
    </template>

    <!-- 搜索/重置按钮 + 额外插槽 -->
    <div class="flex items-center gap-2 ml-auto">
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <!-- 可插入自定义按钮（如导出、新增等） -->
      <slot name="extraButtons"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive} from 'vue'
import type {SearchConfigItem} from './type.ts'

const props = defineProps<{
  config: SearchConfigItem[]
}>()

const emit = defineEmits<{
  (e: 'search', value: Record<string, any>): void
}>()

// 普通字段数据
const form: Record<string, any> = reactive({})
// 日期范围数据
const dateRange: Record<string, [string, string] | null> = reactive({})

// 初始化
props.config.forEach(item => {
  if (item.type === 'daterange') {
    dateRange[item.field] = null
  } else {
    form[item.field] = ''
  }
})

// 组装参数
const buildParams = (): Record<string, any> => {
  const params: Record<string, any> = {}

  // 普通字段（过滤空值）
  Object.keys(form).forEach(key => {
    const val = form[key]
    if (val !== '' && val !== undefined && val !== null) {
      params[key] = val
    }
  })

  // 日期范围
  Object.keys(dateRange).forEach(key => {
    const range = dateRange[key]
    if (range && range.length === 2) {
      const configItem = props.config.find(c => c.field === key)
      if (configItem?.startField && configItem?.endField) {
        params[configItem.startField] = range[0]
        params[configItem.endField] = range[1]
      } else {
        // 默认后缀拼接
        params[`${key}Start`] = range[0]
        params[`${key}End`] = range[1]
      }
    }
  })

  return params
}

const handleSearch = () => {
  emit('search', buildParams())
}

const handleReset = () => {
  // 重置所有字段
  props.config.forEach(item => {
    if (item.type === 'daterange') {
      dateRange[item.field] = null
    } else {
      form[item.field] = ''
    }
  })
  handleSearch() // 重置后立即触发搜索，刷新列表
}
</script>
