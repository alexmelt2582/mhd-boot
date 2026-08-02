<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    :title="modalTitle"
    width="560px"
    :close-on-click-modal="false"
    destroy-on-close
    class="dict-item-modal"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="dictItemFormRules"
      label-width="90px"
      class="px-2"
    >
      <!-- 字典类型（只读展示） -->
      <el-form-item label="字典类型">
        <el-tag type="info" size="large" class="font-mono text-sm">
          {{ currentDictType || '未选择' }}
        </el-tag>
      </el-form-item>

      <el-form-item
        v-for="field in dictItemFormFields"
        :key="field.prop"
        :prop="field.prop"
        :label="field.label"
      >
        <!-- 文本输入 -->
        <el-input
          v-if="field.type === 'input'"
          v-model="formData[field.prop as keyof typeof formData]"
          :placeholder="field.placeholder"
          :maxlength="field.maxlength"
          clearable
        />
        <!-- 文本域 -->
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="formData[field.prop as keyof typeof formData]"
          :placeholder="field.placeholder"
          :maxlength="field.maxlength"
          :rows="field.rows"
          type="textarea"
          resize="none"
        />
      </el-form-item>

      <!-- 状态（单独处理，使用单选） -->
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="opt in statusOptions"
            :key="opt.value"
            :label="opt.value"
          >
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
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

<script setup lang="ts">
/**
 * DictItemEditModal - 字典数据编辑弹窗
 *
 * 用于新增和编辑字典数据项，自动绑定当前字典类型。
 * 通过 v-model:visible 控制显示，通过 editData 区分新增/编辑模式。
 *
 * @author 前端架构师
 */

import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { DictItemDTO, DictItemVO } from '@/api/admin/system/dict/type'
import { addDictItem, updateDictItem } from '@/api/admin/system/dict/api'
import { dictItemFormFields, dictItemFormRules, statusOptions } from './data'

/* ==================== Props & Emits ==================== */

interface Props {
  /** 弹窗可见性 */
  visible: boolean
  /** 编辑数据，为 null 时表示新增模式 */
  editData: DictItemVO | null
  /** 当前所属字典类型（新增时必填） */
  currentDictType: string
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
const modalTitle = computed(() => (props.editData ? '编辑字典数据' : '新增字典数据'))

/** 表单引用 */
const formRef = ref<FormInstance>()

/** 提交加载状态 */
const submitLoading = ref(false)

/** 表单数据 */
const formData = reactive<DictItemDTO & { status: string }>({
  dictItemId: undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  remark: ''
})

/* ==================== 监听 ==================== */

/** 监听 editData 变化，初始化表单 */
watch(
  () => props.editData,
  (val) => {
    if (val) {
      formData.dictItemId = val.dictItemId
      formData.dictType = val.dictType
      formData.dictLabel = val.dictLabel
      formData.dictValue = val.dictValue
      formData.remark = val.remark
    } else {
      resetForm()
      // 新增时自动填充当前字典类型
      formData.dictType = props.currentDictType
    }
  },
  { immediate: true }
)

/** 监听 visible 变化 */
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
  formData.dictItemId = undefined
  formData.dictType = props.currentDictType
  formData.dictLabel = ''
  formData.dictValue = ''
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

  // 校验字典类型
  if (!formData.dictType) {
    ElMessage.warning('请先选择字典类型')
    return
  }

  submitLoading.value = true
  try {
    const apiFn = props.editData ? updateDictItem : addDictItem
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

<style scoped>
.dict-item-modal :deep(.el-dialog__body) {
  padding-top: 16px;
  padding-bottom: 8px;
}
.dict-item-modal :deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>
