<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑空间' : '新增空间'" width="620px" :close-on-click-modal="false" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="pr-4">
      <el-form-item label="空间名称" prop="spaceName">
        <el-input v-model="form.spaceName" placeholder="请输入空间名称" maxlength="50" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="空间类型" prop="spaceType">
            <el-select v-model="form.spaceType" class="w-full">
              <el-option v-for="o in SpaceTypeOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属楼层" prop="floor">
            <el-select v-model="form.floor" class="w-full">
              <el-option v-for="o in FloorOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属区域" prop="areaName">
            <el-select v-model="form.areaName" class="w-full">
              <el-option v-for="o in AreaNameOptions.filter(e => e.value)" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="容纳人数" prop="capacity">
            <el-input-number v-model="form.capacity" :min="1" :max="100" class="w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="999" class="w-48" />
      </el-form-item>
      <el-form-item label="设备配置" prop="equipmentConfig">
        <el-input v-model="form.equipmentConfig" type="textarea" :rows="3" placeholder='JSON格式，例如: {"projector":true,"whiteboard":true,"network":"WiFi"}' />
        <span class="text-xs text-slate-400">请输入JSON格式的设备配置，留空表示无特殊配置</span>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="空间描述" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="使用规则" prop="useRules">
        <el-input v-model="form.useRules" type="textarea" :rows="2" placeholder="使用规则说明" maxlength="500" show-word-limit />
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
import { createSpace, updateSpace } from '@/api/space/api'
import { meMsgSuccess, meMsgError } from '@/utils/modal'
import { SpaceTypeOptions, AreaNameOptions, FloorOptions, defaultForm } from './data'

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

const form = reactive({ ...defaultForm })

const rules: FormRules = {
  spaceName: [{ required: true, message: '请输入空间名称', trigger: 'blur' }],
  spaceType: [{ required: true, message: '请选择空间类型', trigger: 'change' }],
  areaName: [{ required: true, message: '请选择区域', trigger: 'change' }],
  floor: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
}

watch(
  () => props.editData,
  (val) => {
    if (val && props.editingId) {
      form.spaceName = val.spaceName || ''
      form.spaceType = val.spaceType || 'SEAT'
      form.areaName = val.areaName || ''
      form.floor = val.floor || '1F'
      form.capacity = val.capacity || 1
      form.sortOrder = val.sortOrder ?? 0
      form.description = val.description || ''
      form.useRules = val.useRules || ''
      form.equipmentConfig = val.equipmentConfig ? JSON.stringify(val.equipmentConfig, null, 2) : ''
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
    let equipmentConfig: any = undefined
    if (form.equipmentConfig.trim()) {
      try {
        equipmentConfig = JSON.parse(form.equipmentConfig)
      } catch {
        meMsgError({ message: '设备配置JSON格式不正确' })
        submitting.value = false
        return
      }
    }

    const data: any = {
      id: props.editingId,
      spaceName: form.spaceName.trim(),
      spaceType: form.spaceType,
      areaName: form.areaName,
      floor: form.floor,
      capacity: form.capacity,
      sortOrder: form.sortOrder,
      description: form.description.trim() || undefined,
      useRules: form.useRules.trim() || undefined,
      equipmentConfig,
    }

    if (isEdit.value) {
      await updateSpace(data)
      meMsgSuccess({ message: '更新成功' })
    } else {
      delete data.id
      await createSpace(data)
      meMsgSuccess({ message: '创建成功' })
    }
    visible.value = false
    emit('success')
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>
