<!-- views/system/dict/components/DictItemDialog.vue -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑字典项' : '新增字典项'"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    @update:model-value="handleUpdateVisible"
    @closed="handleClosed"
  >
    <!-- 表单区域 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="所属字典" prop="dictType" v-if="!isEdit">
        <el-select
          v-model="formData.dictType"
          placeholder="请选择字典类型"
          class="w-full"
          filterable
        >
          <el-option
            v-for="item in dictTypeOptions"
            :key="item.dictType"
            :label="`${item.dictName}（${item.dictType}）`"
            :value="item.dictType"
          />
        </el-select>
      </el-form-item>

      <!-- 编辑时显示字典类型（只读） -->
      <el-form-item label="所属字典" v-else>
        <el-input :model-value="formData.dictType" disabled/>
      </el-form-item>

      <el-form-item label="字典标签" prop="dictLabel">
        <el-input
          v-model="formData.dictLabel"
          placeholder="请输入字典标签，用于前端展示（如：男）"
          maxlength="100"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="字典值" prop="dictValue">
        <el-input
          v-model="formData.dictValue"
          placeholder="请输入字典值，与后端存储对应（如：1）"
          maxlength="100"
          show-word-limit
          clearable
        >
          <template #append>
            <el-tooltip
              content="字典值是后端存储的实际值，通常为数字或英文字符串"
              placement="top"
            >
              <el-icon>
                <QuestionFilled/>
              </el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="排序号" prop="dictSort">
        <el-input-number
          v-model="formData.dictSort"
          min="0"
          max="10000"
          controls-position="right"
          class="w-full"
          placeholder="请输入排序号，数值越小越靠前"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio
            v-for="option in DICT_STATUS_OPTIONS"
            :key="option.value"
            :label="option.value"
          >
            {{ option.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="submitting">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * DictItemDialog - 字典数据（字典项）新增/编辑弹窗
 *
 * @description
 * 用于新增或编辑字典数据项，支持以下功能：
 * - 新增模式：可选择所属字典类型
 * - 编辑模式：自动加载详情数据，字典类型不可更改
 * - 表单验证：标签、值、排序号、状态等字段校验
 * - 防重复提交：提交中禁用所有操作按钮
 *
 * @example
 * // 新增
 * <DictItemDialog
 *   v-model="visible"
 *   :dict-type-options="typeList"
 *   :default-dict-type="currentType"
 *   @success="handleSuccess"
 * />
 *
 * // 编辑
 * <DictItemDialog
 *   v-model="visible"
 *   :dict-item-id="editId"
 *   :dict-type-options="typeList"
 *   @success="handleSuccess"
 * />
 */
import {computed, reactive, ref, watch} from 'vue'
import type {FormInstance} from 'element-plus'
import {ElMessage} from 'element-plus'
import {QuestionFilled} from '@element-plus/icons-vue'
import {addDictItem, getDictItemDetail, updateDictItem} from "@/api/admin/system/dict/api";
import type {DictItemDTO, DictTypeVO} from "@/api/admin/system/dict/type";
import {DICT_STATUS_OPTIONS, dictItemFormRules, initDictItemFormData} from '../data'

// ==================== Props & Emits ====================

interface Props {
  /** 控制弹窗显示/隐藏（v-model） */
  modelValue: boolean
  /** 编辑时的字典项ID，新增时传 null 或 undefined */
  dictItemId?: number | null
  /** 可选的字典类型列表（用于新增时选择所属字典） */
  dictTypeOptions?: DictTypeVO[]
  /** 默认的字典类型（新增时自动选中，通常从左侧列表传入） */
  defaultDictType?: string
}

interface Emits {
  /** 更新 v-model 值 */
  (e: 'update:modelValue', value: boolean): void

  /** 操作成功后的回调 */
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  dictItemId: null,
  dictTypeOptions: () => [],
  defaultDictType: ''
})

const emit = defineEmits<Emits>()

// ==================== 响应式数据 ====================

/** 表单引用 */
const formRef = ref<FormInstance>()
/** 加载详情数据的状态 */
const loading = ref(false)
/** 提交按钮的加载状态 */
const submitting = ref(false)
/** 表单校验规则 */
const rules = dictItemFormRules
/** 表单数据 */
const formData = reactive<DictItemDTO>(initDictItemFormData())

// ==================== 计算属性 ====================

/** 是否为编辑模式 */
const isEdit = computed(() => !!props.dictItemId)

// ==================== 监听器 ====================

/**
 * 监听弹窗打开状态
 *
 * 当弹窗打开时：
 * - 编辑模式：根据 dictItemId 加载详情数据
 * - 新增模式：使用默认字典类型初始化表单
 */
watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    // 先重置表单
    formRef.value?.resetFields()
    if (isEdit.value && props.dictItemId) {
      // 编辑模式：加载详情数据
      await loadDetail(props.dictItemId)
    } else {
      // 新增模式：使用默认值初始化
      const defaultSort = 0 // 可扩展：根据已有数据计算下一个排序号
      Object.assign(formData, initDictItemFormData(props.defaultDictType, defaultSort))
    }
  }
)

