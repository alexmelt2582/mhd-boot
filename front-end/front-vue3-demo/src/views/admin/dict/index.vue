<template>
  <div class="layout-padding h-full">
    <el-row class="h-full" :gutter="16">
      <!-- 左侧：字典类型树 -->
      <el-col :span="6" class="h-full">
        <el-card class="h-full flex flex-col" shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="font-bold">字典类型</span>
              <div>
                <el-button type="primary" size="small" icon="Plus" @click="openTypeDialog()">
                  新增
                </el-button>
                <el-button size="small" icon="Refresh" @click="handleRefreshTree">
                  刷新
                </el-button>
              </div>
            </div>
          </template>
          <div class="flex-1 overflow-hidden">
            <el-input
              v-model="treeFilterText"
              placeholder="请输入字典项或名称"
              clearable
              class="mb-4"
              prefix-icon="Search"
            />
            <el-scrollbar class="h-full">
              <el-tree
                ref="dictTreeRef"
                :data="treeData"
                :props="{ children: '', label: 'dictName' }"
                :filter-node-method="filterTreeNode"
                @node-click="handleNodeClick"
                default-expand-all
                highlight-current
              >
                <template #default="{ data }">
                  <div class="flex justify-between items-center w-full pr-4 hover:bg-gray-50">
                    <div class="flex-1 truncate">
                      <span>{{ data.dictName }}</span>
                      <span class="text-gray-400 text-xs ml-2">{{ data.dictType }}</span>
                    </div>
                    <div class="opacity-0 hover:opacity-100 transition-opacity">
                      <el-button-group>
                        <el-button size="small" icon="Edit" @click.stop="openTypeDialog(data.id)"/>
                        <el-button
                          size="small"
                          icon="Delete"
                          type="danger"
                          @click.stop="handleDeleteType(data.id)"
                        />
                      </el-button-group>
                    </div>
                  </div>
                </template>
              </el-tree>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：字典数据项列表 -->
      <el-col :span="18" class="h-full">
        <el-card class="h-full flex flex-col" shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <div>
                <span class="font-bold">字典数据项</span>
                <span v-if="currentDictType" class="ml-2 text-sm text-gray-500">
                  (当前类型：{{ currentDictType.dictName }})
                </span>
              </div>
              <div>
                <el-button type="primary" size="small" icon="Plus" @click="openItemDialog()">
                  新增数据
                </el-button>
                <el-button size="small" icon="Refresh" @click="fetchItems()"/>
              </div>
            </div>
          </template>
          <div class="flex-1 overflow-hidden">
            <el-table
              :data="itemList"
              style="width: 100%; height: 100%"
              stripe
              border
              v-loading="itemLoading"
            >
              <el-table-column type="index" label="序号" width="60" align="center"/>
              <el-table-column prop="dictLabel" label="字典标签" align="center"/>
              <el-table-column prop="dictValue" label="字典键值" align="center"/>
              <el-table-column prop="sort" label="排序" width="80" align="center"/>
              <el-table-column label="状态" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === '0' ? 'success' : 'danger'">
                    {{ row.status === '0' ? '正常' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" show-overflow-tooltip/>
              <el-table-column prop="createTime" label="创建时间" align="center" width="180"/>
              <el-table-column label="操作" width="150" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openItemDialog(row)">
                    修改
                  </el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteItem(row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <!-- 分页 -->
          <div class="pt-4 flex justify-end">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="fetchItems"
              @current-change="fetchItems"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 弹窗组件 -->
    <DictTypeForm ref="dictTypeFormRef" @refresh="handleRefreshTree"/>
    <DictItemForm ref="dictItemFormRef" @refresh="fetchItems"/>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref, watch} from 'vue';
import DictTypeForm from './form.vue';
import DictItemForm from './item-form.vue';
import {useMessage, useMessageBox} from "@/hooks/message.ts";
import {getAllDicts} from "@/api/admin/system/dict/api.ts";

// 状态定义
const treeData = ref<any[]>([]);
const treeFilterText = ref('');
const dictTreeRef = ref();
const currentDictType = ref<any>(null);
const itemList = ref<any[]>([]);
const itemLoading = ref(false);
const pagination = reactive({current: 1, size: 20, total: 0});

// 弹窗组件Ref
const dictTypeFormRef = ref<InstanceType<typeof DictTypeForm>>();
const dictItemFormRef = ref<InstanceType<typeof DictItemForm>>();

// 1. 加载字典类型树
const fetchTree = async () => {
  try {
    const res = await getAllDicts();
    treeData.value = res.data || [];
    // 默认选中第一项
    if (treeData.value.length > 0) {
      handleNodeClick(treeData.value[0]);
    }
  } catch (err: any) {
    console.error(err.msg || '加载字典树失败')
  }
};

// 2. 树的过滤逻辑
const filterTreeNode = (value: string, data: any) => {
  if (!value) return true;
  return data.dictName.includes(value) || data.dictType.includes(value);
};
watch(treeFilterText, (val) => {
  dictTreeRef.value!.filter(val);
});

// 3. 点击树节点
const handleNodeClick = (data: any) => {
  currentDictType.value = data;
  pagination.current = 1;
  fetchItems();
};

// 4. 加载字典数据项列表
const fetchItems = async () => {
  if (!currentDictType.value) return;
  itemLoading.value = true;
  try {
    // const res = await getDictItems({
    //   dictType: currentDictType.value.dictType
    // });
    // 由于Mock模拟没有分页，这里手动截取
    // const allData = res.data || [];
    // pagination.total = allData.length;
    // itemList.value = allData.slice((pagination.current - 1) * pagination.size, pagination.current * pagination.size);
  } catch (err: any) {
    console.error(err.msg || '加载数据项失败')
  } finally {
    itemLoading.value = false;
  }
};

// 5. 刷新树
const handleRefreshTree = () => {
  fetchTree();
};

// 6. 打开字典类型弹窗（新增/编辑）
const openTypeDialog = (id?: string | number) => {
  dictTypeFormRef.value?.openDialog(id);
};

// 7. 打开字典项弹窗（新增/编辑）
const openItemDialog = (row?: any) => {
  if (!currentDictType.value && !row) {
    useMessage().warning('请先选择一个字典类型');
    return;
  }
  const data = row ? {...row} : {dictType: currentDictType.value?.dictType};
  dictItemFormRef.value?.openDialog(data);
};

// 8. 删除字典类型
const handleDeleteType = async (id: string | number) => {
  try {
    await useMessageBox().confirm('确认删除该字典类型吗？删除后关联数据将被移除');
    // await deleteDictType([id]);
    useMessage().success('删除成功');
    fetchTree();
  } catch (err: any) {
    if (err !== 'cancel') useMessage().error(err.msg || '删除失败');
  }
};

// 9. 删除字典项
const handleDeleteItem = async (id: string | number) => {
  try {
    await useMessageBox().confirm('确认删除该字典数据项吗？');
    // await deleteDictItem([id]);
    useMessage().success('删除成功');
    fetchItems();
  } catch (err: any) {
    if (err !== 'cancel') useMessage().error(err.msg || '删除失败');
  }
};

onMounted(() => {
  fetchTree();
});
</script>

<style scoped>
.layout-padding {
  padding: 16px;
}

.h-full {
  height: 100%;
}

:deep(.el-card__body) {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
}
</style>
