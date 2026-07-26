<template>
  <div>
    <PageHeader title="违规管理" description="管理黑名单记录、查看信用流水并手动调整积分">
      <template #actions>
        <el-button type="primary" @click="openBlacklistModal">添加黑名单</el-button>
        <el-button type="warning" @click="openAdjustModal">手动调整积分</el-button>
      </template>
    </PageHeader>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab 1: 黑名单记录 -->
      <el-tab-pane label="黑名单记录" name="blacklist">
        <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
          <el-input v-model="blacklistQuery.keyword" placeholder="搜索用户姓名" class="w-48" clearable @keyup.enter="fetchBlacklist" />
          <el-select v-model="blacklistQuery.violationType" placeholder="违规类型" class="w-36" clearable @change="fetchBlacklist">
            <el-option v-for="o in ViolationTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-select v-model="blacklistQuery.status" placeholder="状态" class="w-28" clearable @change="fetchBlacklist">
            <el-option v-for="o in BlacklistStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-button type="primary" @click="fetchBlacklist">搜索</el-button>
          <el-button @click="resetBlacklistSearch">重置</el-button>
        </div>

        <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
          <el-table :data="blacklistList" v-loading="blacklistLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column label="违规类型" width="100">
              <template #default="{ row }">{{ ViolationTypeMap[row.violationType] || row.violationType }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="违规原因" min-width="180" show-overflow-tooltip />
            <el-table-column prop="startTime" label="封禁开始" width="170" />
            <el-table-column prop="endTime" label="封禁结束" width="170" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <StatusTag :status="row.status" type="blacklist" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-popconfirm
                  v-if="row.status === 1"
                  title="确认解除该黑名单？"
                  confirm-button-text="确定"
                  @confirm="handleRemoveBlacklist(row.id)"
                >
                  <template #reference>
                    <el-button type="success" link size="small">解除</el-button>
                  </template>
                </el-popconfirm>
                <span v-else class="text-xs text-slate-400">-</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="blacklistList.length === 0 && !blacklistLoading">
            <EmptyState description="暂无黑名单记录" />
          </div>
        </div>

        <div v-if="blacklistTotal > 0" class="mt-4 flex justify-center">
          <el-pagination
            v-model:current-page="blacklistQuery.page"
            :page-size="blacklistQuery.pageSize"
            :total="blacklistTotal"
            background
            layout="total, prev, pager, next, sizes"
            :page-sizes="[10, 20, 50]"
            @current-change="fetchBlacklist"
            @size-change="() => { blacklistQuery.page = 1; fetchBlacklist() }"
          />
        </div>
      </el-tab-pane>

      <!-- Tab 2: 信用流水 -->
      <el-tab-pane label="信用流水" name="creditLogs">
        <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
          <el-select v-model="creditLogQuery.userId" placeholder="选择用户" class="w-48" clearable filterable @change="fetchCreditLogs">
            <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
          </el-select>
          <el-select v-model="creditLogQuery.changeType" placeholder="变动类型" class="w-28" clearable @change="fetchCreditLogs">
            <el-option v-for="o in CreditChangeTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-button type="primary" @click="fetchCreditLogs">搜索</el-button>
          <el-button @click="resetCreditLogSearch">重置</el-button>
        </div>

        <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
          <el-table :data="creditLogList" v-loading="creditLogLoading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column label="变动" width="90">
              <template #default="{ row }">
                <span :class="row.changeScore > 0 ? 'text-emerald-500' : 'text-red-500'">
                  {{ row.changeScore > 0 ? '+' : '' }}{{ row.changeScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="afterScore" label="变动后" width="80" />
            <el-table-column prop="remark" label="原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="变动时间" width="170" />
          </el-table>
          <div v-if="creditLogList.length === 0 && !creditLogLoading">
            <EmptyState description="暂无信用流水记录" />
          </div>
        </div>

        <div v-if="creditLogTotal > 0" class="mt-4 flex justify-center">
          <el-pagination
            v-model:current-page="creditLogQuery.page"
            :page-size="creditLogQuery.pageSize"
            :total="creditLogTotal"
            background
            layout="total, prev, pager, next, sizes"
            :page-sizes="[10, 20, 50]"
            @current-change="fetchCreditLogs"
            @size-change="() => { creditLogQuery.page = 1; fetchCreditLogs() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Modals -->
    <ViolationModal
      v-model:blacklistModelValue="blacklistModalVisible"
      v-model:adjustModelValue="adjustModalVisible"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import type { BlacklistVO } from '@/api/blacklist/type'
import type { CreditLogVO } from '@/api/credit/type'
import { getBlacklist, removeBlacklist } from '@/api/blacklist/api'
import { getUserCreditLogs } from '@/api/credit/api'
import { getUserList } from '@/api/user/api'
import { meMsgSuccess } from '@/utils/modal'
import { ViolationTypeOptions, BlacklistStatusOptions, ViolationTypeMap, CreditChangeTypeOptions, defaultBlacklistQuery, defaultCreditLogQuery } from './data'
import ViolationModal from './modal.vue'

const activeTab = ref('blacklist')

// Blacklist
const blacklistLoading = ref(false)
const blacklistList = ref<BlacklistVO[]>([])
const blacklistTotal = ref(0)
const blacklistQuery = reactive({ ...defaultBlacklistQuery })

// Credit logs
const creditLogLoading = ref(false)
const creditLogList = ref<CreditLogVO[]>([])
const creditLogTotal = ref(0)
const creditLogQuery = reactive({ ...defaultCreditLogQuery })

// Modals
const blacklistModalVisible = ref(false)
const adjustModalVisible = ref(false)

// User options for credit log filter
const userOptions = ref<{ label: string; value: number }[]>([])

async function loadUsers() {
  try {
    const res = await getUserList({ page: 1, pageSize: 200 })
    if (res.code === 0) {
      userOptions.value = res.data.list.map((u: any) => ({
        label: `${u.realName} (${u.username})`,
        value: u.id,
      }))
    }
  } catch { /* ignore */ }
}

loadUsers()

function openBlacklistModal() {
  blacklistModalVisible.value = true
}

function openAdjustModal() {
  adjustModalVisible.value = true
}

function handleModalSuccess() {
  if (activeTab.value === 'blacklist') fetchBlacklist()
  else fetchCreditLogs()
}

async function handleRemoveBlacklist(id: number) {
  try {
    await removeBlacklist(id)
    meMsgSuccess({ message: '已解除' })
    fetchBlacklist()
  } catch { /* handled */ }
}

function resetBlacklistSearch() {
  blacklistQuery.keyword = ''
  blacklistQuery.violationType = ''
  blacklistQuery.status = undefined
  fetchBlacklist()
}

function resetCreditLogSearch() {
  creditLogQuery.userId = undefined
  creditLogQuery.changeType = ''
  fetchCreditLogs()
}

function handleTabChange() {
  if (activeTab.value === 'blacklist') fetchBlacklist()
  else fetchCreditLogs()
}

async function fetchBlacklist() {
  blacklistLoading.value = true
  try {
    const q: any = { ...blacklistQuery }
    if (q.status === '') q.status = undefined
    const res = await getBlacklist(q)
    if (res.code === 0) {
      blacklistList.value = res.data.list
      blacklistTotal.value = res.data.total
    }
  } finally {
    blacklistLoading.value = false
  }
}

async function fetchCreditLogs() {
  creditLogLoading.value = true
  try {
    const userId = creditLogQuery.userId || 2 // default to user 2 for demo
    const q: any = { page: creditLogQuery.page, pageSize: creditLogQuery.pageSize }
    if (creditLogQuery.changeType) q.changeType = creditLogQuery.changeType
    const res = await getUserCreditLogs(userId, q)
    if (res.code === 0) {
      creditLogList.value = res.data.list
      creditLogTotal.value = res.data.total
    }
  } finally {
    creditLogLoading.value = false
  }
}

onMounted(() => fetchBlacklist())
</script>
