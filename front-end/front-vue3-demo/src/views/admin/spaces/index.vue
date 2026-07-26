<template>
  <div>
    <PageHeader title="空间管理" description="管理图书馆所有空间资源，包括座位和研讨室">
      <template #actions>
        <el-button type="primary" @click="openModal(null)">新增空间</el-button>
      </template>
    </PageHeader>

    <!-- Search form -->
    <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <el-input v-model="query.keyword" placeholder="搜索空间名称" class="w-48" clearable @keyup.enter="doSearch" />
      <el-select v-model="query.spaceType" placeholder="空间类型" class="w-32" clearable @change="doSearch">
        <el-option v-for="o in SpaceTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.areaName" placeholder="区域" class="w-36" clearable @change="doSearch">
        <el-option v-for="o in AreaNameOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.floor" placeholder="楼层" class="w-28" clearable @change="doSearch">
        <el-option v-for="o in FloorOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" class="w-32" clearable @change="doSearch">
        <el-option v-for="o in StatusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- Table -->
    <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="spaceName" label="空间名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="空间类型" width="90">
          <template #default="{ row }">{{ SpaceTypeMap[row.spaceType] || row.spaceType }}</template>
        </el-table-column>
        <el-table-column prop="areaName" label="区域" width="110" />
        <el-table-column prop="floor" label="楼层" width="70" />
        <el-table-column prop="capacity" label="容纳人数" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="space" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openModal(row)">编辑</el-button>
            <el-button
              type="warning" link size="small"
              @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm title="确认删除该空间？" confirm-button-text="确定" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="暂无空间数据" />
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

    <!-- Modal -->
    <SpaceModal v-model="modalVisible" :editing-id="editingId" :edit-data="editData" @success="fetchList" />
  </div>
</template>

<script setup lang="ts">
import type { SpaceVO } from '@/api/space/type'
import { getSpaceList, deleteSpace, updateSpaceStatus } from '@/api/space/api'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import { SpaceTypeOptions, AreaNameOptions, FloorOptions, StatusOptions, SpaceTypeMap, defaultQuery } from './data'
import SpaceModal from './modal.vue'

const loading = ref(false)
const list = ref<SpaceVO[]>([])
const total = ref(0)
const query = reactive({ ...defaultQuery })

const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const editData = ref<SpaceVO | null>(null)

function openModal(row: SpaceVO | null) {
  if (row) {
    editingId.value = row.id
    editData.value = { ...row }
  } else {
    editingId.value = null
    editData.value = null
  }
  modalVisible.value = true
}

async function handleDelete(id: number) {
  try {
    await deleteSpace(id)
    meMsgSuccess({ message: '删除成功' })
    fetchList()
  } catch { /* handled */ }
}

async function toggleStatus(row: SpaceVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '停用'
  try {
    await updateSpaceStatus(row.id, newStatus)
    meMsgSuccess({ message: `已${label}` })
    fetchList()
  } catch { /* handled */ }
}

function doSearch() {
  query.page = 1
  fetchList()
}

function resetSearch() {
  query.keyword = ''
  query.spaceType = ''
  query.areaName = ''
  query.floor = ''
  query.status = undefined
  doSearch()
}

function handleSizeChange() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getSpaceList(query)
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
