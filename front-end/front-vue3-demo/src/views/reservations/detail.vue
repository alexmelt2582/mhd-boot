<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <!-- Back button -->
    <el-button text class="mb-4" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon>
      返回
    </el-button>

    <div v-loading="loading">
      <template v-if="reservation">
        <div class="grid gap-6 lg:grid-cols-3">
          <!-- Left: Reservation info -->
          <div class="lg:col-span-2 space-y-4">
            <!-- Basic info card -->
            <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
              <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
                <h1 class="text-xl font-bold text-slate-900 dark:text-slate-100">{{ reservation.spaceName }}</h1>
                <div class="flex gap-2">
                  <StatusTag :status="reservation.status" type="reservation" />
                  <StatusTag :status="reservation.approvalStatus" type="approval" />
                </div>
              </div>

              <div class="space-y-3 text-sm">
                <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon><Ticket /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">预约码：</span>
                  <span class="font-mono text-primary">{{ reservation.reservationCode }}</span>
                </div>

                <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon><Calendar /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">日期：</span>
                  {{ formatDate(reservation.startTime) }}
                </div>

                <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon><Clock /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">时段：</span>
                  {{ formatTimeOnly(reservation.startTime) }} ~ {{ formatTimeOnly(reservation.endTime) }}
                  <el-tag size="small" type="info" class="ml-1">
                    {{ calcDuration(reservation.startTime, reservation.endTime) }}
                  </el-tag>
                </div>

                <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon><Location /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">类型：</span>
                  {{ SpaceTypeMap[reservation.spaceType] || reservation.spaceType || '-' }}
                </div>

                <div v-if="reservation.purpose" class="flex items-start gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon class="mt-0.5"><EditPen /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">用途：</span>
                  <span>{{ reservation.purpose }}</span>
                </div>

                <div
                  v-if="reservation.participants && reservation.participants.length > 0"
                  class="flex items-start gap-2 text-slate-500 dark:text-slate-400"
                >
                  <el-icon class="mt-0.5"><UserFilled /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">参与人：</span>
                  <span>{{ reservation.participants.join('、') }}</span>
                </div>

                <div class="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                  <el-icon><Timer /></el-icon>
                  <span class="font-medium text-slate-700 dark:text-slate-300">创建时间：</span>
                  {{ reservation.createTime }}
                </div>
              </div>
            </div>

            <!-- Cancel info card -->
            <div
              v-if="reservation.status === 3"
              class="rounded-xl border border-orange-200 bg-orange-50 p-5 dark:border-orange-800 dark:bg-orange-950"
            >
              <div class="flex items-center gap-2 text-sm font-medium text-orange-700 dark:text-orange-300">
                <el-icon><WarningFilled /></el-icon>
                预约已取消
              </div>
              <div class="mt-2 space-y-1 text-sm text-orange-600 dark:text-orange-400">
                <p>取消时间：{{ reservation.cancelTime || '-' }}</p>
                <p>取消原因：{{ reservation.cancelReason || '未填写' }}</p>
              </div>
            </div>

            <!-- Approval pending note -->
            <div
              v-if="reservation.approvalStatus === 2"
              class="rounded-xl border border-amber-200 bg-amber-50 p-5 dark:border-amber-800 dark:bg-amber-950"
            >
              <div class="flex items-center gap-2 text-sm font-medium text-amber-700 dark:text-amber-300">
                <el-icon><Clock /></el-icon>
                等待管理员审批
              </div>
              <p class="mt-1 text-sm text-amber-600 dark:text-amber-400">
                研讨室预约需要管理员审批，请耐心等待
              </p>
            </div>

            <!-- Approval rejected note -->
            <div
              v-if="reservation.approvalStatus === 4"
              class="rounded-xl border border-red-200 bg-red-50 p-5 dark:border-red-800 dark:bg-red-950"
            >
              <div class="flex items-center gap-2 text-sm font-medium text-red-700 dark:text-red-300">
                <el-icon><CircleCloseFilled /></el-icon>
                审批未通过
              </div>
              <p class="mt-1 text-sm text-red-600 dark:text-red-400">
                驳回原因：{{ reservation.approvalRemark || '未说明原因' }}
              </p>
            </div>

            <!-- Defaulted note -->
            <div
              v-if="reservation.status === 4"
              class="rounded-xl border border-red-200 bg-red-50 p-5 dark:border-red-800 dark:bg-red-950"
            >
              <div class="flex items-center gap-2 text-sm font-medium text-red-700 dark:text-red-300">
                <el-icon><WarningFilled /></el-icon>
                预约已违约
              </div>
              <p class="mt-1 text-sm text-red-600 dark:text-red-400">
                未在规定时间内签到，该预约已标记为违约。违约将扣除信用积分，请合理规划时间。
              </p>
            </div>
          </div>

          <!-- Right: Check-in panel -->
          <div class="lg:col-span-1">
            <div class="sticky top-24 rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
              <h2 class="mb-4 text-lg font-bold text-slate-900 dark:text-slate-100">签到管理</h2>

              <!-- Status: booked (0) -->
              <template v-if="reservation.status === 0">
                <!-- QR Code placeholder -->
                <div class="mb-4 flex flex-col items-center rounded-lg border-2 border-dashed border-slate-200 bg-slate-50 p-6 dark:border-slate-700 dark:bg-slate-800">
                  <el-icon :size="48" class="text-slate-300 dark:text-slate-600"><PictureFilled /></el-icon>
                  <p class="mt-2 text-xs text-slate-400 dark:text-slate-500">签到二维码</p>
                  <p class="mt-1 text-[10px] text-slate-300 dark:text-slate-600">请到图书馆扫码或使用下方按钮签到</p>
                </div>

                <el-button
                  type="primary"
                  size="large"
                  class="w-full"
                  :loading="checkinLoading"
                  @click="handleCheckin"
                >
                  <el-icon><CircleCheck /></el-icon>
                  立即签到
                </el-button>
                <p class="mt-2 text-center text-xs text-slate-400 dark:text-slate-500">
                  建议在预约时间开始前 15 分钟签到
                </p>
              </template>

              <!-- Status: checked-in (1) — exclude temp-leave which has its own section -->
              <template v-if="reservation.status === 1 && checkinLog && checkinLog.status !== 2">
                <div
                  v-if="checkinLog.status === 1 || checkinLog.status === 3"
                  class="mb-4 rounded-lg bg-emerald-50 p-4 text-center dark:bg-emerald-950"
                >
                  <el-icon class="text-3xl text-emerald-500"><CircleCheckFilled /></el-icon>
                  <p class="mt-1 font-medium text-emerald-700 dark:text-emerald-300">使用中</p>
                  <p class="text-xs text-emerald-500 dark:text-emerald-400">
                    签到于 {{ checkinLog.checkinTime?.slice(11, 16) || '-' }}
                  </p>
                </div>

                <div class="space-y-3">
                  <el-button
                    type="warning"
                    class="w-full"
                    :loading="tempLeaveLoading"
                    @click="handleTempLeave"
                  >
                    <el-icon><Timer /></el-icon>
                    暂离
                  </el-button>
                  <el-button
                    type="danger"
                    class="w-full"
                    plain
                    :loading="checkoutLoading"
                    @click="showCheckoutConfirm"
                  >
                    <el-icon><SwitchButton /></el-icon>
                    签退
                  </el-button>
                </div>
                <p class="mt-2 text-center text-xs text-slate-400 dark:text-slate-500">
                  暂离时限 30 分钟，超时将自动签退
                </p>
              </template>

              <!-- Status: temp-leave (2) -->
              <template v-if="checkinLog && checkinLog.status === 2">
                <div class="mb-4 rounded-lg bg-amber-50 p-4 text-center dark:bg-amber-950">
                  <el-icon class="text-3xl text-amber-500"><Timer /></el-icon>
                  <p class="mt-1 font-medium text-amber-700 dark:text-amber-300">暂离中</p>
                  <p class="text-xs text-amber-500 dark:text-amber-400">
                    离开于 {{ checkinLog.tempLeaveTime?.slice(11, 16) || '-' }}
                  </p>

                  <!-- Countdown timer -->
                  <div class="mt-3">
                    <p class="text-xs text-slate-400 dark:text-slate-500">剩余暂离时间</p>
                    <div class="mt-1 text-2xl font-mono font-bold" :class="countdownCritical ? 'text-red-500' : 'text-amber-600 dark:text-amber-400'">
                      {{ formatCountdown(remainingSeconds) }}
                    </div>
                    <el-progress
                      :percentage="countdownPercent"
                      :color="countdownCritical ? '#ef4444' : '#f59e0b'"
                      :show-text="false"
                      class="mt-1"
                    />
                  </div>
                </div>

                <el-button
                  type="primary"
                  class="w-full"
                  :loading="tempReturnLoading"
                  @click="handleTempReturn"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  返回签到
                </el-button>
              </template>

              <!-- Status: completed (2) -->
              <template v-if="reservation.status === 2">
                <div class="mb-4 rounded-lg bg-slate-100 p-4 text-center dark:bg-slate-800">
                  <el-icon class="text-3xl text-slate-400"><CircleCheckFilled /></el-icon>
                  <p class="mt-1 font-medium text-slate-500 dark:text-slate-400">已完成</p>
                  <p class="text-xs text-slate-400 dark:text-slate-500">本次使用已结束</p>
                </div>
              </template>

              <!-- Status: cancelled (3) -->
              <template v-if="reservation.status === 3">
                <div class="mb-4 rounded-lg bg-orange-50 p-4 text-center dark:bg-orange-950">
                  <el-icon class="text-3xl text-orange-400"><CircleCloseFilled /></el-icon>
                  <p class="mt-1 font-medium text-orange-600 dark:text-orange-300">已取消</p>
                  <p class="text-xs text-orange-500 dark:text-orange-400">该预约已被取消</p>
                </div>
              </template>

              <!-- Status: defaulted (4) -->
              <template v-if="reservation.status === 4">
                <div class="mb-4 rounded-lg bg-red-50 p-4 text-center dark:bg-red-950">
                  <el-icon class="text-3xl text-red-400"><WarningFilled /></el-icon>
                  <p class="mt-1 font-medium text-red-600 dark:text-red-300">已违约</p>
                  <p class="text-xs text-red-500 dark:text-red-400">未按时签到，已影响信用积分</p>
                </div>
              </template>

              <!-- Check-in timeline -->
              <div v-if="checkinLog" class="mt-6 border-t border-slate-100 pt-4 dark:border-slate-800">
                <h3 class="mb-3 text-sm font-semibold text-slate-700 dark:text-slate-300">签到时间线</h3>
                <el-timeline>
                  <el-timeline-item
                    v-if="checkinLog.checkinTime"
                    timestamp=""
                    placement="top"
                    color="#22c55e"
                  >
                    <div class="text-sm">
                      <span class="font-medium text-slate-700 dark:text-slate-300">签到</span>
                      <span class="ml-2 text-xs text-slate-400">{{ checkinLog.checkinTime?.slice(11, 19) || '' }}</span>
                    </div>
                  </el-timeline-item>
                  <el-timeline-item
                    v-if="checkinLog.tempLeaveTime"
                    timestamp=""
                    placement="top"
                    color="#f59e0b"
                  >
                    <div class="text-sm">
                      <span class="font-medium text-slate-700 dark:text-slate-300">暂离</span>
                      <span class="ml-2 text-xs text-slate-400">{{ checkinLog.tempLeaveTime?.slice(11, 19) || '' }}</span>
                    </div>
                  </el-timeline-item>
                  <el-timeline-item
                    v-if="checkinLog.tempReturnTime"
                    timestamp=""
                    placement="top"
                    color="#3b82f6"
                  >
                    <div class="text-sm">
                      <span class="font-medium text-slate-700 dark:text-slate-300">暂离返回</span>
                      <span class="ml-2 text-xs text-slate-400">{{ checkinLog.tempReturnTime?.slice(11, 19) || '' }}</span>
                    </div>
                  </el-timeline-item>
                  <el-timeline-item
                    v-if="checkinLog.checkoutTime"
                    timestamp=""
                    placement="top"
                    color="#6b7280"
                  >
                    <div class="text-sm">
                      <span class="font-medium text-slate-700 dark:text-slate-300">签退</span>
                      <span class="ml-2 text-xs text-slate-400">{{ checkinLog.checkoutTime?.slice(11, 19) || '' }}</span>
                    </div>
                  </el-timeline-item>
                  <el-timeline-item
                    v-if="!checkinLog.checkinTime && !checkinLog.tempLeaveTime && !checkinLog.checkoutTime"
                    timestamp=""
                    placement="top"
                    color="#9ca3af"
                  >
                    <div class="text-sm text-slate-400">暂无签到记录</div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Checkout confirmation dialog -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="确认签退"
      width="400px"
    >
      <el-alert
        title="签退后将结束本次使用"
        type="warning"
        :closable="false"
        show-icon
      />
      <p class="mt-4 text-sm text-slate-500 dark:text-slate-400">
        确认签退吗？签退后将无法继续使用该空间，如中途离开可以选择「暂离」。
      </p>
      <template #footer>
        <el-button @click="checkoutDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="checkoutLoading" @click="handleCheckout">确认签退</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { SpaceTypeMap } from '@/api/reservation/type'
