<template>
  <div>
    <PageHeader title="预约管理" description="监控和管理所有预约记录，支持强制操作和审批" />

    <!-- Search form -->
    <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <el-input v-model="query.keyword" placeholder="预约码/用户/空间名" class="w-48" clearable @keyup.enter="doSearch" />
      <el-select v-model="query.status" placeholder="预约状态" class="w-32" clearable @change="doSearch">
        <el-option v-for="o in StatusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.approvalStatus" placeholder="审批状态" class="w-32" clearable @change="doSearch">
        <el-option v-for="o in ApprovalStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD HH:mm:ss" class="w-60" @change="handleDateRangeChange" />
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- Table -->
    <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="reservationCode" label="预约码" width="170" />
        <el-table-column prop="spaceName" label="空间名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column label="时间段" min-width="290">
          <template #default="{ row }">
            <span class="text-sm">{{ row.startTime }}</span>
            <br />
            <span class="text-sm text-slate-400">至 {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预约状态" width="90">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="reservation" />
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="90">
          <template #default="{ row }">
            <StatusTag :status="row.approvalStatus" type="approval" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              v-if="row.status === 0 || row.status === 1"
              title="确认强制取消该预约？"
              confirm-button-text="确定"
              @confirm="handleForceCancel(row.id)"
            >
              <template #reference>
                <el-button type="danger" link size="small">强制取消</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="row.status === 1"
              title="确认强制签退该用户？"
              confirm-button-text="确定"
              @confirm="handleForceCheckout(row.id)"
            >
              <template #reference>
                <el-button type="warning" link size="small">强制签退</el-button>
              </template>
            </el-popconfirm>
            <el-button
              v-if="row.approvalStatus === 2"
              type="primary" link size="small"
              @click="openApprove(row)">
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="暂无预约记录" />
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="mt-4 flex justify-center">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        background
        layout="total, prev, pager, next, sizes"
        :page-sizes="[10, 20, 50]"
        @current-change="fetchList"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- Approve Dialog -->
    <el-dialog v-model="approveVisible" title="审批预约" width="500px">
      <div class="mb-4">
        <p class="mb-2 text-sm"><span class="text-slate-500">预约码：</span>{{ approveTarget?.reservationCode }}</p>
        <p class="mb-2 text-sm"><span class="text-slate-500">申请人：</span>{{ approveTarget?.userName }}</p>
        <p class="mb-2 text-sm"><span class="text-slate-500">空间：</span>{{ approveTarget?.spaceName }}</p>
        <p class="mb-2 text-sm"><span class="text-slate-500">时间：</span>{{ approveTarget?.startTime }} ~ {{ approveTarget?.endTime }}</p>
        <p v-if="approveTarget?.purpose" class="mb-2 text-sm"><span class="text-slate-500">用途：</span>{{ approveTarget?.purpose }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审批备注">
          <el-input v-model="approveRemark" type="textarea" :rows="3" placeholder="请输入审批备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="danger" :loading="approveLoading" @click="handleApprove(false)">驳回</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleApprove(true)">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { ReservationVO } from '@/api/reservation/type'
import { getAllReservations, forceCancelReservation, forceCheckout, approveReservation } from '@/api/reservation/api'
import { meMsgSuccess } from '@/utils/modal'
import { StatusOptions, ApprovalStatusOptions, defaultQuery } from './data'

const loading = ref(false)
const list = ref<ReservationVO[]>([])
const total = ref(0)
const query = reactive({ ...defaultQuery })
const dateRange = ref<[string, string] | null>(null)

// Approve
const approveVisible = ref(false)
const approveTarget = ref<ReservationVO | null>(null)
const approveRemark = ref('')
const approveLoading = ref(false)

function openApprove(row: ReservationVO) {
  approveTarget.value = row
  approveRemark.value = ''
  approveVisible.value = true
}

async function handleApprove(approved: boolean) {
  if (!approveTarget.value) return
  approveLoading.value = true
  try {
    await approveReservation(approveTarget.value.id, approved, approveRemark.value.trim() || undefined)
    meMsgSuccess({ message: approved ? '审批通过' : '已驳回' })
    approveVisible.value = false
    fetchList()
  } catch { /* handled */ } finally {
    approveLoading.value = false
  }
}

async function handleForceCancel(id: number) {
  try {
    await forceCancelReservation(id)
    meMsgSuccess({ message: '已强制取消' })
    fetchList()
  } catch { /* handled */ }
}

async function handleForceCheckout(id: number) {
  try {
    await forceCheckout(id)
    meMsgSuccess({ message: '已强制签退' })
    fetchList()
  } catch { /* handled */ }
}

function handleDateRangeChange(val: [string, string] | null) {
  if (val) {
    query.startTime = val[0]
    query.endTime = val[1]
  } else {
    query.startTime = ''
    query.endTime = ''
  }
  doSearch()
}

function doSearch() {
  query.page = 1
  fetchList()
}

function resetSearch() {
  query.keyword = ''
  query.status = undefined
  query.approvalStatus = undefined
  query.startTime = ''
  query.endTime = ''
  dateRange.value = null
  doSearch()
}

function handleSizeChange() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const q: any = { ...query }
    if (!q.startTime) delete q.startTime
    if (!q.endTime) delete q.endTime
    const res = await getAllReservations(q)
    if (res.code === 0) {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchList())
</script>
