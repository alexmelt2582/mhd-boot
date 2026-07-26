<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑设备' : '新增设备'" width="600px" :close-on-click-modal="false" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="pr-4">
      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="form.equipmentName" placeholder="请输入设备名称" maxlength="50" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="设备类型" prop="equipmentType">
            <el-select v-model="form.equipmentType" class="w-full" placeholder="请选择">
              <el-option v-for="o in EquipmentTypeOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备型号" prop="equipmentModel">
            <el-input v-model="form.equipmentModel" placeholder="请输入型号" maxlength="50" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="所属空间" prop="spaceId">
        <el-select v-model="form.spaceId" class="w-full" placeholder="选择空间（可选）" clearable filterable>
          <el-option v-for="s in spaceOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="采购日期">
            <el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" class="w-full" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维护日期">
            <el-date-picker v-model="form.lastMaintenanceDate" type="date" placeholder="选择日期" class="w-full" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">故障</el-radio>
          <el-radio :value="2">报废</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" maxlength="200" show-word-limit />
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
import { createEquipment, updateEquipment } from '@/api/equipment/api'
import { getSpaceList } from '@/api/space/api'
import type { SpaceVO } from '@/api/space/type'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import { EquipmentTypeOptions, defaultForm } from './data'

const props = defineProps<{
  modelValue: boolean
  editingId: number | null
  editData: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const isEdit = computed(() => !!props.editingId)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const spaceOptions = ref<{ label: string; value: number }[]>([])

const form = reactive({ ...defaultForm })

const rules: FormRules = {
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
}

async function loadSpaces() {
  try {
    const res = await getSpaceList({ page: 1, pageSize: 200 })
    if (res.code === 0) {
      spaceOptions.value = res.data.list.map((s: SpaceVO) => ({
        label: `${s.spaceName} (${s.areaName} ${s.floor})`,
        value: s.id,
      }))
    }
  } catch { /* ignore */ }
}

loadSpaces()

watch(
  () => props.editData,
  (val) => {
    if (val) {
      form.equipmentName = val.equipmentName || ''
      form.equipmentType = val.equipmentType || ''
      form.spaceId = val.spaceId ?? null
      form.equipmentModel = val.equipmentModel || ''
      form.purchaseDate = val.purchaseDate || ''
      form.lastMaintenanceDate = val.lastMaintenanceDate || ''
      form.status = val.status ?? 1
      form.remark = val.remark || ''
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
      id: props.editingId,
      equipmentName: form.equipmentName.trim(),
      equipmentType: form.equipmentType,
      spaceId: form.spaceId,
      equipmentModel: form.equipmentModel.trim() || undefined,
      purchaseDate: form.purchaseDate || undefined,
      lastMaintenanceDate: form.lastMaintenanceDate || undefined,
      remark: form.remark.trim() || undefined,
    }

    if (isEdit.value) {
      await updateEquipment(data)
      meMsgSuccess({ message: '更新成功' })
    } else {
      delete data.id
      await createEquipment(data)
      meMsgSuccess({ message: '创建成功' })
    }
    visible.value = false
    emit('success')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>
