<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <PageHeader title="信用积分" description="查看您的信用积分、积分规则和积分变动记录" />

    <div class="space-y-6">
      <!-- Score gauge section -->
      <div class="rounded-2xl border border-slate-200 bg-white p-8 dark:border-white/5 dark:bg-slate-900">
        <div class="flex flex-col items-center gap-8 md:flex-row md:justify-center">
          <!-- Circular gauge -->
          <div class="relative flex items-center justify-center">
            <el-progress
              type="dashboard"
              :percentage="scorePercent"
              :color="scoreColor"
              :stroke-width="12"
              :width="180"
            >
              <template #default>
                <div class="text-center">
                  <div class="text-4xl font-extrabold" :style="{ color: scoreColor }">{{ currentScore }}</div>
                  <div class="mt-1 text-xs text-slate-400 dark:text-slate-500">/ 100</div>
                </div>
              </template>
            </el-progress>
          </div>

          <!-- Score details -->
          <div class="flex-1 space-y-4 text-center md:text-left">
            <div>
              <div class="text-sm text-slate-500 dark:text-slate-400">当前信用等级</div>
              <div class="mt-1 flex items-center justify-center gap-2 md:justify-start">
                <span class="text-xl font-bold" :style="{ color: scoreColor }">{{ scoreLevel }}</span>
                <span
                  class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                  :class="scoreBadgeClass"
                >
                  {{ scoreLevel }}
                </span>
              </div>
            </div>
            <div class="text-sm text-slate-500 dark:text-slate-400">
              {{ scoreDescription }}
            </div>
            <div class="flex flex-wrap justify-center gap-4 md:justify-start">
              <div class="text-center">
                <div class="text-lg font-bold text-emerald-500">+{{ totalReward }}</div>
                <div class="text-xs text-slate-400 dark:text-slate-500">累计加分</div>
              </div>
              <div class="text-center">
                <div class="text-lg font-bold text-red-500">{{ totalPunish }}</div>
                <div class="text-xs text-slate-400 dark:text-slate-500">累计扣分</div>
              </div>
              <div class="text-center">
                <div class="text-lg font-bold text-slate-700 dark:text-slate-300">{{ creditLogs.length }}</div>
                <div class="text-xs text-slate-400 dark:text-slate-500">变动次数</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Credit rules -->
      <div class="rounded-2xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
        <div class="border-b border-slate-100 px-6 py-4 dark:border-white/5">
          <h3 class="text-base font-bold text-slate-900 dark:text-slate-100">积分规则</h3>
          <p class="mt-0.5 text-xs text-slate-400 dark:text-slate-500">了解信用积分的加减规则</p>
        </div>
        <div class="p-4">
          <el-collapse v-model="activeRules" accordion>
            <el-collapse-item
              v-for="rule in rules"
              :key="rule.id"
              :name="rule.id"
            >
              <template #title>
                <div class="flex w-full items-center gap-3 pr-4">
                  <span
                    class="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold"
                    :class="rule.ruleType === 'REWARD' ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400'"
                  >
                    {{ rule.ruleType === 'REWARD' ? '+' : '-' }}
                  </span>
                  <span class="text-sm font-medium text-slate-900 dark:text-slate-100">{{ rule.ruleName }}</span>
                  <span
                    class="ml-auto shrink-0 text-sm font-bold"
                    :class="rule.ruleType === 'REWARD' ? 'text-emerald-500' : 'text-red-500'"
                  >
                    {{ rule.ruleType === 'REWARD' ? '+' : '' }}{{ rule.changeValue }}
                  </span>
                  <el-tag size="small" :type="rule.ruleType === 'REWARD' ? 'success' : 'danger'" effect="plain">
                    {{ rule.ruleType === 'REWARD' ? '加分' : '扣分' }}
                  </el-tag>
                </div>
              </template>
              <div class="pl-9 pr-4 text-sm text-slate-600 dark:text-slate-400">
                {{ rule.description }}
              </div>
            </el-collapse-item>
          </el-collapse>
          <EmptyState v-if="rules.length === 0" description="暂无积分规则" :image-size="60" />
        </div>
      </div>

      <!-- Credit history -->
      <div class="rounded-2xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-6 py-4 dark:border-white/5">
          <div>
            <h3 class="text-base font-bold text-slate-900 dark:text-slate-100">积分变动记录</h3>
            <p class="mt-0.5 text-xs text-slate-400 dark:text-slate-500">详细记录每次积分的增减</p>
          </div>
          <el-select v-model="logFilter" placeholder="变动类型" clearable size="small" class="w-32" @change="fetchLogs">
            <el-option label="全部记录" value="" />
            <el-option label="签到履约" value="CHECKIN" />
            <el-option label="预约违约" value="DEFAULTED" />
            <el-option label="手动调整" value="MANUAL" />
          </el-select>
        </div>

        <div v-loading="logLoading">
          <EmptyState v-if="creditLogs.length === 0" description="暂无积分变动记录" :image-size="80" />

          <!-- Desktop table -->
          <div v-else class="hidden md:block">
            <el-table :data="creditLogs" stripe class="dark:!bg-transparent">
              <el-table-column prop="createTime" label="时间" width="170">
                <template #default="{ row }">
                  <span class="text-xs text-slate-600 dark:text-slate-400">{{ row.createTime }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="变动原因" min-width="240">
                <template #default="{ row }">
                  <span class="text-sm text-slate-700 dark:text-slate-300">{{ row.remark }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="changeScore" label="变动分值" width="100" align="center">
                <template #default="{ row }">
                  <span
                    class="text-sm font-bold"
                    :class="row.changeScore > 0 ? 'text-emerald-500' : 'text-red-500'"
                  >
                    {{ row.changeScore > 0 ? '+' : '' }}{{ row.changeScore }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="afterScore" label="变动后积分" width="110" align="center">
                <template #default="{ row }">
                  <span class="text-sm font-medium text-slate-700 dark:text-slate-300">{{ row.afterScore }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Mobile card list -->
          <div v-else class="divide-y divide-slate-100 dark:divide-white/5">
            <div
              v-for="log in creditLogs"
              :key="log.id"
              class="flex items-start gap-3 px-4 py-3"
            >
              <div
                class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold"
                :class="log.changeScore > 0 ? 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400' : 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400'"
              >
                {{ log.changeScore > 0 ? '+' : '' }}{{ log.changeScore }}
              </div>
              <div class="min-w-0 flex-1">
                <p class="text-sm text-slate-700 dark:text-slate-300">{{ log.remark }}</p>
                <div class="mt-1 flex items-center gap-2 text-xs text-slate-400 dark:text-slate-500">
                  <span>{{ log.createTime }}</span>
                  <span>变动后：{{ log.afterScore }}分</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="logTotal > logQuery.pageSize" class="border-t border-slate-100 px-6 py-4 dark:border-white/5">
          <el-pagination
            v-model:current-page="logQuery.page"
            :page-size="logQuery.pageSize"
            :total="logTotal"
            background
            layout="prev, pager, next"
            @current-change="fetchLogs"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { getCreditRules, getMyCreditLogs } from '@/api/credit/api'
import type { CreditRuleVO, CreditLogVO } from '@/api/credit/type'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

// --- Score ---
const currentScore = computed(() => userStore.userInfo?.creditScore ?? 85)
const scorePercent = computed(() => Math.min(currentScore.value, 100))

const scoreColor = computed(() => {
  if (currentScore.value >= 80) return '#22c55e'
  if (currentScore.value >= 60) return '#f59e0b'
  return '#ef4444'
})

const scoreLevel = computed(() => {
  if (currentScore.value >= 90) return '优秀'
  if (currentScore.value >= 80) return '良好'
  if (currentScore.value >= 60) return '一般'
  return '较低'
})

const scoreBadgeClass = computed(() => {
  if (currentScore.value >= 80) return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'
  if (currentScore.value >= 60) return 'bg-amber-50 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400'
  return 'bg-red-50 text-red-700 dark:bg-red-500/10 dark:text-red-400'
})

const scoreDescription = computed(() => {
  if (currentScore.value >= 90) return '您的信用状况优秀，可享受所有预约权限和优先预约权。'
  if (currentScore.value >= 80) return '您的信用状况良好，可正常预约和使用图书馆空间。'
  if (currentScore.value >= 60) return '您的信用分偏低，可能无法预约热门时段座位，请注意维护信用记录。'
  return '您的信用分较低，已限制部分预约功能，请尽快改善信用记录。'
})

// --- Rules ---
const rules = ref<CreditRuleVO[]>([])
const activeRules = ref<number | string>('')

async function fetchRules() {
  try {
    const res = await getCreditRules()
    if (res.code === 0) {
      rules.value = res.data
    }
  } catch { /* ignore */ }
}

// --- Logs ---
const logLoading = ref(false)
const creditLogs = ref<CreditLogVO[]>([])
const logTotal = ref(0)
const logFilter = ref('')

const logQuery = reactive({
  page: 1,
  pageSize: 10,
})

const totalReward = ref(0)
const totalPunish = ref(0)

async function fetchLogs() {
  logLoading.value = true
  try {
    const res = await getMyCreditLogs({
      page: logQuery.page,
      pageSize: logQuery.pageSize,
      referenceType: logFilter.value || undefined,
    })
    if (res.code === 0) {
      creditLogs.value = res.data.list
      logTotal.value = res.data.total
    }
  } catch { /* ignore */ } finally {
    logLoading.value = false
  }
}

async function fetchAllLogsForStats() {
  try {
    const res = await getMyCreditLogs({ page: 1, pageSize: 999 })
    if (res.code === 0) {
      const allLogs = res.data.list
      totalReward.value = allLogs.filter((l) => l.changeScore > 0).reduce((sum, l) => sum + l.changeScore, 0)
      totalPunish.value = allLogs.filter((l) => l.changeScore < 0).reduce((sum, l) => sum + Math.abs(l.changeScore), 0)
    }
  } catch { /* ignore */ }
}

watch(logFilter, () => {
  logQuery.page = 1
  fetchLogs()
})

onMounted(() => {
  fetchRules()
  fetchLogs()
  fetchAllLogsForStats()
})
</script>