// ==================== 方法 ====================

/**
 * 加载字典数据详情（编辑时使用）
 *
 * @param dictItemId - 字典项ID
 *
 * 加载失败时自动关闭弹窗，避免显示空白表单
 */
const loadDetail = async (dictItemId: number) => {
  loading.value = true
  try {
    const {data} = await getDictItemDetail(dictItemId)

    // 将详情数据填充到表单
    Object.assign(formData, {
      dictItemId: data.dictItemId,
      dictLabel: data.dictLabel,
      dictValue: data.dictValue,
      dictType: data.dictType,
      dictSort: data.dictSort,
      status: data.status,
      remark: data.remark || ''
    })
  } catch (error: any) {
    const msg = error?.message || error?.msg || ''
    console.error('加载字典项详情失败:', msg, error)
    handleCancel()
  } finally {
    loading.value = false
  }
}

/**
 * 更新弹窗显示状态
 *
 * @param visible - 是否显示
 *
 * 注意：提交中不允许关闭弹窗，防止误操作
 */
const handleUpdateVisible = (visible: boolean) => {
  // 提交中禁止关闭弹窗
  if (submitting.value && !visible) return
  emit('update:modelValue', visible)
}

/**
 * 取消按钮点击
 * 提交中时不允许取消，防止意外关闭
 */
const handleCancel = () => {
  if (submitting.value) return
  emit('update:modelValue', false)
}

/**
 * 弹窗完全关闭后的回调
 * 用于清理表单验证状态和临时数据
 */
const handleClosed = () => {
  formRef.value?.resetFields()
}

/**
 * 提交表单
 *
 * 处理流程：
 * 1. 防重复提交检查
 * 2. 表单验证
 * 3. 调用新增或更新接口
 * 4. 成功后关闭弹窗并通知父组件
 * 5. 失败时显示错误信息
 */
const handleSubmit = async () => {
  // 防止重复提交
  if (submitting.value) return

  // 表单验证（使用 catch 处理验证失败的情况）
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 构建提交数据
    const submitData: DictItemDTO = {
      dictLabel: formData.dictLabel,
      dictValue: formData.dictValue,
      dictType: formData.dictType,
      dictSort: formData.dictSort,
      status: formData.status,
      remark: formData.remark
    }

    // 根据模式调用不同接口
    if (isEdit.value) {
      submitData.dictItemId = formData.dictItemId
      await updateDictItem(submitData)
      ElMessage.success('修改成功')
    } else {
      await addDictItem(submitData)
      ElMessage.success('新增成功')
    }

    // 关闭弹窗并通知父组件刷新
    emit('update:modelValue', false)
    emit('success')
  } catch (error: any) {
    const msg = error?.message || error?.msg || ''
    console.error('提交字典项失败:', msg, error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
