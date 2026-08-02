<!-- views/system/dict/components/DictTypeList.vue -->
<template>
  <div class="flex flex-col h-full">
    <!-- 头部操作区 -->
    <div class="p-4 border-b border-gray-200">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-gray-800">字典类型</h3>
        <el-button type="primary" size="small" @click="handleAdd">
          <el-icon>
            <Plus/>
          </el-icon>
          新增
        </el-button>
      </div>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索字典类型..."
        clearable
        size="small"
        class="w-full"
      >
        <template #prefix>
          <el-icon>
            <Search/>
          </el-icon>
        </template>
      </el-input>
    </div>

    <!-- 字典类型列表 -->
    <div class="flex-1 overflow-y-auto">
      <div v-if="loading" class="flex justify-center items-center h-full">
        <el-icon class="is-loading" :size="24">
          <Loading/>
        </el-icon>
      </div>

      <template v-else-if="filteredDictTypes.length > 0">
        <div
          v-for="item in filteredDictTypes"
          :key="item.dictId"
          :class="[
            'px-4 py-3 cursor-pointer transition-colors duration-200 border-b border-gray-100',
            'hover:bg-blue-50',
            activeType?.dictId === item.dictId ? 'bg-blue-50 border-l-4 border-l-blue-500' : 'border-l-4 border-l-transparent'
          ]"
          @click="handleSelect(item)"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1 min-w-0">
              <div class="font-medium text-gray-900 truncate">{{ item.dictName }}</div>
              <div class="text-xs mt-1">
                <el-tag size="small" type="success">{{ item.dictType }}</el-tag>
              </div>
              <div class="text-xs text-gray-400 mt-1 truncate" v-if="item.remark">
                {{ item.remark }}
              </div>
            </div>

            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, item)">
              <el-button text size="small" class="ml-2" @click.stop>
                <el-icon>
                  <MoreFilled/>
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon>
                      <Edit/>
                    </el-icon>
                    编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon>
                      <Delete/>
                    </el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="flex items-center gap-4 mt-2 text-xs text-gray-400">
            <span>{{ item.createTime }}</span>
          </div>
        </div>
      </template>

      <div v-else class="flex flex-col items-center justify-center h-full text-gray-400">
        <el-icon :size="48">
          <FolderOpened/>
        </el-icon>
        <p class="mt-2">暂无字典类型</p>
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="p-3 border-t border-gray-200">
      <el-button size="small" class="w-full" @click="handleRefreshCache">
        <el-icon>
          <RefreshRight/>
        </el-icon>
        刷新字典缓存
      </el-button>
    </div>

    <!-- 字典类型弹窗 -->
    <dict-type-dialog
      v-model="dialogVisible"
      :dict-id="currentEditId"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Delete,
  Edit,
  FolderOpened,
  Loading,
  MoreFilled,
  Plus,
  RefreshRight,
  Search
} from '@element-plus/icons-vue'
import DictTypeDialog from "./dict-type-dialog.vue";
import {deleteDictType, getAllDicts, refreshDictCache} from "@/api/admin/system/dict/api.ts";
import {useMessage} from "@/hooks/message.ts";
import type {DictTypeVO} from "@/api/admin/system/dict/type.ts";

const props = defineProps<{
  activeType: DictTypeVO | null
}>()

const emit = defineEmits<{
  select: [type: DictTypeVO]
  refresh: []
}>()

const dictTypes = ref<DictTypeVO[]>([])
const searchKeyword = ref('')
const loading = ref(false)

// 弹窗相关
/** 弹窗显示状态 */
const dialogVisible = ref(false)

/** 当前编辑的字典ID，null 表示新增 */
const currentEditId = ref<number | null>(null)

/** 新增 */
const handleAdd = () => {
  currentEditId.value = null
  dialogVisible.value = true
}

/** 编辑 */
const handleEdit = (dictId: number) => {
  currentEditId.value = dictId
  dialogVisible.value = true
}

/** 操作成功后的回调 */
const handleSuccess = () => {
  fetchDictTypes()
}

// 过滤后的字典类型
const filteredDictTypes = computed(() => {
  if (!searchKeyword.value) return dictTypes.value
  const keyword = searchKeyword.value.toLowerCase()
  return dictTypes.value.filter(item =>
    item.dictName.toLowerCase().includes(keyword) ||
    item.dictType.toLowerCase().includes(keyword)
  )
})

// 获取字典类型列表
const fetchDictTypes = async () => {
  loading.value = true
  try {
    const {data} = await getAllDicts()
    dictTypes.value = data || []
  } catch (error) {
    console.error('获取字典类型失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择字典类型
const handleSelect = (item: DictTypeVO) => {
  emit('select', item)
}

// 删除
const handleDelete = (item: DictTypeVO) => {
  ElMessageBox.confirm(
    `确定要删除字典类型"${item.dictName}"吗？删除后该类型下的所有字典数据将被清空！`,
    '危险操作',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await deleteDictType(item.dictId)
      ElMessage.success('删除成功')
      if (props.activeType?.dictId === item.dictId) {
        emit('select', null as any)
      }
      fetchDictTypes()
      emit('refresh')
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

// 处理下拉菜单命令
const handleCommand = (command: string, item: DictTypeVO) => {
  switch (command) {
    case 'edit':
      handleEdit(item)
      break
    case 'delete':
      handleDelete(item)
      break
  }
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await refreshDictCache()
    useMessage().info('缓存刷新成功')
  } catch (error) {
    console.error('刷新缓存失败:', error)
  }
}

onMounted(() => {
  fetchDictTypes()
})

defineExpose({
  fetchDictTypes
})
</script>
