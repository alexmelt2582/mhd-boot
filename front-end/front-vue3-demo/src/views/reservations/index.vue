<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <PageHeader title="我的预约" description="查看和管理您的所有预约记录" />

    <!-- Status tabs -->
    <div class="mb-6">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane
          v-for="tab in statusTabOptions"
          :key="String(tab.value)"
          :label="tab.label"
          :name="String(tab.value)"
        />
      </el-tabs>
    </div>

    <!-- Filters -->
    <div class="mb-6 flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">空间类型</label>
        <el-select v-model="query.spaceType" placeholder="全部类型" class="w-32" @change="doSearch">
          <el-option v-for="o in spaceTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">日期范围</label>
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="w-64"
          value-format="YYYY-MM-DD"
          @change="onDateRangeChange"
        />
      </div>
      <el-button type="primary" @click="doSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button text @click="resetFilters">重置</el-button>
    </div>

    <!-- Reservation cards -->
    <div v-loading="loading" class="min-h-[300px]">
      <div v-if="reservationList.length === 0 && !loading" class="py-16">
        <EmptyState description="暂无预约记录">
          <el-button type="primary" @click="router.push('/spaces')">去预约</el-button>
        </EmptyState>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="item in reservationList"
          :key="item.id"
          class="group rounded-xl border border-slate-200 bg-white p-5 transition-all hover:border-primary/30 hover:shadow-md dark:border-white/5 dark:bg-slate-900"
        >
          <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <!-- Left: info -->
            <div class="flex-1 space-y-2">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="text-lg font-bold text-slate-900 dark:text-slate-100">{{ item.spaceName }}</h3>
                <StatusTag :status="item.status" type="reservation" />
                <StatusTag :status="item.approvalStatus" type="approval" />
              </div>

              <div class="flex flex-wrap items-center gap-4 text-sm text-slate-500 dark:text-slate-400">
                <span class="inline-flex items-center gap-1">
                  <el-icon><Calendar /></el-icon>
                  预约码：{{ item.reservationCode }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(item.startTime) }} ~ {{ formatTimeOnly(item.endTime) }}
                </span>
                <span v-if="item.spaceType" class="inline-flex items-center gap-1">
                  <el-icon><Location /></el-icon>
                  {{ SpaceTypeMap[item.spaceType] || item.spaceType }}
                </span>
              </div>

              <div v-if="item.purpose" class="text-sm text-slate-400 dark:text-slate-500">
                <el-icon class="mr-1"><EditPen /></el-icon>
                用途：{{ item.purpose }}
              </div>

              <div v-if="item.participants && item.participants.length > 0" class="text-sm text-slate-400 dark:text-slate-500">
                <el-icon class="mr-1"><UserFilled /></el-icon>
                参与人：{{ item.participants.join('、') }}
              </div>

              <!-- Cancel info -->
              <div v-if="item.status === 3 && item.cancelTime" class="text-xs text-orange-500 dark:text-orange-400">
                取消时间：{{ item.cancelTime }} | 原因：{{ item.cancelReason || '未填写' }}
              </div>

              <!-- Approval status note -->
              <div v-if="item.approvalStatus === 2" class="text-xs text-amber-500 dark:text-amber-400">
                等待管理员审批中...
              </div>
              <div v-if="item.approvalStatus === 4 && item.approvalRemark" class="text-xs text-red-500 dark:text-red-400">
                驳回原因：{{ item.approvalRemark }}
              </div>
            </div>

            <!-- Right: actions -->
            <div class="flex flex-shrink-0 items-center gap-2">
              <el-button
                v-if="item.status === 0"
                type="primary"
                size="small"
                @click="handleCheckin(item)"
              >
                立即签到
              </el-button>
              <el-button
                v-if="item.status === 0"
                type="danger"
                size="small"
                plain
                @click="showCancelDialog(item)"
              >
                取消预约
              </el-button>
              <el-button
                size="small"
                plain
                @click="goDetail(item.id)"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        background
        layout="prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <!-- Cancel confirmation dialog -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预约"
      width="460px"
      :close-on-click-modal="false"
    >
      <div class="space-y-4">
        <el-alert
          title="温馨提示"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="list-inside list-disc text-sm">
              <li>取消预约将扣除 <span class="font-bold text-red-500">2</span> 信用积分（距开始时间不足2小时扣除 <span class="font-bold text-red-500">5</span> 分）</li>
              <li>频繁取消预约可能影响您的信用等级</li>
              <li>如有疑问请联系管理员</li>
            </ul>
          </template>
        </el-alert>

        <div>
          <label class="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300">
            取消原因
          </label>
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请填写取消原因（必填）"
            maxlength="200"
            show-word-limit
          />
        </div>

        <div class="rounded-lg bg-slate-50 p-3 text-xs text-slate-500 dark:bg-slate-800 dark:text-slate-400">
          <p>当前信用积分充足，取消后预计不影响正常预约权限。</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="cancelDialogVisible = false">再想想</el-button>
        <el-button
          type="danger"
          :loading="canceling"
          :disabled="!cancelForm.reason.trim()"
          @click="confirmCancel"
        >
          确认取消
        </el-button>
      </template>
    </el-dialog>

    <!-- Checkin success dialog -->
    <el-dialog v-model="checkinSuccessVisible" title="签到成功" width="400px" center>
      <div class="text-center">
        <el-icon class="text-5xl text-emerald-500"><CircleCheckFilled /></el-icon>
        <p class="mt-4 text-lg font-bold text-slate-900 dark:text-slate-100">签到成功!</p>
        <div class="mt-2 rounded-lg bg-slate-50 p-4 dark:bg-slate-800">
          <p class="text-sm text-slate-500">签到空间</p>
          <p class="mt-1 text-lg font-bold text-primary">{{ checkedInSpace }}</p>
        </div>
        <p class="mt-2 text-xs text-slate-400">请遵守图书馆使用规定，保持安静</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="checkinSuccessVisible = false; fetchList()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { ReservationVO } from '@/api/reservation/type'
