<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑规则' : '新增规则'" width="560px" :close-on-click-modal="false" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="pr-4">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="form.ruleName" placeholder="请输入规则名称" maxlength="50" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="规则类型" prop="ruleType">
            <el-select v-model="form.ruleType" class="w-full">
              <el-option v-for="o in RuleTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分值变动" prop="changeValue">
            <el-input-number v-model="form.changeValue" :min="-100" :max="100" class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="关联类型" prop="referenceType">
        <el-select v-model="form.referenceType" class="w-full" placeholder="请选择关联的信用参考类型">
          <el-option v-for="o in ReferenceTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="规则描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入规则描述" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="启用状态">
        <el-switch v-model="form.isEnabled" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import type { CreditRuleVO } from '@/api/credit/type'
import { saveCreditRule } from '@/api/credit/api'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import { RuleTypeOptions, ReferenceTypeOptions, defaultForm } from './data'

const props = defineProps<{
  modelValue: boolean
  editingRule: CreditRuleVO | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const isEdit = computed(() => !!props.editingRule?.id)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({ ...defaultForm })

const rules: FormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  changeValue: [
    { required: true, message: '请输入分值变动', trigger: 'blur' },
    { type: 'number', min: -100, max: 100, message: '分值范围 -100 ~ 100', trigger: 'blur' },
  ],
  referenceType: [{ required: true, message: '请选择关联类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入规则描述', trigger: 'blur' }],
}

watch(
  () => props.editingRule,
  (val) => {
    if (val) {
      form.ruleName = val.ruleName || ''
      form.ruleType = val.ruleType || 'REWARD'
      form.changeValue = val.changeValue ?? 0
      form.referenceType = val.referenceType || ''
      form.description = val.description || ''
      form.isEnabled = val.isEnabled ?? 1
    } else {
      Object.assign(form, defaultForm)
    }
  },
  { immediate: true },
)

function handleClosed() {
  formRef.value?.resetFields()
  Object.assign(form, defaultForm)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: any = {
      id: props.editingRule?.id,
      ruleName: form.ruleName.trim(),
      ruleType: form.ruleType,
      changeValue: form.changeValue,
      referenceType: form.referenceType,
      description: form.description.trim(),
      isEnabled: form.isEnabled,
    }
    if (!data.id) delete data.id
    await saveCreditRule(data)
    meMsgSuccess({ message: isEdit.value ? '更新成功' : '创建成功' })
    visible.value = false
    emit('success')
  } catch { /* handled */ } finally {
    submitting.value = false
  }
}
</script>
