<template>
  <div>
    <PageHeader title="系统设置" description="配置预约规则、信用规则、通知设置和系统参数" />

    <div class="space-y-6">
      <!-- Section 1: 预约规则 -->
      <el-card shadow="never">
        <template #header>
          <div class="flex items-center gap-2">
            <el-icon color="#3b82f6"><Calendar /></el-icon>
            <span class="font-semibold text-slate-900 dark:text-slate-100">预约规则</span>
          </div>
        </template>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <el-form-item label="可提前预约天数">
            <el-input-number v-model="reservationForm.advanceDays" :min="1" :max="30" />
            <p class="mt-1 text-xs text-slate-400">用户最多可提前多少天预约</p>
          </el-form-item>
          <el-form-item label="座位单次最长(小时)">
            <el-input-number v-model="reservationForm.maxSeatHours" :min="1" :max="12" :precision="1" :step="0.5" />
            <p class="mt-1 text-xs text-slate-400">单人座位预约最大时长</p>
          </el-form-item>
          <el-form-item label="研讨室单次最长(小时)">
            <el-input-number v-model="reservationForm.maxRoomHours" :min="1" :max="12" :precision="1" :step="0.5" />
            <p class="mt-1 text-xs text-slate-400">研讨室预约最大时长</p>
          </el-form-item>
          <el-form-item label="每日最多预约次数">
            <el-input-number v-model="reservationForm.maxDaily" :min="1" :max="10" />
            <p class="mt-1 text-xs text-slate-400">每位用户每天最多预约次数</p>
          </el-form-item>
          <el-form-item label="研讨室最小参与人数">
            <el-input-number v-model="reservationForm.minRoomParticipants" :min="2" :max="20" />
            <p class="mt-1 text-xs text-slate-400">预约研讨室的最低参与人数</p>
          </el-form-item>
          <el-form-item label="研讨室最大参与人数">
            <el-input-number v-model="reservationForm.maxRoomParticipants" :min="2" :max="50" />
            <p class="mt-1 text-xs text-slate-400">研讨室最大容纳人数</p>
          </el-form-item>
        </div>
        <div class="mt-4 flex justify-end">
          <el-button type="primary" :loading="savingReservation" @click="saveReservation">保存设置</el-button>
        </div>
      </el-card>

      <!-- Section 2: 信用规则 -->
      <el-card shadow="never">
        <template #header>
          <div class="flex items-center gap-2">
            <el-icon color="#f59e0b"><Tickets /></el-icon>
            <span class="font-semibold text-slate-900 dark:text-slate-100">信用规则</span>
          </div>
        </template>
        <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">
          信用规则配置包括积分增减规则、黑名单阈值等，可在独立页面中详细配置。
        </p>
        <el-button @click="router.push('/admin/credit-rules')">
          前往信用规则管理
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </el-card>

      <!-- Section 3: 通知设置 -->
      <el-card shadow="never">
        <template #header>
          <div class="flex items-center gap-2">
            <el-icon color="#8b5cf6"><Bell /></el-icon>
            <span class="font-semibold text-slate-900 dark:text-slate-100">通知设置</span>
          </div>
        </template>
        <div class="space-y-4">
          <div v-for="item in notificationItems" :key="item.key" class="flex items-center justify-between rounded-lg bg-slate-50 p-4 dark:bg-slate-800">
            <div>
              <p class="font-medium text-slate-900 dark:text-slate-100">{{ item.label }}</p>
              <p class="text-xs text-slate-400 dark:text-slate-500">{{ item.description }}</p>
            </div>
            <el-switch v-model="notificationSettings[item.key]" inline-prompt active-text="开启" inactive-text="关闭" />
          </div>
        </div>
        <div class="mt-4 flex justify-end">
          <el-button type="primary" :loading="savingNotification" @click="saveNotifications">保存设置</el-button>
        </div>
      </el-card>

      <!-- Section 4: 系统参数 -->
      <el-card shadow="never">
        <template #header>
          <div class="flex items-center gap-2">
            <el-icon color="#ef4444"><Setting /></el-icon>
            <span class="font-semibold text-slate-900 dark:text-slate-100">系统参数</span>
          </div>
        </template>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <el-form-item label="最大登录尝试次数">
            <el-input-number v-model="systemForm.maxLoginAttempts" :min="3" :max="20" />
            <p class="mt-1 text-xs text-slate-400">超过此次数账号将被锁定</p>
          </el-form-item>
          <el-form-item label="锁定时长(分钟)">
            <el-input-number v-model="systemForm.lockDuration" :min="5" :max="1440" :step="5" />
            <p class="mt-1 text-xs text-slate-400">账号锁定后的自动解锁时长</p>
          </el-form-item>
          <el-form-item label="会话超时(分钟)">
            <el-input-number v-model="systemForm.sessionTimeout" :min="5" :max="1440" :step="5" />
            <p class="mt-1 text-xs text-slate-400">长时间无操作后自动退出登录</p>
          </el-form-item>
        </div>
        <div class="mt-4 flex justify-end">
          <el-button type="primary" :loading="savingSystem" @click="saveSystem">保存设置</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { meMsgSuccess } from '@/utils/modal'

const router = useRouter()

// Reservation settings
const savingReservation = ref(false)
const reservationForm = reactive({
  advanceDays: 7,
  maxSeatHours: 4,
  maxRoomHours: 4,
  maxDaily: 2,
  minRoomParticipants: 2,
  maxRoomParticipants: 12,
})

// Notification settings
const savingNotification = ref(false)
const notificationSettings = reactive<Record<string, boolean>>({
  reservationSuccess: true,
  reservationReminder: true,
  violationReminder: true,
  creditChange: true,
  systemNotice: false,
})

const notificationItems = [
  { key: 'reservationSuccess', label: '预约成功通知', description: '用户预约成功后推送通知' },
  { key: 'reservationReminder', label: '预约提醒', description: '预约开始前发送提醒通知' },
  { key: 'violationReminder', label: '违约提醒', description: '发生违约行为时推送警告' },
  { key: 'creditChange', label: '积分变动通知', description: '信用积分增减时推送通知' },
  { key: 'systemNotice', label: '系统公告', description: '系统维护或重要公告推送' },
]

// System settings
const savingSystem = ref(false)
const systemForm = reactive({
  maxLoginAttempts: 5,
  lockDuration: 30,
  sessionTimeout: 120,
})

async function saveReservation() {
  savingReservation.value = true
  try {
    // Simulate API call
    await new Promise((r) => setTimeout(r, 500))
    meMsgSuccess({ message: '预约规则已保存' })
  } finally {
    savingReservation.value = false
  }
}

async function saveNotifications() {
  savingNotification.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    meMsgSuccess({ message: '通知设置已保存' })
  } finally {
    savingNotification.value = false
  }
}

async function saveSystem() {
  savingSystem.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    meMsgSuccess({ message: '系统参数已保存' })
  } finally {
    savingSystem.value = false
  }
}
</script>