import type { ReservationVO } from '@/api/reservation/type'
import type { CheckinLogVO } from '@/api/checkin/type'
import { getReservationById } from '@/api/reservation/api'
import { checkin, tempLeave, tempReturn, checkout, getCheckinLog } from '@/api/checkin/api'
import { meMsgSuccess, meMsgError } from '@/utils/modal'

const router = useRouter()
const route = useRoute()
const reservationId = Number(route.params.id)

const loading = ref(true)
const reservation = ref<ReservationVO | null>(null)
const checkinLog = ref<CheckinLogVO | null>(null)

// Button loading states
const checkinLoading = ref(false)
const tempLeaveLoading = ref(false)
const tempReturnLoading = ref(false)
const checkoutLoading = ref(false)
const checkoutDialogVisible = ref(false)

// Countdown timer
const TEMP_LEAVE_LIMIT = 30 * 60 // 30 minutes in seconds
const remainingSeconds = ref(0)
const countdownPercent = ref(0)
const countdownCritical = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

/** Format date */
function formatDate(dt: string): string {
  if (!dt) return '-'
  return dt.slice(0, 10)
}

function formatTimeOnly(dt: string): string {
  if (!dt || dt.length < 16) return dt || '-'
  return dt.slice(11, 16)
}

function calcDuration(start: string, end: string): string {
  if (!start || !end) return '-'
  const s = new Date(start.replace(/-/g, '/'))
  const e = new Date(end.replace(/-/g, '/'))
  const diffMs = e.getTime() - s.getTime()
  const hours = Math.floor(diffMs / 3600000)
  const mins = Math.floor((diffMs % 3600000) / 60000)
  if (hours > 0 && mins > 0) return `${hours}小时${mins}分钟`
  if (hours > 0) return `${hours}小时`
  return `${mins}分钟`
}

