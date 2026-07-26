<template>
  <div>
    <PageHeader title="积分规则配置" description="配置信用积分加减分规则，控制用户信用体系">
      <template #actions>
        <el-button type="primary" @click="openModal(null)">新增规则</el-button>
      </template>
    </PageHeader>

    <!-- Table -->
    <div class="rounded-xl border border-slate-200 bg-white dark:border-white/5 dark:bg-slate-900">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="规则类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.ruleType === 'REWARD' ? 'success' : 'danger'" size="small">
              {{ row.ruleType === 'REWARD' ? '加分' : '扣分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分值变动" width="90">
          <template #default="{ row }">
            <span :class="row.changeValue > 0 ? 'font-bold text-emerald-500' : 'font-bold text-red-500'">
              {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="规则描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isEnabled === 1"
              @change="(val: boolean) => handleToggleEnable(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openModal(row)">编辑</el-button>
            <el-popconfirm title="确认删除该规则？" confirm-button-text="确定" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading">
        <EmptyState description="暂无积分规则" />
      </div>
    </div>

    <!-- Modal -->
    <CreditRuleModal v-model="modalVisible" :editing-rule="editingRule" @success="fetchList" />
  </div>
</template>

<script setup lang="ts">
import type { CreditRuleVO } from '@/api/credit/type'
import { getCreditRules, saveCreditRule, deleteCreditRule } from '@/api/credit/api'
import { meMsgSuccess } from '@/utils/modal'
import CreditRuleModal from './modal.vue'

const loading = ref(false)
const list = ref<CreditRuleVO[]>([])

const modalVisible = ref(false)
const editingRule = ref<CreditRuleVO | null>(null)

function openModal(rule: CreditRuleVO | null) {
  editingRule.value = rule
  modalVisible.value = true
}

async function handleToggleEnable(row: CreditRuleVO, enabled: boolean) {
  try {
    await saveCreditRule({
      id: row.id,
      ruleName: row.ruleName,
      ruleType: row.ruleType,
      changeValue: row.changeValue,
      referenceType: row.referenceType,
      description: row.description,
      isEnabled: enabled ? 1 : 0,
    })
    meMsgSuccess({ message: enabled ? '已启用' : '已禁用' })
    fetchList()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await deleteCreditRule(id)
    meMsgSuccess({ message: '删除成功' })
    fetchList()
  } catch { /* handled */ }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCreditRules()
    if (res.code === 0) {
      list.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchList())
</script>
