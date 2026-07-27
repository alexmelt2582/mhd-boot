<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'add' ? '新增字典项' : '编辑字典项'"
    width="500px"
    :close-on-click-modal="false"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
      <el-form-item label="字典标签" prop="dictLabel">
        <el-input v-model="form.dictLabel" placeholder="请输入字典标签（中文展示名）" />
      </el-form-item>
      <el-form-item label="字典值" prop="dictValue">
        <el-input v-model="form.dictValue" placeholder="请输入字典值（存储值，如 SEAT）" />
      </el-form-item>
      <el-form-item label="字典类型">
        <el-input :model-value="dictType" disabled />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息（选填）" />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" inline-prompt active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { formRules } from './data'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import type {DictVO} from "@/api/admin/system/dict/type.ts";
import {saveDict} from "@/api/admin/system/dict/api.ts";

const props = defineProps<{
  visible: boolean
  mode: 'add' | 'edit'
  dictType: string
  formData: Partial<DictVO>
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  saved: []
}>()

const formRef = ref()
const submitting = ref(false)

const form = reactive({
  dictLabel: '',
  dictValue: '',
  remark: '',
  sortOrder: 99,
  status: 1,
})

watch(
  () => props.visible,
  (val) => {
    if (val && props.formData) {
      form.dictLabel = props.formData.dictLabel || ''
      form.dictValue = props.formData.dictValue || ''
      form.remark = props.formData.remark || props.formData.remark || ''
      form.sortOrder = props.formData.sortOrder ?? 99
      form.status = props.formData.status ?? 1
    }
  },
)

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: any = {
      dictType: props.dictType,
      dictLabel: form.dictLabel,
      dictValue: form.dictValue,
      sortOrder: form.sortOrder,
      status: form.status,
      remark: form.remark,
    }
    if (props.mode === 'edit' && props.formData.id) {
      data.id = props.formData.id
    }
    const res = await saveDict(data)
    if (res.code === "0") {
      meMsgSuccess({ message: props.mode === 'add' ? '新增成功' : '更新成功' })
      emit('update:visible', false)
      emit('saved')
    }
  } finally {
    submitting.value = false
  }
}
</script>