import { SpaceTypeMap } from '@/api/reservation/type'
import { getMyReservations, cancelReservation } from '@/api/reservation/api'
import { checkin } from '@/api/checkin/api'
import { statusTabOptions, spaceTypeOptions, defaultSearchParams } from './data'
import { meMsgSuccess, meMsgError } from '@/utils/modal'

const router = useRouter()

const loading = ref(false)
const reservationList = ref<ReservationVO[]>([])
const total = ref(0)
const activeTab = ref('')

const query = reactive({ ...defaultSearchParams })

// Cancel dialog
const cancelDialogVisible = ref(false)
const canceling = ref(false)
const cancelTarget = ref<ReservationVO | null>(null)
const cancelForm = reactive({ reason: '' })

// Checkin success
const checkinSuccessVisible = ref(false)
const checkedInSpace = ref('')

/** Format date time */
function formatDateTime(dt: string): string {
  if (!dt) return ''
  return dt.replace(' ', ' ').slice(0, 16)
}

function formatTimeOnly(dt: string): string {
  if (!dt || dt.length < 16) return dt || ''
  return dt.slice(11, 16)
}

/** Tab change */
function onTabChange(val: string | number) {
  query.status = val === '' ? undefined : (Number(val) as any)
  doSearch()
}

/** Date range change */
function onDateRangeChange(val: [string, string] | null) {
  if (val && val.length === 2) {
    query.startTime = val[0]
    query.endTime = val[1]
  } else {
    query.startTime = ''
    query.endTime = ''
  }
  doSearch()
}

function resetFilters() {
  query.spaceType = ''
  query.dateRange = []
  query.startTime = ''
  query.endTime = ''
  query.keyword = ''
  activeTab.value = ''
  doSearch()
}

function doSearch() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMyReservations({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status,
      spaceType: query.spaceType || undefined,
      startTime: query.startTime || undefined,
      endTime: query.endTime || undefined,
    })
    if (res.code === 0) {
      reservationList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/my-reservations/${id}`)
}

function showCancelDialog(item: ReservationVO) {
  cancelTarget.value = item
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value || !cancelForm.reason.trim()) return
  canceling.value = true
  try {
    const res = await cancelReservation(cancelTarget.value.id, cancelForm.reason.trim())
    if (res.code === 0) {
      meMsgSuccess({ message: '预约已取消' })
      cancelDialogVisible.value = false
      fetchList()
    }
  } finally {
    canceling.value = false
  }
}

async function handleCheckin(item: ReservationVO) {
  try {
    const res = await checkin(item.id)
    if (res.code === 0) {
      checkedInSpace.value = item.spaceName || ''
      checkinSuccessVisible.value = true
    }
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => fetchList())
</script>
