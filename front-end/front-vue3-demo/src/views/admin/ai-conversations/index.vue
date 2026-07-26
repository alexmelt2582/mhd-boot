<template>
  <div>
    <PageHeader title="AI 问答记录" description="查看用户与 AI 助手的对话记录，了解 AI 服务质量" />

    <!-- Search -->
    <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">用户关键词</label>
        <el-input v-model="query.keyword" placeholder="用户名" class="w-36" clearable @keyup.enter="doSearch" />
      </div>
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">对话类型</label>
        <el-select v-model="query.convType" placeholder="全部" class="w-28" clearable @change="doSearch">
          <el-option v-for="o in convTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
      <div>
        <label class="mb-1 block text-xs text-slate-500 dark:text-slate-400">日期范围</label>
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          class="w-60"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="onDateChange"
        />
      </div>
      <div class="self-end">
        <el-button type="primary" @click="doSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table
      :data="list"
      v-loading="loading"
      stripe
      border
      row-key="id"
      @expand-change="onRowExpand"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="p-4">
            <div class="mb-3 rounded-lg bg-blue-50 p-3 dark:bg-blue-500/10">
              <p class="mb-1 text-xs font-semibold text-blue-600 dark:text-blue-400">问题</p>
              <p class="text-sm text-slate-700 dark:text-slate-300">{{ row.question }}</p>
            </div>
            <div class="rounded-lg bg-emerald-50 p-3 dark:bg-emerald-500/10">
              <p class="mb-1 text-xs font-semibold text-emerald-600 dark:text-emerald-400">回答</p>
              <p class="text-sm text-slate-700 dark:text-slate-300">{{ row.answer }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="question" label="问题" min-width="280" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="cursor-pointer text-primary hover:underline">{{ truncate(row.question, 50) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="回答" min-width="280" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="text-slate-600 dark:text-slate-400">{{ truncate(row.answer, 50) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag
            :type="row.convType === 'RECOMMEND' ? 'warning' : 'primary'"
            size="small"
          >
            {{ row.convType === 'RECOMMEND' ? '推荐' : '问答' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收藏" width="70" align="center">
        <template #default="{ row }">
          <el-icon :size="16" :color="row.isFavorite ? '#f59e0b' : '#cbd5e1'">
            <StarFilled v-if="row.isFavorite" />
            <Star v-else />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column label="有用" width="70" align="center">
        <template #default="{ row }">
          <span v-if="row.isUseful === 1" class="text-emerald-500">
            <el-icon><CaretTop /></el-icon>
          </span>
          <span v-else-if="row.isUseful === 0" class="text-red-500">
            <el-icon><CaretBottom /></el-icon>
          </span>
          <span v-else class="text-slate-300">
            <el-icon><Minus /></el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="对话时间" width="180" />
      <el-table-column label="操作" width="80" fixed="right" align="center">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该记录？" confirm-button-text="删除" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="list.length === 0 && !loading" class="py-16">
      <EmptyState description="暂无 AI 对话记录" />
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
  </div>
</template>

<script setup lang="ts">
import { getConversations, deleteConversation } from '@/api/ai/api'
import type { AIConversationVO } from '@/api/ai/type'
import { defaultConversationQuery, tableColumns, convTypeOptions } from './data'
import { meMsgSuccess } from '@/utils/modal'

const loading = ref(false)
const list = ref<AIConversationVO[]>([])
const total = ref(0)

const query = reactive({ ...defaultConversationQuery })

function truncate(str: string, len: number): string {
  if (!str) return '-'
  return str.length > len ? str.slice(0, len) + '...' : str
}

function onDateChange(val: [string, string] | null) {
  if (val && val.length === 2) {
    query.startTime = val[0]
    query.endTime = val[1]
  } else {
    query.startTime = ''
    query.endTime = ''
  }
  doSearch()
}

function onRowExpand(row: any, expandedRows: any[]) {
  // expand toggle
}

function doSearch() {
  query.page = 1
  fetchList()
}

function resetSearch() {
  Object.assign(query, { ...defaultConversationQuery, dateRange: [] })
  doSearch()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getConversations({
      page: query.page,
      pageSize: query.pageSize,
      convType: query.convType || undefined,
      startTime: query.startTime || undefined,
      endTime: query.endTime || undefined,
    })
    if (res.code === 0) {
      let items = res.data.list
      // Client-side keyword filter for userName
      if (query.keyword) {
        const kw = query.keyword.toLowerCase()
        items = items.filter((c: AIConversationVO) =>
          c.userName?.toLowerCase().includes(kw) || c.question.toLowerCase().includes(kw),
        )
      }
      list.value = items
      total.value = items.length > 0 ? res.data.total : 0
    }
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    const res = await deleteConversation(id)
    if (res.code === 0) {
      meMsgSuccess({ message: '删除成功' })
      fetchList()
    }
  } catch { /* handled */ }
}

onMounted(() => fetchList())
</script>
