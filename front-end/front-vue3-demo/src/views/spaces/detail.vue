<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <div v-loading="loading">
      <div v-if="space" class="grid gap-6 lg:grid-cols-3">
        <!-- Left: Space info -->
        <div class="lg:col-span-2">
          <el-button text class="mb-4" @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>

          <!-- Image placeholder -->
          <div class="mb-6 flex h-64 items-center justify-center rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 dark:from-primary/30 dark:to-secondary/30">
            <svg class="h-24 w-24 text-primary/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>

          <h1 class="text-2xl font-bold text-slate-900 dark:text-slate-100">{{ space.spaceName }}</h1>
          <div class="mt-2 flex flex-wrap items-center gap-3">
            <StatusTag :status="space.status" type="space" />
            <span class="text-slate-500 dark:text-slate-400">
              <el-icon class="mr-1"><Location /></el-icon>
              {{ space.areaName }} · {{ space.floor }}
            </span>
            <span class="text-slate-500 dark:text-slate-400">
              <el-icon class="mr-1"><User /></el-icon>
              {{ space.capacity }} 人
            </span>
          </div>

          <!-- Equipment -->
          <div v-if="hasEquipment" class="mt-6">
            <h3 class="mb-3 font-semibold text-slate-900 dark:text-slate-100">设备配置</h3>
            <div class="flex flex-wrap gap-2">
              <el-tag v-if="space.equipmentConfig.power" type="info">电源插座</el-tag>
              <el-tag v-if="space.equipmentConfig.projector" type="warning">投影仪</el-tag>
              <el-tag v-if="space.equipmentConfig.whiteboard" type="success">白板</el-tag>
              <el-tag v-if="space.equipmentConfig.network" type="primary">{{ space.equipmentConfig.network === 'wifi6' ? 'WiFi6' : '网络' }}</el-tag>
              <el-tag v-if="space.equipmentConfig.computer">{{ space.equipmentConfig.computer }}</el-tag>
              <el-tag v-if="space.equipmentConfig.videoConf" type="danger">视频会议</el-tag>
              <el-tag v-if="space.equipmentConfig.sofa">沙发</el-tag>
              <el-tag v-if="space.equipmentConfig.audio">音频设备</el-tag>
            </div>
          </div>

          <!-- Description -->
          <div v-if="space.description" class="mt-6">
            <h3 class="mb-2 font-semibold text-slate-900 dark:text-slate-100">空间介绍</h3>
            <p class="text-sm leading-relaxed text-slate-500 dark:text-slate-400">{{ space.description }}</p>
          </div>

          <!-- Rules -->
          <div v-if="space.useRules" class="mt-4">
            <h3 class="mb-2 font-semibold text-slate-900 dark:text-slate-100">使用规则</h3>
            <p class="text-sm leading-relaxed text-slate-500 dark:text-slate-400">{{ space.useRules }}</p>
          </div>
        </div>

        <!-- Right: Booking panel -->
        <div class="lg:col-span-1">
          <div class="sticky top-24 rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-white/5 dark:bg-slate-900">
            <h2 class="mb-4 text-lg font-bold text-slate-900 dark:text-slate-100">预约空间</h2>

            <el-alert v-if="space.status !== 1" :title="space.status === 0 ? '该空间维修中，暂不可预约' : '该空间已停用'"
              type="error" show-icon :closable="false" class="mb-4" />

            <template v-if="space.status === 1">
              <!-- Date picker -->
              <div class="mb-4">
                <label class="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-300">预约日期</label>
                <el-date-picker
                  v-model="bookingDate"
                  type="date"
                  placeholder="选择日期"
                  class="w-full"
                  :disabled-date="disabledDate"
                  @change="onDateChange"
                />
              </div>

              <!-- Time slots -->
              <div class="mb-4">
                <label class="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300">
                  时段选择（{{ timeSlots.filter(s => s.selected).length }}个时段 · {{ selectedDuration }}分钟）
                </label>
                <div class="grid grid-cols-4 gap-1.5">
                  <button
                    v-for="slot in timeSlots"
                    :key="slot.hour * 60 + slot.minute"
                    :disabled="slot.booked"
                    @click="toggleSlot(slot)"
                    :class="[
                      'rounded-md px-2 py-2 text-xs font-medium transition-all',
                      slot.selected
                        ? 'bg-primary text-white shadow-sm'
                        : slot.booked
                          ? 'cursor-not-allowed bg-slate-100 text-slate-300 dark:bg-slate-800 dark:text-slate-600'
                          : 'bg-slate-50 text-slate-600 hover:bg-primary/10 hover:text-primary dark:bg-slate-800 dark:text-slate-400',
                    ]"
                  >
                    {{ formatTime(slot.hour, slot.minute) }}
                  </button>
                </div>
              </div>

              <!-- Info -->
              <div class="mb-4 rounded-lg bg-slate-50 p-3 text-xs text-slate-500 dark:bg-slate-800 dark:text-slate-400">
                <p>最小预约时长：30分钟</p>
                <p>最大预约时长：{{ space.spaceType === 'ROOM' ? '10小时' : '4小时' }}</p>
                <p>预约日期：{{ bookingDate || '未选择' }}</p>
              </div>

              <!-- Purpose -->
              <div class="mb-4">
                <el-input v-model="purpose" type="textarea" :rows="2" placeholder="预约用途（选填）" />
              </div>

              <el-button
                type="primary"
                class="w-full"
                size="large"
                :disabled="!canBook"
                :loading="booking"
                @click="handleBook"
              >
                确认预约
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- Success dialog -->
      <el-dialog v-model="successVisible" title="预约成功" width="400px" center>
        <div class="text-center">
          <el-icon class="text-5xl text-emerald-500"><CircleCheckFilled /></el-icon>
          <p class="mt-4 text-lg font-bold text-slate-900 dark:text-slate-100">预约成功！</p>
          <div class="mt-2 rounded-lg bg-slate-50 p-4 dark:bg-slate-800">
            <p class="text-sm text-slate-500">预约码</p>
            <p class="mt-1 text-2xl font-mono font-bold text-primary">{{ reservationCode }}</p>
          </div>
          <p class="mt-2 text-xs text-slate-400">请凭预约码到图书馆签到</p>
        </div>
        <template #footer>
          <el-button @click="successVisible = false; router.push('/my-reservations')">查看我的预约</el-button>
          <el-button type="primary" @click="successVisible = false">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SpaceVO } from '@/api/space/type'
