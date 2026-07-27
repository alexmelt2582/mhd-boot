export interface SearchConfigItem {
  field: string                    // 表单字段名（与后端参数对应）
  label: string                    // 标签文本
  type: 'input' | 'select' | 'daterange'
  placeholder?: string
  clearable?: boolean              // 默认 true
  filterable?: boolean             // 仅 select 有效，默认 false
  valueFormat?: string             // 日期格式，如 'YYYY-MM-DD'
  options?: { label: string; value: any }[]  // select 的选项
  // 日期范围拆分字段（推荐使用，可直接映射后端字段）
  startField?: string
  endField?: string
}
