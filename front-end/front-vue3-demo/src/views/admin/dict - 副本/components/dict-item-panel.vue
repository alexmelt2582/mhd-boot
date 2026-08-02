<template>
  <div class="flex flex-col h-full">
    <!-- 头部信息 -->
    <div class="p-4 border-b border-gray-200">
      <template v-if="dictType">
        <div class="flex justify-between items-center">
          <div>
            <div class="flex items-center gap-2">
              <h3 class="text-lg font-semibold text-gray-800">{{ dictType.dictName }}</h3>
            </div>
            <p class="text-sm text-gray-500 mt-1" v-if="dictType.remark">
              {{ dictType.remark }}
            </p>
          </div>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon>
              <Plus/>
            </el-icon>
            新增字典项
          </el-button>
        </div>
      </template>
      <template v-else>
        <div class="flex flex-col items-center justify-center py-8 text-gray-400">
          <el-icon :size="48">
            <ArrowLeft/>
          </el-icon>
          <p class="mt-2">请在左侧选择字典类型</p>
        </div>
      </template>
    </div>

    <!-- 字典项列表 -->
    <div class="flex-1 overflow-y-auto p-4" v-if="dictType">
      <div class="flex gap-2 mb-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索字典标签或值..."
          clearable
          size="small"
          class="w-64"
        >
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterStatus"
          placeholder="状态筛选"
          clearable
          size="small"
          class="w-32"
        >
          <el-option
            v-for="option in DICT_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-button
          size="small"
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>

      <div v-if="loading" class="flex justify-center items-center py-20">
        <el-icon class="is-loading" :size="32">
          <Loading/>
        </el-icon>
      </div>

      <template v-else-if="filteredDictItems.length > 0">
        <el-table
          :data="filteredDictItems"
          border
          stripe
          class="w-full"
          @-selectionchange="handleSelectionChange"
          :max-height="tableHeight"
        >
          <el-table-column type="selection" width="50" align="center"/>
          <el-table-column prop="dictLabel" label="字典标签" min-width="150">
            <template #default="{ row }">
              <el-tag size="small">{{ row.dictLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dictValue" label="字典值" min-width="120">
            <template #default="{ row }">
              <el-tag size="small" type="info">{{ row.dictValue }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dictSort" label="排序" width="80" align="center"/>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip/>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-else class="flex flex-col items-center justify-center py-20 text-gray-400">
        <el-icon :size="48">
          <Document/>
        </el-icon>
        <p class="mt-2">暂无字典数据</p>
      </div>
    </div>

    <!-- 字典数据弹窗 -->
    <DictItemDialog
      v-model="dialogVisible"
      :edit-data="editData"
      :dict-type-options="dictTypeOptions"
      :current-dict-type="dictType?.dictType"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {ArrowLeft, Document, Loading, Plus, Search} from '@element-plus/icons-vue'
import {
  deleteDictItem,
  getAllDicts,
  getDictByType,
  updateDictItem
} from "@/api/admin/system/dict/api";
import type {DictItemVO, DictTypeVO} from "@/api/admin/system/dict/type";
import {DICT_STATUS_MAP, DICT_STATUS_OPTIONS, DictStatus} from '../data'
import DictItemDialog from "@/views/admin/dict/components/dict-item-dialog.vue";

const props = defineProps<{
  dictType: DictTypeVO | null
}>()

const emit = defineEmits<{
  refresh: []
}>()

const dictItems = ref<DictItemVO[]>([])
const dictTypeOptions = ref<DictTypeVO[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref<number | ''>('')
const selectedIds = ref<number[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const editData = ref<DictItemVO | null>(null)

const tableHeight = computed(() => window.innerHeight - 350)

// 过滤后的数据
const filteredDictItems = computed(() => {
  let result = dictItems.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.dictLabel.toLowerCase().includes(keyword) ||
      item.dictValue.toLowerCase().includes(keyword)
    )
  }

  if (filterStatus.value !== '') {
    result = result.filter(item => item.status === filterStatus.value)
  }

  return result
})

// 获取字典类型列表（用于弹窗选择）
const fetchDictTypeOptions = async () => {
  try {
    const {data} = await getAllDicts()
    dictTypeOptions.value = data || []
  } catch (error) {
    console.error('获取字典类型失败:', error)
  }
}

// 获取字典数据
const fetchDictItems = async () => {
  if (!props.dictType) return

  loading.value = true
  try {
    const {data} = await getDictByType(props.dictType.dictType)
    dictItems.value = data || []
  } catch (error) {
    console.error('获取字典数据失败:', error)
  } finally {
    loading.value = false
  }
}

watch(() => props.dictType, () => {
  if (props.dictType) {
    fetchDictItems()
  } else {
    dictItems.value = []
  }
  searchKeyword.value = ''
  filterStatus.value = ''
  selectedIds.value = []
})

// 新增
const handleAdd = () => {
  editData.value = null
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: DictItemVO) => {
  editData.value = row
  dialogVisible.value = true
}

// 删除
const handleDelete = (row: DictItemVO) => {
  ElMessageBox.confirm(
    `确定要删除字典项"${row.dictLabel}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDictItem(row.dictItemId)
      ElMessage.success('删除成功')
      fetchDictItems()
      emit('refresh')
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的字典项')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的${selectedIds.value.length}个字典项吗？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await Promise.all(selectedIds.value.map(id => deleteDictItem(id)))
      ElMessage.success('批量删除成功')
      fetchDictItems()
      emit('refresh')
    } catch (error) {
      console.error('批量删除失败:', error)
    }
  })
}

// 表格选择
const handleSelectionChange = (selection: DictItemVO[]) => {
  selectedIds.value = selection.map(item => item.dictItemId)
}

// 操作成功
const handleSuccess = () => {
  fetchDictItems()
  emit('refresh')
}

onMounted(() => {
  fetchDictTypeOptions()
  if (props.dictType) {
    fetchDictItems()
  }
})
</script>
