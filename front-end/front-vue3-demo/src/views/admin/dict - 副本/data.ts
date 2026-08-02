import type {FormRules} from 'element-plus';

/** 字典状态枚举 */
export enum DictStatus {
  /** 正常/启用 */
  NORMAL = 0,
  /** 停用/禁用 */
  DISABLED = 1
}

/** 字典状态选项 */
export const DICT_STATUS_OPTIONS = [
  { label: '正常', value: DictStatus.NORMAL },
  { label: '停用', value: DictStatus.DISABLED }
]

/** 字典状态映射 */
export const DICT_STATUS_MAP: Record<number, string> = {
  [DictStatus.NORMAL]: '正常',
  [DictStatus.DISABLED]: '停用'
}


// ==================== 表单校验规则 ====================

// 字典类型 表单校验规则
export const dictTypeFormRules: FormRules = {
  dictName: [
    {required: true, message: '请输入字典名称', trigger: 'blur'},
    {max: 100, message: `字典名称长度不能超过100个字符`, trigger: 'blur'}
  ],
  dictType: [
    {required: true, message: '请输入字典类型', trigger: 'blur'},
    {max: 100, message: `字典类型长度不能超过100个字符`, trigger: 'blur'},
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '字典类型必须以字母开头，只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  remark: [
    {max: 200, message: `备注长度不能超过200个字符`, trigger: 'blur'}
  ]
};

// 字典数据项 表单校验规则
export const dictItemFormRules: FormRules = {
  dictType: [
    { required: true, message: '请选择字典类型', trigger: 'change' }
  ],
  dictLabel: [
    { required: true, message: '请输入字典标签', trigger: 'blur' },
    { max: 100, message: `字典标签长度不能超过100个字符`, trigger: 'blur' }
  ],
  dictValue: [
    { required: true, message: '请输入字典值', trigger: 'blur' },
    { max: 100, message: `字典值长度不能超过100个字符`, trigger: 'blur' }
  ],
  dictSort: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    {
      type: 'number',
      min: 0,
      max: 10000,
      message: `排序号必须在0到10000之间`,
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// ==================== 表单初始值 ====================

/**
 * 获取字典类型表单初始值
 * 使用工厂函数而非直接导出对象，避免引用污染
 */
export const initDictTypeFormData = () => ({
  dictId: undefined as number | undefined,
  dictName: '',
  dictType: '',
  remark: ''
})

/**
 * 获取字典数据表单初始值
 * @param dictType - 可选的默认字典类型
 * @param dictSort - 可选的默认排序值
 */
export const initDictItemFormData = (dictType?: string, dictSort?: number) => ({
  dictItemId: undefined as number | undefined,
  dictLabel: '',
  dictValue: '',
  dictType: dictType || '',
  dictSort: dictSort ?? 1,
  status: DictStatus.NORMAL,
  remark: ''
})
