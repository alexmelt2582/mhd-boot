<template>
  <div class="h-full flex gap-4 p-4 bg-gray-50">
    <!-- ==================== 左侧：字典类型 ==================== -->
    <div class="w-full lg:w-[38%] xl:w-[35%] flex flex-col bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <!-- 头部 -->
      <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-1 h-5 bg-blue-500 rounded-full"></div>
          <h2 class="text-base font-semibold text-gray-800">字典类型</h2>
          <el-tag size="small" type="info" class="ml-1">{{ typeTotal }} 条</el-tag>
        </div>
        <div class="flex gap-2">
          <el-button
            type="primary"
            size="small"
            :icon="'Plus'"
            @click="openTypeAdd"
            class="transition-all duration-300 hover:scale-105"
          >
            新增
          </el-button>
          <el-button
            size="small"
            :icon="'Refresh'"
            @click="handleRefreshCache"
            class="transition-all duration-300 hover:scale-105"
          >
            刷新缓存
          </el-button>
        </div>
      </div>

      <!-- 搜索区 -->
      <div class="px-5 py-3 bg-gray-50/50 border-b border-gray-100">
        <el-form :model="typeQuery" inline class="dict-search-form">
          <el-form-item
            v-for="field in dictTypeSearchFields"
            :key="field.prop"
            class="mb-0 mr-2"
          >
            <el-input
              v-model="typeQuery[field.prop as keyof DictTypeQuery]"
              :placeholder="field.placeholder"
              clearable
              size="small"
              class="w-36"
              @keyup.enter="handleTypeSearch"
            />
          </el-form-item>
          <el-form-item class="mb-0">
            <el-button type="primary" size="small" :icon="'Search'" @click="handleTypeSearch">查询</el-button>
            <el-button size="small" :icon="'RefreshRight'" @click="handleTypeReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <div class="flex-1 overflow-hidden p-3">
        <el-table
          v-loading="typeLoading"
          :data="typeList"
          highlight-current-row
          stripe
          size="small"
          class="w-full h-full"
          @row-click="handleSelectType"
        >
          <el-table-column
            v-for="col in dictTypeColumns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth"
            :align="col.align"
            show-overflow-tooltip
          >
            <template #default="{ row }" v-if="col.slot">
              <div class="flex items-center justify-center gap-2">
                <el-button
                  link
                  type="primary"
                  size="small"
                  :icon="'Edit'"
                  @click.stop="openTypeEdit(row)"
                  class="transition-all duration-300 hover:scale-110"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  :icon="'Delete'"
                  @click.stop="handleTypeDelete(row)"
                  class="transition-all duration-300 hover:scale-110"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>

          <!-- 空状态 -->
          <template #empty>
            <el-empty description="暂无字典类型" :image-size="80" />
          </template>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="px-4 py-3 border-t border-gray-100 flex justify-end">
        <el-pagination
          v-model:current-page="typeQuery.pageNo"
          v-model:page-size="typeQuery.pageSize"
          :total="typeTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          size="small"
          @size-change="handleTypeSizeChange"
          @current-change="handleTypePageChange"
        />
      </div>
    </div>

    <!-- ==================== 右侧：字典数据 ==================== -->
    <div class="w-full lg:w-[62%] xl:w-[65%] flex flex-col bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <!-- 头部 -->
      <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-1 h-5 bg-emerald-500 rounded-full"></div>
          <h2 class="text-base font-semibold text-gray-800">字典数据</h2>
          <el-tag
            v-if="hasSelectedType"
            type="success"
            size="small"
            class="ml-1 font-mono"
          >
            {{ selectedType?.dictName }} ({{ selectedType?.dictType }})
          </el-tag>
          <el-tag v-else type="info" size="small" class="ml-1">请先选择左侧字典类型</el-tag>
        </div>
        <el-button
          type="primary"
          size="small"
          :icon="'Plus'"
          :disabled="!hasSelectedType"
          @click="openItemAdd"
          class="transition-all duration-300 hover:scale-105"
        >
          新增数据
        </el-button>
      </div>

      <!-- 搜索区 -->
      <div class="px-5 py-3 bg-gray-50/50 border-b border-gray-100">
        <el-form :model="itemQuery" inline class="dict-search-form">
          <el-form-item
            v-for="field in dictItemSearchFields"
            :key="field.prop"
            class="mb-0 mr-2"
          >
            <el-input
              v-if="field.type === 'input'"
              v-model="itemQuery[field.prop as keyof DictItemQuery]"
              :placeholder="field.placeholder"
              clearable
              size="small"
              class="w-36"
              @keyup.enter="handleItemSearch"
            />
            <el-select
              v-else-if="field.type === 'select'"
              v-model="itemQuery[field.prop as keyof DictItemQuery]"
              :placeholder="field.placeholder"
              clearable
              size="small"
              class="w-32"
            >
              <el-option
                v-for="opt in field.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item class="mb-0">
            <el-button type="primary" size="small" :icon="'Search'" @click="handleItemSearch">查询</el-button>
            <el-button size="small" :icon="'RefreshRight'" @click="handleItemReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <div class="flex-1 overflow-hidden p-3">
        <el-table
          v-loading="itemLoading"
          :data="itemList"
          stripe
          size="small"
          class="w-full h-full"
        >
          <el-table-column
            v-for="col in dictItemColumns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth"
            :align="col.align"
            show-overflow-tooltip
          >
            <!-- 状态列自定义渲染 -->
            <template #default="{ row }" v-if="col.prop === 'status'">
              <el-tag
                :type="getStatusOption(row.status).type"
                size="small"
                effect="light"
                class="transition-all duration-300"
              >
                {{ getStatusOption(row.status).label }}
              </el-tag>
            </template>

            <!-- 操作列 -->
            <template #default="{ row }" v-else-if="col.slot">
              <div class="flex items-center justify-center gap-2">
                <el-button
                  link
                  type="primary"
                  size="small"
                  :icon="'Edit'"
                  @click="openItemEdit(row)"
                  class="transition-all duration-300 hover:scale-110"
                >
                  编辑
                </el-button>
                <el-button
                  link
                  type="danger"
                  size="small"
                  :icon="'Delete'"
                  @click="handleItemDelete(row)"
                  class="transition-all duration-300 hover:scale-110"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>

          <!-- 空状态 -->
          <template #empty>
            <el-empty
              :description="hasSelectedType ? '该类型下暂无字典数据' : '请先选择左侧字典类型'"
              :image-size="80"
            />
          </template>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="px-4 py-3 border-t border-gray-100 flex justify-end">
        <el-pagination
          v-model:current-page="itemQuery.pageNo"
          v-model:page-size="itemQuery.pageSize"
          :total="itemTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          size="small"
          @size-change="handleItemSizeChange"
          @current-change="handleItemPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DictItemPanel from "@/views/admin/dict/components/dict-item-panel.vue";
