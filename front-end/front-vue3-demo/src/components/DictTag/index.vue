<template>
  <div class="dict-tag-wrapper">
    <!--
      遍历所有字典选项，只渲染与当前值匹配的项
      支持单值和多值展示
    -->
    <template v-for="(item, index) in options" :key="item.value ?? index">
      <template v-if="values.includes(String(item.value))">
        <!--
          情况1：无标签样式或 default 类型 → 纯文本展示
          适用于不需要视觉强调的字段
        -->
        <span
          v-if="!item.elTagType || item.elTagType === 'default'"
          :class="item.elTagClass"
        >
          {{ item.label }}
        </span>

        <!--
          情况2：有指定标签类型 → 使用 el-tag 渲染
          - primary 类型使用默认样式（Element Plus 的 primary 就是默认蓝色）
          - 其他类型直接传递
        -->
        <el-tag
          v-else
          :type="item.elTagType === 'primary' ? '' : item.elTagType"
          :class="item.elTagClass"
          :disable-transitions="true"
        >
          {{ item.label }}
        </el-tag>
      </template>
    </template>

    <!-- 当没有匹配项时，显示原始值 -->
    <span v-if="!hasMatch && showRawValue" class="dict-tag-raw-value">
      {{ displayRawValue }}
    </span>
  </div>
</template>

<script setup lang="ts">
/**
 * DictTag - 字典标签渲染组件
 *
 * @description
 * 根据字典选项列表和当前值，自动渲染对应的标签或文本。
 * 支持单值和多值展示，支持自定义标签类型和样式。
 *
 * @example
 * // 基础用法 - 单值
 * <DictTag :options="statusOptions" :value="'normal'" />
 *
 * // 多值用法
 * <DictTag :options="statusOptions" :value="['normal', 'disabled']" />
 *
 * // 配合字典缓存使用
 * <DictTag
 *   :options="getDictOptions(DICT_TYPES.NORMAL_STATUS)"
 *   :value="row.status"
 * />
 */

import { computed, type PropType } from 'vue';

// ==================== 类型定义 ====================
/** 字典选项类型 */
interface DictOption {
  /** 显示标签 */
  label: string
  /** 字典值 */
  value: string | number
  /**
   * el-tag 类型
   * - primary: 主要（蓝色）
   * - success: 成功（绿色）
   * - warning: 警告（橙色）
   * - danger: 危险（红色）
   * - info: 信息（灰色）
   * - default: 默认（纯文本，不使用 el-tag）
   */
  elTagType?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default' | ''
  /** 自定义 CSS 类名 */
  elTagClass?: string
}

// ==================== Props ====================

const props = defineProps({
	/** 字典选项列表 */
	options: {
		type: Array as PropType<DictOption[]>,
		default: () => [],
	},
  /**
   * 当前值
   * - 单值：string | number
   * - 多值：(string | number)[]
   */
	value: {
		type: [Number, String, Array] as PropType<number | string | (string | number)[]>,
		default: undefined,
	},
  /**
   * 是否显示原始值
   * 当没有匹配项时，如果为 true 则显示原始值，否则不显示
   */
  showRawValue: {
    type: Boolean,
    default: true,
  },
  /**
   * 分隔符
   * 多值显示时使用的分隔符
   */
  separator: {
    type: String,
    default: ' ',
  },
});

// ==================== 计算属性 ====================
/**
 * 将 value 统一转为字符串数组
 * 便于模板中用 includes 匹配
 */
const values = computed<string[]>(() => {
  if (props.value == null || props.value === '') return []
  // 统一转为数组
  const arr = Array.isArray(props.value) ? props.value : [props.value]
  // 统一转为字符串进行比较
  return arr.map(item => String(item))
})
/**
 * 是否至少有一个匹配项
 */
const hasMatch = computed(() => {
  if (values.value.length === 0) return false

  const optionValues = props.options.map(item => String(item.value))
  return values.value.some(v => optionValues.includes(v))
})

/**
 * 原始值展示文本
 * 多值用分隔符连接
 */
const displayRawValue = computed(() => {
  if (props.value == null) return ''
  return Array.isArray(props.value)
    ? props.value.join(props.separator)
    : String(props.value)
})
</script>

<style scoped>
.dict-tag-wrapper {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

/* 多个 el-tag 之间的间距 */
.dict-tag-wrapper :deep(.el-tag + .el-tag) {
  margin-left: 0;
}

/* 原始值展示样式 */
.dict-tag-raw-value {
  color: #909399;
  font-size: 13px;
}
</style>
