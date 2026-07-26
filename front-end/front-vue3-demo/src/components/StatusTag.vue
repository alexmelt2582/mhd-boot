<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    status: number | string
    type: 'reservation' | 'checkin' | 'user' | 'space' | 'blacklist' | 'equipment' | 'approval'
    size?: '' | 'small' | 'large'
    effect?: 'dark' | 'light' | 'plain'
  }>(),
  {
    size: 'small',
    effect: 'light',
  },
)

const statusMaps: Record<string, Record<string | number, { label: string; tagType: string }>> = {
  reservation: {
    0: { label: '已预约', tagType: 'primary' },
    1: { label: '已签到', tagType: 'success' },
    2: { label: '已完成', tagType: 'info' },
    3: { label: '已取消', tagType: 'warning' },
    4: { label: '已违约', tagType: 'danger' },
  },
  checkin: {
    0: { label: '待签到', tagType: 'warning' },
    1: { label: '已签到', tagType: 'success' },
    2: { label: '暂离中', tagType: 'warning' },
    3: { label: '暂离返回', tagType: 'primary' },
    4: { label: '已签退', tagType: 'info' },
    5: { label: '已违约', tagType: 'danger' },
  },
  user: {
    1: { label: '正常', tagType: 'success' },
    0: { label: '禁用', tagType: 'danger' },
  },
  space: {
    1: { label: '可用', tagType: 'success' },
    0: { label: '维修中', tagType: 'warning' },
    2: { label: '停用', tagType: 'danger' },
  },
  blacklist: {
    1: { label: '封禁中', tagType: 'danger' },
    0: { label: '已解封', tagType: 'info' },
  },
  equipment: {
    1: { label: '正常', tagType: 'success' },
    0: { label: '故障', tagType: 'danger' },
    2: { label: '报废', tagType: 'info' },
  },
  approval: {
    1: { label: '无需审批', tagType: 'info' },
    2: { label: '待审批', tagType: 'warning' },
    3: { label: '已通过', tagType: 'success' },
    4: { label: '已拒绝', tagType: 'danger' },
  },
}

const statusMap = computed(() => statusMaps[props.type] || {})

const tagType = computed(() => statusMap.value[props.status]?.tagType || 'info')
const label = computed(() => statusMap.value[props.status]?.label || String(props.status))
</script>