import DictTypeList from "@/views/admin/dict/components/dict-type-list.vue";
import type {
  DictItemQuery,
  DictItemVO,
  DictTypeQuery,
  DictTypeVO
} from "@/api/admin/system/dict/type.ts";
import {useMessage, useMessageBox} from "@/hooks/message.ts";
import {
  deleteDictItem,
  deleteDictType,
  pageDictItems, pageDictTypes,
  refreshDictCache
} from "@/api/admin/system/dict/api.ts";

/* ==================== 左侧：字典类型 ==================== */

/** 类型表格加载状态 */
const typeLoading = ref(false)

/** 类型表格数据 */
const typeList = ref<DictTypeVO[]>([])

/** 类型总条数 */
const typeTotal = ref(0)

/** 类型查询参数 */
const typeQuery = reactive<DictTypeQuery>({
  pageNo: 1,
  pageSize: 10,
  dictName: '',
  dictType: ''
})

/** 当前选中的字典类型 */
const selectedType = ref<DictTypeVO | null>(null)

/** 类型编辑弹窗 */
const typeModalVisible = ref(false)
const typeEditData = ref<DictTypeVO | null>(null)

/* ==================== 右侧：字典数据 ==================== */

/** 数据表格加载状态 */
const itemLoading = ref(false)

/** 数据表格数据 */
const itemList = ref<DictItemVO[]>([])

/** 数据总条数 */
const itemTotal = ref(0)

/** 数据查询参数 */
const itemQuery = reactive<DictItemQuery>({
  pageNo: 1,
  pageSize: 10,
  dictType: '',
  dictLabel: '',
})

/** 数据编辑弹窗 */
const itemModalVisible = ref(false)
const itemEditData = ref<DictItemVO | null>(null)

/* ==================== 计算属性 ==================== */

/** 是否已选中字典类型 */
const hasSelectedType = computed(() => !!selectedType.value)

/** 当前选中类型的字典类型编码 */
const currentDictType = computed(() => selectedType.value?.dictType || '')

