<!-- src/views/system/dict/DictTypeEditModal.vue -->

<script setup lang="ts">
/**
 * DictTypeEditModal - 字典类型编辑弹窗
 *
 * 用于新增和编辑字典类型，支持表单校验和提交状态管理。
 * 通过 v-model:visible 控制显示，通过 editData 区分新增/编辑模式。
 *
 * @author 前端架构师
 */

import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { DictTypeDTO, DictTypeVO } from '@/api/admin/system/dict/type'
import { addDictType, updateDictType } from '@/api/admin/system/dict/api'
import { dictTypeFormFields, dictTypeFormRules } from './data'

/* ==================== Props & Emits ==================== */

interface Props {
  /** 弹窗可见性 */
  visible: boolean
  /** 编辑数据，为 null 时表示新增模式 */
  editData: DictTypeVO | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  /** 更新 visible */
  (e: 'update:visible', value: boolean): void
  /** 操作成功回调 */
  (e: 'success'): void
}>()

/* ==================== 状态管理 ==================== */

/** 弹窗标题 */
const modalTitle = computed(() => (props.editData ? '编辑字典类型' : '新增字典类型'))

/** 表单引用 */
const formRef = ref<FormInstance>()

/** 提交加载状态 */
const submitLoading = ref(false)

/** 表单数据 */
const formData = reactive<DictTypeDTO>({
  dictId: undefined,
  dictName: '',
  dictType: '',
  remark: ''
})

/* ==================== 监听 ==================== */

/** 监听 editData 变化，初始化表单 */
watch(
  () => props.editData,
  (val) => {
    if (val) {
      // 编辑模式：回填数据
      formData.dictId = val.dictId
      formData.dictName = val.dictName
      formData.dictType = val.dictType
      formData.remark = val.remark
    } else {
      // 新增模式：重置表单
      resetForm()
    }
  },
  { immediate: true }
)

/** 监听 visible 变化，关闭时重置表单 */
watch(
  () => props.visible,
  (val) => {
    if (!val) {
      setTimeout(() => resetForm(), 200)
    }
  }
)

/* ==================== 方法 ==================== */

/** 重置表单 */
function resetForm() {
  formData.dictId = undefined
  formData.dictName = ''
  formData.dictType = ''
  formData.remark = ''
  formRef.value?.resetFields()
}

/** 关闭弹窗 */
function handleClose() {
  emit('update:visible', false)
}

/** 提交表单 */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const apiFn = props.editData ? updateDictType : addDictType
    const res = await apiFn({ ...formData })
    if (res) {
      ElMessage.success(props.editData ? '修改成功' : '新增成功')
      emit('success')
      handleClose()
    }
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    :title="modalTitle"
    width="560px"
    :close-on-click-modal="false"
    destroy-on-close
    class="dict-type-modal"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="dictTypeFormRules"
      label-width="90px"
      class="px-2"
    >
      <el-form-item
        v-for="field in dictTypeFormFields"
        :key="field.prop"
        :prop="field.prop"
        :label="field.label"
      >
        <el-input
          v-if="field.type === 'input'"
          v-model="formData[field.prop as keyof DictTypeDTO]"
          :placeholder="field.placeholder"
          :maxlength="field.maxlength"
          clearable
          :disabled="field.prop === 'dictType' && !!editData"
        />
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="formData[field.prop as keyof DictTypeDTO]"
          :placeholder="field.placeholder"
          :maxlength="field.maxlength"
          :rows="field.rows"
          type="textarea"
          resize="none"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="handleClose" class="transition-all duration-300 hover:scale-105">
          取 消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
          class="transition-all duration-300 hover:scale-105"
        >
          确 定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dict-type-modal :deep(.el-dialog__body) {
  padding-top: 16px;
  padding-bottom: 8px;
}
</style>
