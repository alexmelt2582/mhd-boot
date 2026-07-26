<template>
  <div>
    <PageHeader title="定时任务" description="管理系统的定时任务调度，支持执行、暂停、恢复和修改 Cron 表达式" />

    <!-- Search -->
    <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">任务名称</label>
        <el-input v-model="query.jobName" placeholder="任务名称" class="w-44" clearable @keyup.enter="doSearch" />
      </div>
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">任务组</label>
        <el-input v-model="query.jobGroup" placeholder="任务组" class="w-36" clearable @keyup.enter="doSearch" />
      </div>
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">状态</label>
        <el-select v-model="query.status" placeholder="全部" class="w-28" clearable @change="doSearch">
          <el-option label="运行中" :value="1" />
          <el-option label="已暂停" :value="0" />
        </el-select>
      </div>
      <div class="self-end">
        <el-button type="primary" @click="doSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table :data="list" v-loading="loading" stripe border>
      <el-table-column prop="jobName" label="任务名称" width="180">
        <template #default="{ row }">
          <span class="font-medium text-slate-900 dark:text-slate-100">{{ row.jobName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="jobGroup" label="任务组" width="120" />
      <el-table-column prop="cronExpression" label="Cron 表达式" width="180">
        <template #default="{ row }">
          <code class="rounded bg-slate-100 px-1.5 py-0.5 text-xs dark:bg-slate-800">{{ row.cronExpression }}</code>
        </template>
      </el-table-column>
      <el-table-column label="运行状态" width="100">
        <template #default="{ row }">
          <span class="inline-flex items-center gap-1.5">
            <span
              :class="[
                'inline-block h-2 w-2 rounded-full',
                row.status === 1 ? 'bg-emerald-500 animate-pulse' : 'bg-slate-400',
              ]"
            />
            <span :class="row.status === 1 ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-500'">
              {{ row.status === 1 ? '运行中' : '已暂停' }}
            </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="280" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openTriggerConfirm(row)">执行一次</el-button>
          <el-button
            v-if="row.status === 1"
            type="warning"
            link
            size="small"
            @click="handlePause(row.id)"
          >
            暂停
          </el-button>
          <el-button
            v-else
            type="success"
            link
            size="small"
            @click="handleResume(row.id)"
          >
            恢复
          </el-button>
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="list.length === 0 && !loading" class="py-16">
      <EmptyState description="暂无定时任务" />
    </div>

    <div v-if="total > 0" class="mt-4 flex justify-center">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        background
        layout="prev, pager, next, total"
        @current-change="fetchList"
      />
    </div>

    <!-- Trigger confirm dialog -->
    <el-dialog v-model="triggerVisible" title="确认执行" width="420px">
      <div class="py-4 text-center">
        <el-icon class="mb-3 text-4xl text-primary"><VideoPlay /></el-icon>
        <p class="text-slate-700 dark:text-slate-300">
          确定要立即执行任务 <span class="font-bold text-primary">"{{ triggerTarget?.jobName }}"</span> 吗？
        </p>
        <p class="mt-2 text-xs text-slate-400">该操作将触发一次即时执行</p>
      </div>
      <template #footer>
        <el-button @click="triggerVisible = false">取消</el-button>
        <el-button type="primary" :loading="triggering" @click="confirmTrigger">确认执行</el-button>
      </template>
    </el-dialog>

    <!-- Edit cron dialog -->
    <el-dialog v-model="editVisible" title="编辑 Cron 表达式" width="460px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="任务名称">
          <el-input :model-value="editTarget?.jobName" disabled />
        </el-form-item>
        <el-form-item label="Cron 表达式">
          <el-input v-model="editForm.cronExpression" placeholder="请输入 Cron 表达式，如 0 0 2 * * ?" />
        </el-form-item>
        <el-form-item label="任务备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="常用示例">
          <div class="flex flex-wrap gap-1">
            <el-tag
              v-for="preset in cronPresets"
              :key="preset.label"
              class="cursor-pointer"
              size="small"
              @click="editForm.cronExpression = preset.value"
            >
              {{ preset.label }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="confirmEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { getJobList, triggerJob, pauseJob, resumeJob, updateJob } from '@/api/job/api'
import type { JobVO } from '@/api/job/type'
import { defaultJobQuery, tableColumns } from './data'
import { meMsgSuccess, meMsgError } from '@/utils/modal'

const loading = ref(false)
const list = ref<JobVO[]>([])
const total = ref(0)

const query = reactive({ ...defaultJobQuery })

// Trigger
const triggerVisible = ref(false)
const triggering = ref(false)
const triggerTarget = ref<JobVO | null>(null)

// Edit
const editVisible = ref(false)
const updating = ref(false)
const editTarget = ref<JobVO | null>(null)
const editForm = reactive({ cronExpression: '', remark: '' })

const cronPresets = [
  { label: '每天凌晨2点', value: '0 0 2 * * ?' },
  { label: '每15分钟', value: '0 */15 * * * ?' },
  { label: '每小时', value: '0 0 * * * ?' },
  { label: '每天凌晨3点', value: '0 0 3 * * ?' },
  { label: '每天凌晨4点', value: '0 0 4 * * ?' },
  { label: '每年1月1日', value: '0 0 0 1 1 ?' },
]

function doSearch() {
  query.page = 1
  fetchList()
}

function resetSearch() {
  Object.assign(query, defaultJobQuery)
  doSearch()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getJobList({
      page: query.page,
      pageSize: query.pageSize,
      jobName: query.jobName || undefined,
      jobGroup: query.jobGroup || undefined,
      status: query.status,
    })
    if (res.code === 0) {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function openTriggerConfirm(row: JobVO) {
  triggerTarget.value = row
  triggerVisible.value = true
}

async function confirmTrigger() {
  if (!triggerTarget.value) return
  triggering.value = true
  try {
    const res = await triggerJob(triggerTarget.value.id)
    if (res.code === 0) {
      meMsgSuccess({ message: res.msg || '执行成功' })
      triggerVisible.value = false
      fetchList()
    }
  } finally {
    triggering.value = false
  }
}

async function handlePause(id: number) {
  try {
    const res = await pauseJob(id)
    if (res.code === 0) {
      meMsgSuccess({ message: res.msg || '已暂停' })
      fetchList()
    }
  } catch { /* handled */ }
}

async function handleResume(id: number) {
  try {
    const res = await resumeJob(id)
    if (res.code === 0) {
      meMsgSuccess({ message: res.msg || '已恢复' })
      fetchList()
    }
  } catch { /* handled */ }
}

function openEdit(row: JobVO) {
  editTarget.value = row
  editForm.cronExpression = row.cronExpression
  editForm.remark = row.remark
  editVisible.value = true
}

async function confirmEdit() {
  if (!editTarget.value) return
  updating.value = true
  try {
    const res = await updateJob({
      id: editTarget.value.id,
      cronExpression: editForm.cronExpression,
      remark: editForm.remark,
    })
    if (res.code === 0) {
      meMsgSuccess({ message: '更新成功' })
      editVisible.value = false
      fetchList()
    }
  } finally {
    updating.value = false
  }
}

onMounted(() => fetchList())
</script>
