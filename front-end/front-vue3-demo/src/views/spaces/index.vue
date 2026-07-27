<template>
  <div class="mx-auto max-w-7xl px-4 py-8 sm:px-6">
    <PageHeader title="空间浏览" description="浏览和筛选可预约的图书馆空间" />

    <!-- Filters -->
    <div class="mb-6 flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4 dark:border-white/5 dark:bg-slate-900">
      <div class="w-full sm:w-48">
        <el-input v-model="query.keyword" placeholder="搜索空间名称" clearable @clear="doSearch" @keyup.enter="doSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div>
        <el-select v-model="query.spaceType" placeholder="空间类型" clearable class="w-32" @change="doSearch">
          <el-option v-for="o in SpaceTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
      <div>
        <el-select v-model="query.areaName" placeholder="区域" clearable class="w-36" @change="doSearch">
          <el-option v-for="o in AreaNameOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
      <div>
        <el-select v-model="query.floor" placeholder="楼层" clearable class="w-28" @change="doSearch">
          <el-option v-for="o in FloorOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>
      <el-button type="primary" @click="doSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <div class="ml-auto">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="card">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="table">
            <el-icon><List /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- Card view -->
    <div v-if="viewMode === 'card'" v-loading="loading">
      <div v-if="spaceList.length === 0" class="py-16">
        <EmptyState description="未找到符合条件的学习空间" />
      </div>
      <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <div
          v-for="space in spaceList"
          :key="space.id"
          @click="goDetail(space.id)"
          class="group cursor-pointer rounded-xl border border-slate-200 bg-white p-5 transition-all hover:-translate-y-1 hover:border-primary/30 hover:shadow-lg dark:border-white/5 dark:bg-slate-900"
        >
          <div class="mb-3 flex items-start justify-between">
            <h3 class="font-bold text-slate-900 transition-colors group-hover:text-primary dark:text-slate-100">{{ space.spaceName }}</h3>
            <StatusTag :status="space.status" type="space" />
          </div>
          <div class="space-y-1.5 text-sm text-slate-500 dark:text-slate-400">
            <div class="flex items-center gap-2">
              <el-icon><Location /></el-icon>
              <span>{{ space.areaName }} · {{ space.floor }}</span>
            </div>
            <div class="flex items-center gap-2">
              <el-icon><User /></el-icon>
              <span>{{ space.spaceType === 'ROOM' ? `容纳 ${space.capacity} 人` : '单人座位' }}</span>
            </div>
            <div v-if="space.equipmentConfig && hasEquipment(space.equipmentConfig)" class="flex flex-wrap gap-1.5 pt-1">
              <el-tag v-if="space.equipmentConfig.power" size="small" type="info">电源</el-tag>
              <el-tag v-if="space.equipmentConfig.projector" size="small" type="warning">投影仪</el-tag>
              <el-tag v-if="space.equipmentConfig.whiteboard" size="small" type="success">白板</el-tag>
              <el-tag v-if="space.equipmentConfig.network" size="small" type="primary">WiFi</el-tag>
              <el-tag v-if="space.equipmentConfig.sofa" size="small">沙发</el-tag>
              <el-tag v-if="space.equipmentConfig.videoConf" size="small" type="danger">视频会议</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table view -->
    <div v-else v-loading="loading">
      <el-table :data="spaceList" stripe class="dark:!bg-slate-900" @row-click="(row: any) => goDetail(row.id)">
        <el-table-column prop="spaceName" label="名称" min-width="150" />
        <el-table-column prop="spaceType" label="类型" width="80">
          <template #default="{ row }">
            {{ SpaceTypeMap[row.spaceType] || row.spaceType }}
          </template>
        </el-table-column>
        <el-table-column prop="areaName" label="区域" width="120" />
        <el-table-column prop="floor" label="楼层" width="60" />
        <el-table-column prop="capacity" label="容纳人数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="space" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        background
        layout="prev, pager, next"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SpaceVO } from '@/api/space/type'
import { getSpaceList } from '@/api/space/api'
import { SpaceTypeMap, SpaceTypeOptions, AreaNameOptions, FloorOptions } from './enums'

const router = useRouter()
const route = useRoute()

const viewMode = ref<'card' | 'table'>('card')
const loading = ref(false)
const spaceList = ref<SpaceVO[]>([])
const total = ref(0)

const query = reactive({
  keyword: (route.query.keyword as string) || '',
  spaceType: (route.query.spaceType as string) || '',
  areaName: (route.query.areaName as string) || '',
  floor: (route.query.floor as string) || '',
  page: 1,
  pageSize: 12,
})

function hasEquipment(config: any): boolean {
  return Object.values(config).some((v) => v && v !== 'normal')
}

function goDetail(id: number) {
  router.push(`/spaces/${id}`)
}

function doSearch() {
  query.page = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getSpaceList(query)
    if (res.code === 0) {
      spaceList.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchList())
</script>
