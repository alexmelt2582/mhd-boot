<template>
  <div>
    <PageHeader title="设备管理" description="管理图书馆所有设备，支持分配/取消分配到空间">
      <template #actions>
        <el-button type="primary" @click="openModal(null)">新增设备</el-button>
      </template>
    </PageHeader>

    <!-- Search form -->
    <div class="mb-4 flex flex-wrap gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <el-input v-model="query.keyword" placeholder="搜索设备名称/型号" class="w-48" clearable @keyup.enter="doSearch" />
      <el-select v-model="query.equipmentType" placeholder="设备类型" class="w-32" clearable @change="doSearch">
        <el-option v-for="o in EquipmentTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" class="w-28" clearable @change="doSearch">
        <el-option v-for="o in EquipmentStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- Table -->
    <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="130" show-overflow-tooltip />
        <el-table-column label="设备类型" width="100">
          <template #default="{ row }">{{ EquipmentTypeMap[row.equipmentType] || row.equipmentType }}</template>
        </el-table-column>
        <el-table-column prop="spaceName" label="所属空间" min-width="120">
          <template #default="{ row }">
            <span v-if="row.spaceId">{{ row.spaceName }}</span>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="equipmentModel" label="设备型号" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="equipment" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openModal(row)">编辑</el-button>
            <el-button
              v-if="!row.spaceId"
              type="success" link size="small"
              @click="openAssign(row)">
              分配
            </el-button>
            <el-button
              v-else
              type="warning" link size="small"
              @click="handleUnassign(row)">
              取消分配
            </el-button>
            <el-popconfirm title="确认删除该设备？" confirm-button-text="确定" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="暂无设备数据" />
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

    <!-- CRUD Modal -->
    <EquipmentModal v-model="modalVisible" :editing-id="editingId" :edit-data="editData" @success="fetchList" />

    <!-- Assign Dialog -->
    <el-dialog v-model="assignVisible" title="分配设备到空间" width="450px">
      <el-form label-width="80px">
        <el-form-item label="目标空间">
          <el-select v-model="assignSpaceId" class="w-full" placeholder="请选择空间" filterable>
            <el-option v-for="s in spaceOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!assignSpaceId" @click="handleAssign">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { EquipmentVO } from '@/api/equipment/type'
import { getEquipmentList, deleteEquipment, assignEquipment, unassignEquipment } from '@/api/equipment/api'
import { getSpaceList } from '@/api/space/api'
import type { SpaceVO } from '@/api/space/type'
import { meMsgSuccess } from '@/utils/modal'
import { EquipmentTypeOptions, EquipmentStatusOptions, EquipmentTypeMap, defaultQuery } from './data'
import EquipmentModal from './modal.vue'

const loading = ref(false)
const list = ref<EquipmentVO[]>([])
const total = ref(0)
const query = reactive({ ...defaultQuery })

const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const editData = ref<EquipmentVO | null>(null)

// Assign
const assignVisible = ref(false)
const assignEquipmentId = ref<number | null>(null)
const assignSpaceId = ref<number | null>(null)
const spaceOptions = ref<{ label: string; value: number }[]>([])

async function loadSpaces() {
  try {
    const res = await getSpaceList({ page: 1, pageSize: 200 })
    if (res.code === 0) {
      spaceOptions.value = res.data.list.map((s: SpaceVO) => ({
        label: `${s.spaceName} (${s.areaName} ${s.floor})`,
        value: s.id,
      }))
    }
  } catch { /* ignore */ }
}

loadSpaces()

function openModal(row: EquipmentVO | null) {
  if (row) {
    editingId.value = row.id
    editData.value = { ...row }
  } else {
    editingId.value = null
    editData.value = null
  }
  modalVisible.value = true
}

function openAssign(row: EquipmentVO) {
  assignEquipmentId.value = row.id
  assignSpaceId.value = null
  assignVisible.value = true
}

async function handleAssign() {
  if (!assignEquipmentId.value || !assignSpaceId.value) return
  try {
    await assignEquipment({ equipmentId: assignEquipmentId.value, spaceId: assignSpaceId.value })
    meMsgSuccess({ message: '分配成功' })
    assignVisible.value = false
    fetchList()
  } catch { /* handled */ }
}

async function handleUnassign(row: EquipmentVO) {
  try {
    await unassignEquipment(row.id)
    meMsgSuccess({ message: '已取消分配' })
    fetchList()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await deleteEquipment(id)
    meMsgSuccess({ message: '删除成功' })
    fetchList()
  } catch { /* handled */ }
}

function doSearch() {
  query.page = 1
  fetchList()
}

function resetSearch() {
  query.keyword = ''
  query.equipmentType = undefined
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
    const res = await getEquipmentList(query)
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
