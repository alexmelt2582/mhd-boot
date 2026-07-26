<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <PageHeader title="消息中心" description="查看系统通知、预约提醒和积分变更消息" />

    <!-- Toolbar -->
    <div class="mb-4 flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 dark:border-white/5 dark:bg-slate-900">
      <el-button type="primary" plain size="small" :disabled="unreadCount === 0" @click="handleMarkAllRead">
        <el-icon class="mr-1"><Check /></el-icon>
        全部标为已读
      </el-button>
      <el-select v-model="filterType" placeholder="消息类型" clearable size="small" class="w-36" @change="doSearch">
        <el-option label="全部消息" value="" />
        <el-option label="系统消息" value="SYSTEM" />
        <el-option label="预约通知" value="RESERVATION" />
        <el-option label="积分通知" value="CREDIT" />
        <el-option label="违规提醒" value="VIOLATION" />
      </el-select>
      <span class="ml-auto text-xs text-slate-400 dark:text-slate-500">
        未读 <span class="font-medium text-primary">{{ unreadCount }}</span> 条
      </span>
    </div>

    <!-- Split panel -->
    <div class="flex gap-4">
      <!-- Left: message list -->
      <div class="w-full shrink-0 rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900 lg:w-80">
        <div v-loading="loading" class="max-h-[calc(100vh-280px)] overflow-y-auto">
          <EmptyState v-if="messageList.length === 0" description="暂无消息" :image-size="80" />

          <div
            v-for="msg in messageList"
            :key="msg.id"
            @click="selectMessage(msg)"
            class="cursor-pointer border-b border-slate-100 px-4 py-3.5 transition-colors hover:bg-slate-50 dark:border-white/5 dark:hover:bg-slate-800/50"
            :class="{ 'bg-primary/5 border-l-2 border-l-primary dark:bg-primary/10': selectedId === msg.id }"
          >
            <div class="flex items-start gap-3">
              <!-- Type icon -->
              <div class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm" :class="typeIconClass(msg.messageType)">
                <el-icon :size="14">
                  <Message v-if="msg.messageType === 'SYSTEM'" />
                  <Clock v-else-if="msg.messageType === 'RESERVATION'" />
                  <Coin v-else-if="msg.messageType === 'CREDIT'" />
                  <WarningFilled v-else />
                </el-icon>
              </div>

              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <span class="truncate text-sm font-medium text-slate-900 dark:text-slate-100">{{ msg.title }}</span>
                  <span v-if="msg.isRead === 0" class="inline-block h-2 w-2 shrink-0 rounded-full bg-primary"></span>
                </div>
                <p class="mt-0.5 truncate text-xs text-slate-500 dark:text-slate-400">{{ msg.content }}</p>
                <div class="mt-1.5 flex items-center gap-2">
                  <span class="text-xs text-slate-400 dark:text-slate-500">{{ formatTimePast(msg.sendTime) }}</span>
                  <el-tag size="small" :type="typeTagType(msg.messageType)" effect="plain">
                    {{ typeLabel(msg.messageType) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- List pagination -->
        <div v-if="total > query.pageSize" class="border-t border-slate-100 px-4 py-3 dark:border-white/5">
          <el-pagination
            v-model:current-page="query.page"
            :page-size="query.pageSize"
            :total="total"
            background
            small
            layout="prev, pager, next"
            @current-change="fetchList"
          />
        </div>
      </div>

      <!-- Right: message detail (desktop) -->
      <div class="hidden flex-1 rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900 lg:block">
        <div v-if="!selectedMessage" class="flex h-full items-center justify-center py-24">
          <div class="text-center">
            <el-icon :size="48" class="text-slate-300 dark:text-slate-600"><Message /></el-icon>
            <p class="mt-4 text-sm text-slate-400 dark:text-slate-500">选择左侧消息查看详情</p>
          </div>
        </div>

        <div v-else class="flex h-full flex-col p-6">
          <!-- Detail header -->
          <div class="mb-4 flex items-start justify-between">
            <div>
              <h2 class="text-lg font-bold text-slate-900 dark:text-slate-100">{{ selectedMessage.title }}</h2>
              <div class="mt-2 flex flex-wrap items-center gap-3 text-xs text-slate-500 dark:text-slate-400">
                <span class="flex items-center gap-1">
                  <el-icon :size="12"><Clock /></el-icon>
                  {{ selectedMessage.sendTime }}
                </span>
                <el-tag size="small" :type="typeTagType(selectedMessage.messageType)" effect="light">
                  {{ typeLabel(selectedMessage.messageType) }}
                </el-tag>
                <span v-if="selectedMessage.isRead === 1" class="text-emerald-500">已读</span>
                <span v-else class="text-primary">未读</span>
              </div>
            </div>
          </div>

          <!-- Detail content -->
          <div class="flex-1 overflow-y-auto rounded-lg bg-slate-50 p-4 dark:bg-slate-800/30">
            <p class="whitespace-pre-wrap text-sm leading-relaxed text-slate-700 dark:text-slate-300">
              {{ selectedMessage.content }}
            </p>
            <div v-if="selectedMessage.relatedId" class="mt-4 text-xs text-slate-400 dark:text-slate-500">
              关联业务ID：{{ selectedMessage.relatedId }}
            </div>
          </div>

          <!-- Detail actions -->
          <div class="mt-4 flex items-center gap-3 border-t border-slate-100 pt-4 dark:border-white/5">
            <el-button v-if="selectedMessage.isRead === 0" type="primary" size="small" @click="handleMarkRead(selectedMessage)">
              标记已读
            </el-button>
            <el-button type="danger" size="small" plain @click="handleDelete(selectedMessage)">
              <el-icon class="mr-1"><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Message as ElIconMessage, Clock, Coin, WarningFilled, Check, Delete } from '@element-plus/icons-vue'
import type { MessageVO, MessageType } from '@/api/message/type'
import { getMyMessages, getUnreadCount, markAsRead, markAllAsRead, deleteMessage } from '@/api/message/api'
import { formatTimePast } from '@/utils/date'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const messageList = ref<MessageVO[]>([])
const total = ref(0)
const unreadCount = ref(0)
const filterType = ref('')
const selectedId = ref<number | null>(null)
const selectedMessage = ref<MessageVO | null>(null)

const query = reactive({
  page: 1,
  pageSize: 12,
  messageType: '' as string,
})

function typeIconClass(type: MessageType): string {
  const map: Record<string, string> = {
    SYSTEM: 'bg-blue-50 text-blue-500 dark:bg-blue-500/10 dark:text-blue-400',
    RESERVATION: 'bg-emerald-50 text-emerald-500 dark:bg-emerald-500/10 dark:text-emerald-400',
    CREDIT: 'bg-amber-50 text-amber-500 dark:bg-amber-500/10 dark:text-amber-400',
    VIOLATION: 'bg-red-50 text-red-500 dark:bg-red-500/10 dark:text-red-400',
  }
  return map[type] || 'bg-slate-50 text-slate-400 dark:bg-slate-500/10 dark:text-slate-400'
}

function typeTagType(type: MessageType): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, string> = {
    SYSTEM: 'info',
    RESERVATION: 'success',
    CREDIT: 'warning',
    VIOLATION: 'danger',
  }
  return (map[type] as any) || 'info'
}

