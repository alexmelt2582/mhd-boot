// import type { DictVO } from '@/api/dict/type'
//
// /** 字典类型列表 */
// export const dictTypes = [
//   { type: 'space_type', label: '空间类型' },
//   { type: 'area_name', label: '区域名称' },
//   { type: 'floor', label: '楼层' },
//   { type: 'equipment_type', label: '设备类型' },
//   { type: 'violation_type', label: '违规类型' },
//   { type: 'message_type', label: '消息类型' },
// ]
//
// /** 表格列配置 */
// export const tableColumns = [
//   { prop: 'dictLabel', label: '字典标签', width: 180 },
//   { prop: 'dictValue', label: '字典值', width: 200 },
//   { prop: 'remark', label: '备注', minWidth: 200 },
//   { prop: 'sortOrder', label: '排序', width: 80 },
//   { prop: 'status', label: '状态', width: 80 },
//   { prop: 'createTime', label: '创建时间', width: 180 },
// ]
//
// /** 表单默认值 */
// export function defaultForm(): Partial<DictVO> {
//   return {
//     dictLabel: '',
//     dictValue: '',
//     remark: '',
//     sortOrder: 99,
//     status: 1,
//   }
// }
//
// /** 表单校验规则 */
// export const formRules = {
//   dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
//   dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
//   sortOrder: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
// }

import type { FormRules } from 'element-plus';

// 字典类型 弹窗表单配置
export const dictTypeForm = {
  id: '',
  dictName: '',
  dictType: '',
  status: '0',
  remark: ''
};

// 字典数据项 弹窗表单配置
export const dictItemForm = {
  id: '',
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: '0',
  remark: ''
};

// 字典类型 表单校验规则
export const dictTypeRules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
};

// 字典数据项 表单校验规则
export const dictItemRules: FormRules = {
  dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入数据键值', trigger: 'blur' }]
};

// 右侧字典数据项表格列配置
export const dictItemColumns = [
  { prop: 'dictLabel', label: '字典标签', width: 120 },
  { prop: 'dictValue', label: '字典键值', width: 120 },
  { prop: 'sort', label: '排序', width: 80 },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    render: (row: any) => (row.status === '0' ? '正常' : '停用')
  },
  { prop: 'remark', label: '备注' },
  { prop: 'createTime', label: '创建时间', width: 180 }
];
