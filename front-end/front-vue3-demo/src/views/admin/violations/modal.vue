<template>
  <!-- Add Blacklist Modal -->
  <el-dialog v-model="blacklistVisible" title="添加黑名单" width="520px" :close-on-click-modal="false" @closed="handleBlacklistClosed">
    <el-form ref="blacklistFormRef" :model="blacklistForm" :rules="blacklistRules" label-width="100px" class="pr-4">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="blacklistForm.userId" class="w-full" placeholder="请选择用户" filterable>
          <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="违规类型" prop="violationType">
        <el-select v-model="blacklistForm.violationType" class="w-full">
          <el-option v-for="o in ViolationTypeOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="封禁天数" prop="penaltyDays">
        <el-input-number v-model="blacklistForm.penaltyDays" :min="1" :max="365" class="w-full" />
      </el-form-item>
      <el-form-item label="违规原因" prop="reason">
        <el-input v-model="blacklistForm.reason" type="textarea" :rows="3" placeholder="请详细描述违规原因" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="blacklistVisible = false">取消</el-button>
      <el-button type="primary" :loading="blacklistSubmitting" @click="handleBlacklistSubmit">确定</el-button>
    </template>
  </el-dialog>

  <!-- Adjust Credit Modal -->
  <el-dialog v-model="adjustVisible" title="手动调整积分" width="480px" :close-on-click-modal="false" @closed="handleAdjustClosed">
    <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px" class="pr-4">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="adjustForm.userId" class="w-full" placeholder="请选择用户" filterable>
          <el-option v-for="u in userOptions" :key="u.value" :label="u.label" :value="u.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="调整分数" prop="changeScore">
        <el-input-number v-model="adjustForm.changeScore" :min="-100" :max="100" class="w-full" />
        <span class="ml-2 text-xs text-slate-400">正数加分，负数扣分</span>
      </el-form-item>
      <el-form-item label="调整原因" prop="remark">
        <el-input v-model="adjustForm.remark" type="textarea" :rows="2" placeholder="请输入调整原因" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adjustVisible = false">取消</el-button>
      <el-button type="primary" :loading="adjustSubmitting" @click="handleAdjustSubmit">确定调整</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { addBlacklist } from '@/api/blacklist/api'
import { manualAdjustCredit } from '@/api/credit/api'
import { getUserList } from '@/api/user/api'
import { meMsgSuccess } from '@/utils/modal'
import { ViolationTypeOptions, defaultBlacklistForm, defaultAdjustForm } from './data'

const props = defineProps<{
  blacklistModelValue: boolean
  adjustModelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:blacklistModelValue', v: boolean): void
  (e: 'update:adjustModelValue', v: boolean): void
  (e: 'success'): void
}>()

const blacklistVisible = computed({
  get: () => props.blacklistModelValue,
  set: (v) => emit('update:blacklistModelValue', v),
})

const adjustVisible = computed({
  get: () => props.adjustModelValue,
  set: (v) => emit('update:adjustModelValue', v),
})

// Blacklist form
const blacklistFormRef = ref<FormInstance>()
const blacklistSubmitting = ref(false)
const blacklistForm = reactive({ ...defaultBlacklistForm })

const blacklistRules: FormRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  violationType: [{ required: true, message: '请选择违规类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入违规原因', trigger: 'blur' }],
}

// Adjust form
const adjustFormRef = ref<FormInstance>()
const adjustSubmitting = ref(false)
const adjustForm = reactive({ ...defaultAdjustForm })

const adjustRules: FormRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  changeScore: [
    { required: true, message: '请输入调整分数', trigger: 'blur' },
    { type: 'number', min: -100, max: 100, message: '分数范围 -100 ~ 100', trigger: 'blur' },
  ],
  remark: [{ required: true, message: '请输入调整原因', trigger: 'blur' }],
}

// User options
const userOptions = ref<{ label: string; value: number }[]>([])

async function loadUsers() {
  try {
    const res = await getUserList({ page: 1, pageSize: 200 })
    if (res.code === 0) {
      userOptions.value = res.data.list.map((u: any) => ({
        label: `${u.realName} (${u.username}) [${u.college}]`,
        value: u.id,
      }))
    }
  } catch { /* ignore */ }
}

loadUsers()

function handleBlacklistClosed() {
  blacklistFormRef.value?.resetFields()
  Object.assign(blacklistForm, defaultBlacklistForm)
}

async function handleBlacklistSubmit() {
  const valid = await blacklistFormRef.value?.validate().catch(() => false)
  if (!valid) return
  blacklistSubmitting.value = true
  try {
    await addBlacklist({
      userId: blacklistForm.userId!,
      violationType: blacklistForm.violationType,
      reason: blacklistForm.reason.trim(),
      penaltyDays: blacklistForm.penaltyDays,
    })
    meMsgSuccess({ message: '添加成功' })
    blacklistVisible.value = false
    emit('success')
  } catch { /* handled */ } finally {
    blacklistSubmitting.value = false
  }
}

function handleAdjustClosed() {
  adjustFormRef.value?.resetFields()
  Object.assign(adjustForm, defaultAdjustForm)
}

async function handleAdjustSubmit() {
  const valid = await adjustFormRef.value?.validate().catch(() => false)
  if (!valid) return
  adjustSubmitting.value = true
  try {
    await manualAdjustCredit({
      userId: adjustForm.userId!,
      changeScore: adjustForm.changeScore,
      remark: adjustForm.remark.trim(),
    })
    meMsgSuccess({ message: '积分调整成功' })
    adjustVisible.value = false
    emit('success')
  } catch { /* handled */ } finally {
    adjustSubmitting.value = false
  }
}
</script>
