<template>
  <div class="p-6">
    <!-- 搜索栏 -->
    <SearchBar :config="searchConfig" @search="handleSearch" />

    <!-- 表格 -->
    <el-table :data="userStore.list" v-loading="userStore.loading" border stripe>
      <!-- 列省略 -->
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="userStore.total"
      @pagination="handlePagination"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// import SearchBar from '@/components/AdminSearchBar.vue'
// import Pagination from '@/components/Pagination.vue'
// import { useUserStore } from '@/stores/user'

// const userStore = useUserStore()
const page = ref(1)
const pageSize = ref(10)
let lastSearchParams: Record<string, any> = {}

const handleSearch = (params: Record<string, any>) => {
  lastSearchParams = params
  page.value = 1
  fetchData()
}

const handlePagination = (payload: { page: number; pageSize: number }) => {
  page.value = payload.page
  pageSize.value = payload.pageSize
  fetchData()
}

const fetchData = () => {
  // userStore.getList({
  //   page: page.value,
  //   pageSize: pageSize.value,
  //   ...lastSearchParams
  // })
}
</script>