function typeLabel(type: MessageType): string {
  const map: Record<string, string> = {
    SYSTEM: '系统消息',
    RESERVATION: '预约通知',
    CREDIT: '积分通知',
    VIOLATION: '违规提醒',
  }
  return map[type] || type
}

function doSearch() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMyMessages({
      page: query.page,
      pageSize: query.pageSize,
      messageType: (filterType.value || undefined) as any,
    })
    if (res.code === 0) {
      messageList.value = res.data.list
      total.value = res.data.total
      // Auto-select first item if nothing selected
      if (!selectedId.value && res.data.list.length > 0) {
        selectMessage(res.data.list[0])
      }
    }
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    if (res.code === 0) {
      unreadCount.value = res.data
    }
  } catch { /* ignore */ }
}

function selectMessage(msg: MessageVO) {
  selectedId.value = msg.id
  selectedMessage.value = msg
}

async function handleMarkRead(msg: MessageVO) {
  try {
    const res = await markAsRead(msg.id)
    if (res.code === 0) {
      msg.isRead = 1
      meMsgSuccess({ message: '已标记为已读' })
      fetchUnreadCount()
    }
  } catch { /* ignore */ }
}

async function handleMarkAllRead() {
  if (unreadCount.value === 0) return
  try {
    const res = await markAllAsRead()
    if (res.code === 0) {
      messageList.value.forEach((m) => (m.isRead = 1))
      if (selectedMessage.value) selectedMessage.value.isRead = 1
      unreadCount.value = 0
      meMsgSuccess({ message: '已全部标记为已读' })
    }
  } catch { /* ignore */ }
}

async function handleDelete(msg: MessageVO) {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await deleteMessage(msg.id)
    if (res.code === 0) {
      meMsgSuccess({ message: '删除成功' })
      if (selectedId.value === msg.id) {
        selectedId.value = null
        selectedMessage.value = null
      }
      fetchList()
      fetchUnreadCount()
    }
  } catch { /* cancelled */ }
}

watch(filterType, () => doSearch())

onMounted(() => {
  fetchList()
  fetchUnreadCount()
})
</script>
