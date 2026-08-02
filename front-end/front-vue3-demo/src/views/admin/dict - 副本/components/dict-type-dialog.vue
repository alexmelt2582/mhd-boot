<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑字典类型' : '新增字典类型'"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    append-to-body
    @update:model-value="handleUpdateVisible"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" v-loading="loading">
      <el-form-item label="字典名称" prop="dictName">
        <el-input
          v-model="formData.dictName"
          placeholder="请输入字典名称（中文展示名）"
          maxlength="100"
          show-word-limit
          clearable
        />
      </el-form-item>
      <el-form-item label="字典类型" prop="dictType">
        <el-input
          v-model="formData.dictType"
          placeholder="请输入字典类型（如：sys_user_sex）"
          maxlength="100"
          show-word-limit
          :disabled="isEdit"
          clearable
        >
          <template #append>
            <el-tooltip content="字典类型的唯一标识，建议使用字母、数字和下划线" placement="top">
              <el-icon>
                <QuestionFilled/>
              </el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="flex justify-end gap-3"></div>
      <el-button @click="handleCancel" :disabled="submitting" class="transition-all duration-300 hover:scale-105">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {computed, reactive, ref, watch} from 'vue'
import type {FormInstance} from 'element-plus'
import {useMessage} from "@/hooks/message";
import type {DictTypeDTO} from "@/api/admin/system/dict/type";
import {addDictType, getDictTypeDetail, updateDictType} from "@/api/admin/system/dict/api";
import {dictTypeFormRules, initDictTypeFormData} from "@/views/admin/dict/data";

// ==================== Props & Emits ====================
interface Props {
  /** 控制弹窗显示/隐藏 */
  modelValue: boolean
  /** 编辑时的字典ID，新增时传 null 或 undefined */
  dictId?: number | null
}

interface Emits {
  /** 更新 v-model 值 */
  (e: 'update:modelValue', value: boolean): void

  /** 操作成功后的回调 */
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  dictId: null
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
const rules = dictTypeFormRules
/** 表单数据 */
const formData = reactive<DictTypeDTO>(initDictTypeFormData())

// ==================== 计算属性 ====================

/** 是否为编辑模式 */
const isEdit = computed(() => !!props.dictId)

// ==================== 监听器 ====================

/**
 * 监听弹窗打开，自动加载编辑数据
 *
 * 当弹窗打开时：
 * - 编辑模式：根据 dictId 加载详情数据
 * - 新增模式：使用默认初始化表单
 */
watch(
  () => props.modelValue,
  async (visible) => {
    if (!visible) return
    // 重置表单
    formRef.value?.resetFields()
    // 编辑模式：加载详情
    if (isEdit.value && props.dictId) {
      await loadDetail(props.dictId)
    } else {
      // 新增模式：重置为初始值
      Object.assign(formData, initDictTypeFormData())
    }
  }
)

// ==================== 方法 ====================

/**
 * 加载字典数据详情（编辑时使用）
 *
 * @param dictId - 字典项ID
 *
 * 加载失败时自动关闭弹窗，避免显示空白表单
 */
const loadDetail = async (dictId: number) => {
  loading.value = true
  try {
    const {data} = await getDictTypeDetail(dictId)
    // 将详情数据填充到表单
    Object.assign(formData, {
      dictId: data.dictId,
      dictName: data.dictName,
      dictType: data.dictType,
      remark: data.remark || ''
    })
  } catch (error: any) {
    const msg = error?.message || error?.msg || ''
    console.error('加载字典类型详情失败:', msg, error)
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
  // 提交中不允许关闭弹窗
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
 * 用于清理表单验证状态
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

  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 构建提交数据
    const submitData: DictTypeDTO = {
      dictName: formData.dictName,
      dictType: formData.dictType,
      remark: formData.remark
    }

    // 根据模式调用不同接口
    if (isEdit.value) {
      submitData.dictId = formData.dictId
      await updateDictType(submitData)
      useMessage().success('修改成功')
    } else {
      await addDictType(formData)
      useMessage().success('新增成功')
    }

    // 关闭弹窗并通知父组件
    emit('update:modelValue', false)
    emit('success')
  } catch (error: any) {
    const msg = error?.message || error?.msg || ''
    console.error('提交字典类型失败:', msg, error)
  } finally {
    submitting.value = false
  }
}
</script>