function formatCountdown(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/** Start countdown timer */
function startCountdown(tempLeaveTime: string) {
  stopCountdown()
  const leaveDt = new Date(tempLeaveTime.replace(/-/g, '/'))
  const elapsed = Math.floor((Date.now() - leaveDt.getTime()) / 1000)
  const remaining = Math.max(0, TEMP_LEAVE_LIMIT - elapsed)
  remainingSeconds.value = remaining
  countdownPercent.value = 100 - Math.floor((remaining / TEMP_LEAVE_LIMIT) * 100)
  countdownCritical.value = remaining <= 5 * 60 // Critical under 5 min

  countdownTimer = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      stopCountdown()
      return
    }
    remainingSeconds.value--
    countdownPercent.value = 100 - Math.floor((remainingSeconds.value / TEMP_LEAVE_LIMIT) * 100)
    countdownCritical.value = remainingSeconds.value <= 5 * 60
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

/** Fetch reservation detail */
async function fetchDetail() {
  loading.value = true
  try {
    const res = await getReservationById(reservationId)
    if (res.code === 0) {
      reservation.value = res.data

      // If checked-in or later, fetch checkin log
      if ([1, 2, 4].includes(res.data.status)) {
        try {
          const logRes = await getCheckinLog(reservationId)
          if (logRes.code === 0) {
            checkinLog.value = logRes.data

            // Start countdown if temp-leave
            if (logRes.data.status === 2 && logRes.data.tempLeaveTime) {
              startCountdown(logRes.data.tempLeaveTime)
            }
          }
        } catch {
          // checkin log might not exist
        }
      }
    }
  } finally {
    loading.value = false
  }
}

/** Checkin */
async function handleCheckin() {
  checkinLoading.value = true
  try {
    const res = await checkin(reservationId)
    if (res.code === 0) {
      meMsgSuccess({ message: '签到成功' })
      checkinLog.value = res.data
      await fetchDetail()
    }
  } finally {
    checkinLoading.value = false
  }
}

/** Temp leave */
async function handleTempLeave() {
  tempLeaveLoading.value = true
  try {
    const res = await tempLeave(reservationId)
    if (res.code === 0) {
      meMsgSuccess({ message: '已暂离，请在 30 分钟内返回' })
      await fetchDetail()
    }
  } finally {
    tempLeaveLoading.value = false
  }
}

/** Temp return */
async function handleTempReturn() {
  tempReturnLoading.value = true
  try {
    const res = await tempReturn(reservationId)
    if (res.code === 0) {
      meMsgSuccess({ message: '已返回签到' })
      stopCountdown()
      await fetchDetail()
    }
  } finally {
    tempReturnLoading.value = false
  }
}

function showCheckoutConfirm() {
  checkoutDialogVisible.value = true
}

/** Checkout */
async function handleCheckout() {
  checkoutLoading.value = true
  try {
    const res = await checkout(reservationId)
    if (res.code === 0) {
      meMsgSuccess({ message: '签退成功' })
      checkoutDialogVisible.value = false
      stopCountdown()
      await fetchDetail()
    }
  } finally {
    checkoutLoading.value = false
  }
}

onMounted(() => fetchDetail())
onUnmounted(() => stopCountdown())
</script>