import { getSpaceById } from '@/api/space/api'
import { createReservation } from '@/api/reservation/api'
import { meMsgSuccess, meMsgError } from '@/utils/modal'

const router = useRouter()
const route = useRoute()
const spaceId = Number(route.params.id)

const loading = ref(true)
const space = ref<SpaceVO | null>(null)
const bookingDate = ref('')
const purpose = ref('')
const booking = ref(false)
const successVisible = ref(false)
const reservationCode = ref('')

interface TimeSlot {
  hour: number
  minute: number
  selected: boolean
  booked: boolean
}

const timeSlots = ref<TimeSlot[]>([])
const maxDuration = computed(() => (space.value?.spaceType === 'ROOM' ? 20 : 8))

const selectedDuration = computed(() => {
  const selected = timeSlots.value.filter((s) => s.selected)
  if (selected.length === 0) return 0
  return selected.length * 30
})

const canBook = computed(() => {
  const selected = timeSlots.value.filter((s) => s.selected)
  if (selected.length === 0) return false
  if (!bookingDate.value) return false
  // Check consecutive
  const indices = timeSlots.value.map((s, i) => (s.selected ? i : -1)).filter((i) => i >= 0)
  for (let k = 1; k < indices.length; k++) {
    if (indices[k] !== indices[k - 1] + 1) return false
  }
  return selected.length <= maxDuration.value
})

function disabledDate(date: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const max = new Date(today)
  max.setDate(max.getDate() + 7)
  return date < today || date > max
}

function formatTime(hour: number, minute: number) {
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function generateSlots() {
  const slots: TimeSlot[] = []
  for (let h = 8; h < 22; h++) {
    for (let m = 0; m < 60; m += 30) {
      slots.push({
        hour: h,
        minute: m,
        selected: false,
        booked: Math.random() < 0.15, // 15% of slots are booked
      })
    }
  }
  timeSlots.value = slots
}

function toggleSlot(slot: TimeSlot) {
  if (slot.booked) return
  slot.selected = !slot.selected
}

function onDateChange() {
  generateSlots()
}

async function fetchSpace() {
  loading.value = true
  try {
    const res = await getSpaceById(spaceId)
    if (res.code === 0) {
      space.value = res.data
    }
  } finally {
    generateSlots()
    loading.value = false
  }
}

async function handleBook() {
  if (!canBook.value || !space.value) return
  const selected = timeSlots.value.filter((s) => s.selected)
  const first = selected[0]
  const last = selected[selected.length - 1]

  const startTime = `${bookingDate.value} ${formatTime(first.hour, first.minute)}:00`
  const endHour = last.hour + (last.minute === 0 ? 0 : 0) + (last.minute + 30 >= 60 ? 1 : 0)
  const endMinute = (last.minute + 30) % 60
  const endTime = `${bookingDate.value} ${formatTime(endHour, endMinute)}:00`

  booking.value = true
  try {
    const res = await createReservation({
      spaceId: space.value.id,
      startTime,
      endTime,
      purpose: purpose.value || undefined,
    })
    if (res.code === 0) {
      reservationCode.value = res.data.reservationCode
      successVisible.value = true
      meMsgSuccess({ message: '预约成功' })
    } else {
      meMsgError({ message: res.msg || '预约失败' })
    }
  } catch {
    meMsgError({ message: '预约失败' })
  } finally {
    booking.value = false
  }
}

onMounted(() => fetchSpace())
</script>
