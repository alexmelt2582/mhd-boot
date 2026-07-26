<template>
  <SearchBar :config="searchConfig" @search="handleSearch">
    <!-- 插入自定义按钮 -->
    <!--    <template #extraButtons>-->
    <!--      <el-button type="success" @click="handleExport">导出</el-button>-->
    <!--      <el-button type="warning" @click="openDialog">新增用户</el-button>-->
    <!--    </template>-->
  </SearchBar>
</template>

<script setup lang="ts">
import type {SearchConfigItem} from '@/components/SearcherBar/type.ts'
import SearchBar from '@/components/SearcherBar/SearchBar.vue'

const page = ref(1)
const pageSize = ref(10)
let lastSearchParams: Record<string, any> = {}

const searchConfig: SearchConfigItem[] = [
  {
    type: 'input', label: '用户名', field: 'username',
    placeholder: '输入用户名', clearable: true
  },
  {
    type: 'select', label: '状态', field: 'status',
    options: [{label: '启用', value: 1}, {label: '禁用', value: 0}],
    clearable: true, filterable: true
  },
  {
    type: 'daterange', label: '注册时间', field: 'createdAt',
    valueFormat: 'YYYY-MM-DD',
    startField: 'createdAtStart',
    endField: 'createdAtEnd',
    clearable: true
  }
]

function handleSearch(params: Record<string, any>) {
  lastSearchParams = params
  page.value = 1
  fetchData()
}

function fetchData() {
  const query = {
    page: page.value,
    pageSize: pageSize.value,
    ...lastSearchParams
  }
  // userStore.getList(query)
}

onMounted(() => {
  fetchData()
})
</script>