/* ==================== 生命周期 ==================== */

onMounted(() => {
  loadTypeList()
})

/* ==================== 字典类型 方法 ==================== */

/** 加载字典类型列表 */
async function loadTypeList() {
  typeLoading.value = true
  try {
    const res = await pageDictTypes({ ...typeQuery })
    if (res.data) {
      typeList.value = res.data.list
      typeTotal.value = res.data.total

      // 首次加载或选中项被删除时，默认选中第一项
      if (typeList.value.length > 0) {
        const stillExists = selectedType.value
          ? typeList.value.some(t => t.dictId === selectedType.value!.dictId)
          : false
        if (!stillExists) {
          handleSelectType(typeList.value[0]!)
        }
      } else {
        selectedType.value = null
        itemList.value = []
        itemTotal.value = 0
      }
    }
  } finally {
    typeLoading.value = false
  }
}
/** 类型搜索 */
function handleTypeSearch() {
  typeQuery.pageNo = 1
  loadTypeList()
}

/** 类型重置 */
function handleTypeReset() {
  typeQuery.dictName = ''
  typeQuery.dictType = ''
  typeQuery.pageNo = 1
  loadTypeList()
}

/** 选中字典类型 */
function handleSelectType(row: DictTypeVO) {
  selectedType.value = row
  // 同步刷新右侧数据
  itemQuery.dictType = row.dictType
  itemQuery.pageNo = 1
  itemQuery.dictLabel = ''
  loadItemList()
}

/** 打开类型新增弹窗 */
function openTypeAdd() {
  typeEditData.value = null
  typeModalVisible.value = true
}

/** 打开类型编辑弹窗 */
function openTypeEdit(row: DictTypeVO) {
  typeEditData.value = { ...row }
  typeModalVisible.value = true
}

/** 删除字典类型 */
async function handleTypeDelete(row: DictTypeVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除字典类型「${row.dictName}」吗？该类型下的所有字典数据也将被删除！`,
      '删除确认',
      { type: 'warning', confirmButtonClass: 'el-button--danger' }
    )
    const res = await deleteDictType(row.dictId)
    if (res) {
      useMessage().success('删除成功')
      loadTypeList()
    }
  } catch {
    // 用户取消
  }
}

/** 刷新缓存 */
async function handleRefreshCache() {
  try {
    const res = await refreshDictCache()
    if (res) {
      useMessage().success('字典缓存刷新成功')
    }
  } catch {
    // 错误已在拦截器处理
  }
}

/* ==================== 字典数据 方法 ==================== */

/** 加载字典数据列表 */
async function loadItemList() {
  if (!currentDictType.value) return
  itemLoading.value = true
  try {
    const res = await pageDictItems({ ...itemQuery })
    if (res.data) {
      itemList.value = res.data.list
      itemTotal.value = res.data.total
    }
  } finally {
    itemLoading.value = false
  }
}

/** 数据搜索 */
function handleItemSearch() {
  itemQuery.pageNo = 1
  loadItemList()
}

/** 数据重置 */
function handleItemReset() {
  itemQuery.dictLabel = ''
  itemQuery.pageNo = 1
  loadItemList()
}

/** 打开数据新增弹窗 */
function openItemAdd() {
  if (!hasSelectedType.value) {
    useMessage().warning('请先选择字典类型')
    return
  }
  itemEditData.value = null
  itemModalVisible.value = true
}

/** 打开数据编辑弹窗 */
function openItemEdit(row: DictItemVO) {
  itemEditData.value = { ...row }
  itemModalVisible.value = true
}

/** 删除字典数据 */
async function handleItemDelete(row: DictItemVO) {
  try {
    await useMessageBox().confirm(
      `确定删除字典数据「${row.dictLabel}」吗？`,
    )
    const res = await deleteDictItem(row.dictItemId)
    if (res) {
      useMessage().success('删除成功')
      loadItemList()
    }
  } catch {
    // 用户取消
  }
}

/* ==================== 分页 ==================== */

function handleTypePageChange(page: number) {
  typeQuery.pageNo = page
  loadTypeList()
}

function handleTypeSizeChange(size: number) {
  typeQuery.pageSize = size
  typeQuery.pageNo = 1
  loadTypeList()
}

function handleItemPageChange(page: number) {
  itemQuery.pageNo = page
  loadItemList()
}

function handleItemSizeChange(size: number) {
  itemQuery.pageSize = size
  itemQuery.pageNo = 1
  loadItemList()
}
</script>
