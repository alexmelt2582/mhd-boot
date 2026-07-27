export const RuleTypeOptions = [
  { label: '加分', value: 'REWARD' },
  { label: '扣分', value: 'PUNISH' },
]

export const tableColumns = [
  { prop: 'id', label: 'ID', width: 70 },
  { prop: 'ruleName', label: '规则名称', minWidth: 150 },
  { prop: 'ruleType', label: '规则类型', width: 90 },
  { prop: 'changeValue', label: '分值变动', width: 90 },
  { prop: 'description', label: '规则描述', minWidth: 200 },
  { prop: 'isEnabled', label: '启用状态', width: 90 },
  { prop: 'sortOrder', label: '排序', width: 70 },
]

export const ReferenceTypeOptions = [
  { label: '签到履约', value: 'CHECKIN' },
  { label: '连续履约', value: 'CONSECUTIVE' },
  { label: '预约违约', value: 'DEFAULTED' },
  { label: '严重违约', value: 'SEVERE_DEFAULT' },
  { label: '取消预约', value: 'CANCEL' },
  { label: '手动调整', value: 'MANUAL' },
]

export const defaultForm = {
  ruleName: '',
  ruleType: 'REWARD' as string,
  changeValue: 2,
  referenceType: '' as string,
  description: '',
  isEnabled: 1,
}
