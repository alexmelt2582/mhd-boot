<template>
  <div>
    <PageHeader title="仪表盘" description="图书馆运营核心数据概览" />

    <!-- Stat cards -->
    <div class="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div v-for="card in statCards" :key="card.label" class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition-shadow hover:shadow-md dark:border-white/5 dark:bg-slate-900">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-500 dark:text-slate-400">{{ card.label }}</p>
            <p class="mt-1 text-3xl font-bold text-slate-900 dark:text-slate-100">{{ card.value }}</p>
            <p :class="['mt-1 text-xs', card.up ? 'text-emerald-500' : 'text-red-500']">
              <el-icon><component :is="card.up ? 'Top' : 'Bottom'" /></el-icon>
              {{ card.change }}
            </p>
          </div>
          <div :class="['flex h-14 w-14 items-center justify-center rounded-xl', card.bg]">
            <el-icon :size="24" :color="card.iconColor"><component :is="card.icon" /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="mb-6 grid gap-6 lg:grid-cols-2">
      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
        <h3 class="mb-4 font-semibold text-slate-900 dark:text-slate-100">近7天预约趋势</h3>
        <div ref="trendChartRef" class="h-72"></div>
      </div>
      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
        <h3 class="mb-4 font-semibold text-slate-900 dark:text-slate-100">空间类型分布</h3>
        <div ref="pieChartRef" class="h-72"></div>
      </div>
    </div>

    <!-- Recent + Alerts -->
    <div class="grid gap-6 lg:grid-cols-2">
      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
        <h3 class="mb-4 font-semibold text-slate-900 dark:text-slate-100">最近预约</h3>
        <el-table :data="recentReservations" stripe size="small" class="dark:!bg-slate-900">
          <el-table-column prop="reservationCode" label="预约码" width="150" />
          <el-table-column prop="spaceName" label="空间" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }"><StatusTag :status="row.status" type="reservation" /></template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="140" />
        </el-table>
      </div>
      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
        <h3 class="mb-4 font-semibold text-slate-900 dark:text-slate-100">待处理审批</h3>
        <div v-if="pendingApprovals.length === 0" class="py-8 text-center text-slate-400 dark:text-slate-500">暂无待审批预约</div>
        <div v-for="item in pendingApprovals" :key="item.id" class="mb-3 rounded-lg bg-slate-50 p-3 dark:bg-slate-800">
          <div class="flex items-center justify-between">
            <span class="font-medium text-slate-900 dark:text-slate-100">{{ item.spaceName }}</span>
            <el-tag type="warning" size="small">待审批</el-tag>
          </div>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ item.userName }} · {{ item.createTime }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

const statCards = [
  { label: '今日预约数', value: 89, change: '↑ 12%', up: true, icon: 'Calendar', bg: 'bg-blue-50 dark:bg-blue-500/10', iconColor: '#3b82f6' },
  { label: '当前在馆人数', value: 67, change: '↑ 8%', up: true, icon: 'UserFilled', bg: 'bg-emerald-50 dark:bg-emerald-500/10', iconColor: '#10b981' },
  { label: '可用座位数', value: 98, change: '↓ 5%', up: false, icon: 'OfficeBuilding', bg: 'bg-amber-50 dark:bg-amber-500/10', iconColor: '#f59e0b' },
  { label: '今日违约率', value: '2.1%', change: '↓ 0.3%', up: true, icon: 'Warning', bg: 'bg-red-50 dark:bg-red-500/10', iconColor: '#ef4444' },
]

const recentReservations = [
  { id: 1, reservationCode: 'RES20260725001', spaceName: 'A区-01号座', status: 1, createTime: '2026-07-25 09:00' },
  { id: 2, reservationCode: 'RES20260725002', spaceName: '研讨室-301', status: 0, createTime: '2026-07-25 08:30' },
  { id: 3, reservationCode: 'RES20260725003', spaceName: 'B区-03号座', status: 0, createTime: '2026-07-25 08:15' },
  { id: 4, reservationCode: 'RES20260725004', spaceName: 'A区-05号座', status: 2, createTime: '2026-07-25 07:00' },
  { id: 5, reservationCode: 'RES20260725005', spaceName: '研讨室-302', status: 4, createTime: '2026-07-25 06:30' },
]

const pendingApprovals = [
  { id: 101, spaceName: '研讨室-302', userName: '李四', createTime: '2026-07-25 09:30' },
  { id: 102, spaceName: '研讨室-301', userName: '王五', createTime: '2026-07-24 16:00' },
]

function initTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const days = ['7/19', '7/20', '7/21', '7/22', '7/23', '7/24', '7/25']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预约数', '签到数'], bottom: 0 },
    grid: { left: 40, right: 20, top: 10, bottom: 30 },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value' },
    series: [
      { name: '预约数', type: 'line', data: [65, 72, 81, 78, 85, 92, 89], smooth: true, itemStyle: { color: '#3b82f6' } },
      { name: '签到数', type: 'line', data: [58, 65, 72, 70, 78, 85, 80], smooth: true, itemStyle: { color: '#10b981' } },
    ],
  })
  return chart
}

function initPieChart() {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['35%', '50%'],
      label: { show: false },
      data: [
        { value: 85, name: '安静学习区', itemStyle: { color: '#3b82f6' } },
        { value: 30, name: '电子阅览区', itemStyle: { color: '#10b981' } },
        { value: 12, name: '研讨区', itemStyle: { color: '#f59e0b' } },
        { value: 15, name: '多媒体区', itemStyle: { color: '#ef4444' } },
        { value: 14, name: '休闲阅读区', itemStyle: { color: '#8b5cf6' } },
      ],
    }],
  })
  return chart
}

onMounted(() => {
  const c1 = initTrendChart()
  const c2 = initPieChart()
  onBeforeUnmount(() => {
    c1?.dispose()
    c2?.dispose()
  })
})
</script>
