// src/views/system/dict/data.ts

import type { FormRules } from 'element-plus'

/* ================================================================
 * 字典类型 - 表格列配置
 * ================================================================ */

export interface DictTypeColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  align?: 'left' | 'center' | 'right'
  slot?: boolean
  formatter?: (row: any) => string
}

export const dictTypeColumns: DictTypeColumn[] = [
  { prop: 'dictId', label: '字典主键', width: 100, align: 'center' },
  { prop: 'dictName', label: '字典名称', minWidth: 140, align: 'left' },
  { prop: 'dictType', label: '字典类型', minWidth: 180, align: 'left' },
  { prop: 'remark', label: '备注', minWidth: 160, align: 'left' },
  { prop: 'createTime', label: '创建时间', width: 160, align: 'center' },
  { prop: 'action', label: '操作', width: 180, align: 'center', slot: true }
]

/* ================================================================
 * 字典类型 - 搜索表单字段配置
 * ================================================================ */

export interface SearchField {
  prop: string
  label: string
  placeholder: string
  type: 'input' | 'select' | 'date'
  options?: { label: string; value: string }[]
}

export const dictTypeSearchFields: SearchField[] = [
  { prop: 'dictName', label: '字典名称', placeholder: '请输入字典名称', type: 'input' },
  { prop: 'dictType', label: '字典类型', placeholder: '请输入字典类型', type: 'input' }
]

/* ================================================================
 * 字典类型 - 弹窗表单字段配置
 * ================================================================ */

export interface FormField {
  prop: string
  label: string
  type: 'input' | 'textarea' | 'number'
  placeholder?: string
  required?: boolean
  maxlength?: number
  rows?: number
}

export const dictTypeFormFields: FormField[] = [
  { prop: 'dictName', label: '字典名称', type: 'input', placeholder: '请输入字典名称', required: true, maxlength: 50 },
  { prop: 'dictType', label: '字典类型', type: 'input', placeholder: '请输入字典类型，如：sys_user_status', required: true, maxlength: 100 },
  { prop: 'remark', label: '备注', type: 'textarea', placeholder: '请输入备注信息', maxlength: 200, rows: 3 }
]

/** 字典类型表单校验规则 */
export const dictTypeFormRules: FormRules = {
  dictName: [
    { required: true, message: '字典名称不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  dictType: [
    { required: true, message: '字典类型不能为空', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '以小写字母开头，只允许小写、数字和下划线', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

/* ================================================================
 * 字典数据 - 表格列配置
 * ================================================================ */

export const dictItemColumns: DictTypeColumn[] = [
  { prop: 'dictItemId', label: '数据主键', width: 100, align: 'center' },
  { prop: 'dictLabel', label: '字典标签', minWidth: 120, align: 'left' },
  { prop: 'dictValue', label: '字典键值', width: 120, align: 'center' },
  { prop: 'sortOrder', label: '排序', width: 80, align: 'center' },
  { prop: 'status', label: '状态', width: 90, align: 'center', slot: true },
  { prop: 'remark', label: '备注', minWidth: 140, align: 'left' },
  { prop: 'createTime', label: '创建时间', width: 160, align: 'center' },
  { prop: 'action', label: '操作', width: 180, align: 'center', slot: true }
]

/* ================================================================
 * 字典数据 - 搜索表单字段配置
 * ================================================================ */

export const dictItemSearchFields: SearchField[] = [
  { prop: 'dictLabel', label: '字典标签', placeholder: '请输入字典标签', type: 'input' },
  { prop: 'dictValue', label: '字典键值', placeholder: '请输入字典键值', type: 'input' },
  {
    prop: 'status',
    label: '状态',
    placeholder: '请选择状态',
    type: 'select',
    options: [
      { label: '正常', value: '0' },
      { label: '停用', value: '1' }
    ]
  }
]

/* ================================================================
 * 字典数据 - 弹窗表单字段配置
 * ================================================================ */

export const dictItemFormFields: FormField[] = [
  { prop: 'dictLabel', label: '字典标签', type: 'input', placeholder: '请输入字典标签', required: true, maxlength: 50 },
  { prop: 'dictValue', label: '字典键值', type: 'input', placeholder: '请输入字典键值', required: true, maxlength: 100 },
  { prop: 'sortOrder', label: '显示排序', type: 'number', placeholder: '请输入排序号' },
  { prop: 'remark', label: '备注', type: 'textarea', placeholder: '请输入备注信息', maxlength: 200, rows: 3 }
]

/** 字典数据表单校验规则 */
export const dictItemFormRules: FormRules = {
  dictLabel: [
    { required: true, message: '字典标签不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '字典键值不能为空', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', min: 0, message: '排序号必须大于等于0', trigger: 'blur', transform: (v) => Number(v) }
  ]
}

/* ================================================================
 * 状态枚举（用于渲染）
 * ================================================================ */

export const statusOptions = [
  { label: '正常', value: '0', type: 'success' as const },
  { label: '停用', value: '1', type: 'danger' as const }
]

/** 根据状态值获取状态标签配置 */
export function getStatusOption(value: string) {
  return statusOptions.find(opt => opt.value === value) || { label: '未知', value, type: 'info' as const }
}
